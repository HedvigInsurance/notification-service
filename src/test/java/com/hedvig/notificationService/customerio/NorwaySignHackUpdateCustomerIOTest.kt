package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
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

    @MockK
    lateinit var sut: CustomerioService

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        sut = CustomerioService(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to seCustomerioClient,
            Workspace.NORWAY to noCustomerIoClient
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

        sut.sendUpdates(updateTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val eventDataSlot = slot<Map<String, Any>>()
        verify { noCustomerIoClient.sendEvent("1337", capture(eventDataSlot)) }
        Assertions.assertThat(eventDataSlot.captured).containsEntry("name", "TmpSignedInsuranceEvent")
    }

    @Test
    fun sendUpdatesBeforeWindoTimeLengthDoesNotSendCustomerIOUpdate() {

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

        sut.sendUpdates(updateTime.plus(1, ChronoUnit.SECONDS))

        verify(inverse = true) { noCustomerIoClient.sendEvent(any(), any()) }
    }
}
