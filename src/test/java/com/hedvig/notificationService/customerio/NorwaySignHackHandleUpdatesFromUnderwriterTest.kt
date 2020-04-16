package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NorwaySignHackHandleUpdatesFromUnderwriterTest {

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
    fun singleSignAttributeNorwegianMemberDoesNotUpdateCustomerIo() {

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        sut.updateCustomerAttributes(
            "1338", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), Instant.parse("2020-04-15T14:53:40.550493Z")
        )

        verify(inverse = true) { noCustomerIoClient.sendEvent("1338", any()) }
    }

    @Test
    fun singleSignAttributeSwedishMemberDoesUpdateCustomerIo() {

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        sut.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            ), Instant.parse("2020-04-15T14:53:40.550493Z")
        )

        verify { seCustomerioClient.updateCustomer("1337", any()) }
    }
}
