package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class NorwaySignHackUpdateCustomerIOTest {

    @MockK
    lateinit var contractLoader: ContractLoader

    @MockK
    lateinit var memberServiceImpl: MemberServiceImpl

    @MockK(relaxed = true)
    lateinit var seCustomerioClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var noCustomerIoClient: CustomerioClient

    lateinit var eventCreator: CustomerioEventCreator

    private val repository =
        InMemoryCustomerIOStateRepository()

    lateinit var customerioService: CustomerioService

    lateinit var scheduler: CustomerioUpdateScheduler

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        eventCreator = CustomerioEventCreatorImpl()

        customerioService = CustomerioService(
            workspaceSelector,
            repository,
            mapOf(
                Workspace.SWEDEN to seCustomerioClient,
                Workspace.NORWAY to noCustomerIoClient
            ),
            mockk()
        )

        scheduler = CustomerioUpdateScheduler(
            eventCreator, repository, contractLoader, customerioService
        )
    }

    @Test
    fun `do not update attributes from underwriter`() {

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val updateTime = Instant.parse("2020-04-15T14:53:40.550493Z")

        customerioService.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), updateTime
        )

        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            makeContractInfo()
        )

        scheduler.sendUpdates(updateTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val eventDataSlot = slot<Map<String, Any>>()
        verify(inverse = true) { noCustomerIoClient.sendEvent("1337", capture(eventDataSlot)) }
    }

    @Test
    fun sendUpdatesBeforeWindowTimeLengthDoesNotSendCustomerIOUpdate() {

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val updateTime = Instant.parse("2020-04-15T14:53:40.550493Z")
        customerioService.updateCustomerAttributes(
            "42", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), updateTime
        )

        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            makeContractInfo()
        )
        scheduler.sendUpdates(updateTime.plus(1, ChronoUnit.SECONDS))

        verify(inverse = true) { noCustomerIoClient.sendEvent(any(), any()) }
    }

    @Test
    fun sendUpdatesWithNothingToUpdateDoesNotCallCustomerio() {

        val someTime = Instant.parse("2020-04-15T14:53:40.550493Z")
        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        scheduler.sendUpdates(someTime)

        verify(inverse = true) { noCustomerIoClient.sendEvent(any(), any()) }
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

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            makeContractInfo(AgreementType.SwedishApartment)
        )
        scheduler.sendUpdates(time.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        verify(inverse = true) { noCustomerIoClient.sendEvent("someMemberID", any()) }
    }
}
