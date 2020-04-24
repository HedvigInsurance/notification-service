package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreator
import com.hedvig.notificationService.customerio.events.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class NorwaySignHackUpdateCustomerIOTest {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    @MockK(relaxed = true)
    lateinit var seCustomerioClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var noCustomerIoClient: CustomerioClient

    lateinit var eventCreator: CustomerioEventCreator

    private val repository =
        InMemoryCustomerIOStateRepository()

    @MockK
    lateinit var sut: CustomerioService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        eventCreator = CustomerioEventCreatorImpl(productPricingFacade)

        sut = CustomerioService(
            WorkspaceSelector(
                productPricingFacade,
                memberServiceImpl
            ),
            repository,
            eventCreator,
            mapOf(
                Workspace.SWEDEN to seCustomerioClient,
                Workspace.NORWAY to noCustomerIoClient
            )
        )
    }

    @Test
    fun sendUpdatesAfterWindowsTimeLengthSendsCustomerIOUpdate() {

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        val updateTime = Instant.parse("2020-04-15T14:53:40.550493Z")

        sut.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), updateTime
        )

        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )

        sut.sendUpdates(updateTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val eventDataSlot = slot<Map<String, Any>>()
        verify { noCustomerIoClient.sendEvent("1337", capture(eventDataSlot)) }
        Assertions.assertThat(eventDataSlot.captured).containsEntry("name", "TmpSignedInsuranceEvent")
    }

    @Test
    fun sendUpdatesBeforeWindowTimeLengthDoesNotSendCustomerIOUpdate() {

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        val updateTime = Instant.parse("2020-04-15T14:53:40.550493Z")
        sut.updateCustomerAttributes(
            "42", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), updateTime
        )

        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )
        sut.sendUpdates(updateTime.plus(1, ChronoUnit.SECONDS))

        verify(inverse = true) { noCustomerIoClient.sendEvent(any(), any()) }
    }

    @Test
    fun sendUpdatesAfterWindowTimeWithTwoMembers() {

        val someTime = Instant.parse("2020-04-15T14:53:40.550493Z")
        repository.save(
            CustomerioState(
                "memberOne",
                someTime
            )
        )
        repository.save(
            CustomerioState(
                "memberTwo",
                someTime
            )
        )

        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )
        sut.sendUpdates(someTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        verify { noCustomerIoClient.sendEvent("memberOne", any()) }
        verify { noCustomerIoClient.sendEvent("memberTwo", any()) }
    }

    @Test
    fun sendUpdatesWithNothingToUpdateDoesNotCallCustomerio() {

        val someTime = Instant.parse("2020-04-15T14:53:40.550493Z")

        sut.sendUpdates(someTime)

        verify(inverse = true) { noCustomerIoClient.sendEvent(any(), any()) }
    }

    @Test
    fun `two updates causes only one sent event`() {
        val time = Instant.parse("2020-04-15T14:53:40.550493Z")

        repository.save(
            CustomerioState(
                "someMemberID",
                time
            )
        )

        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )
        sut.sendUpdates(time.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))
        sut.sendUpdates(time.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        verify(atMost = 1) { noCustomerIoClient.sendEvent("someMemberID", any()) }
    }

    @Test
    fun `swedish contract causes exception to be thrown`() {
        val time = Instant.parse("2020-04-15T14:53:40.550493Z")

        repository.save(
            CustomerioState(
                "someMemberID",
                time
            )
        )

        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.SwedishApartment,
                null,
                null
            )
        )
        sut.sendUpdates(time.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        verify(inverse = true) { noCustomerIoClient.sendEvent("someMemberID", any()) }
    }
}
