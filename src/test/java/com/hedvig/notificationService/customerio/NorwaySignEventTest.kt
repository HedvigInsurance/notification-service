package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class NorwaySignEventTest {

    @Test
    fun singleSignAttributeSendsCustomerIOUpdate() {
        val productPricingFacade = mockk<ProductPricingFacade>(relaxed = true)
        val memberServiceImpl = mockk<MemberServiceImpl>(relaxed = true)
        val seCustomerIOClient = mockk<CustomerioClient>(relaxed = true)
        val noCustomerIOClient = mockk<CustomerioClient>(relaxed = true)

        val sut = CustomerioService(
            productPricingFacade, memberServiceImpl,
            Workspace.SWEDEN to seCustomerIOClient,
            Workspace.NORWAY to noCustomerIOClient
        )

        sut.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            )
        )

        sut.sendUpdates()

        val eventDataSlot = slot<Map<String, Any>>()
        verify { noCustomerIOClient.sendEvent("1337", capture(eventDataSlot)) }
        assertThat(eventDataSlot.captured).containsEntry("name", "TmpSignedInsuranceEvent")
    }

    @Test
    fun singleSignAttributeNorwegianMemberDoesNotUpdateCustomerIo() {
        val productPricingFacade = mockk<ProductPricingFacade>(relaxed = true)
        val memberServiceImpl = mockk<MemberServiceImpl>(relaxed = true)
        val seCustomerIOClient = mockk<CustomerioClient>(relaxed = true)
        val noCustomerIOClient = mockk<CustomerioClient>(relaxed = true)

        val sut = CustomerioService(
            productPricingFacade, memberServiceImpl,
            Workspace.SWEDEN to seCustomerIOClient,
            Workspace.NORWAY to noCustomerIOClient
        )

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        sut.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            )
        )

        verify(inverse = true) { noCustomerIOClient.sendEvent("1337", any()) }
    }

    @Test
    fun singleSignAttributeSwedishMemberDoesUpdateCustomerIo() {
        val productPricingFacade = mockk<ProductPricingFacade>(relaxed = true)
        val memberServiceImpl = mockk<MemberServiceImpl>(relaxed = true)
        val seCustomerIOClient = mockk<CustomerioClient>(relaxed = true)
        val noCustomerIOClient = mockk<CustomerioClient>(relaxed = true)

        val sut = CustomerioService(
            productPricingFacade, memberServiceImpl,
            Workspace.SWEDEN to seCustomerIOClient,
            Workspace.NORWAY to noCustomerIOClient
        )

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        sut.updateCustomerAttributes(
            "1337", mapOf(
                "partner_code" to "campaigncode",
                "sign_source" to "RAPIO",
                "sign_date" to LocalDate.now().atStartOfDay(ZoneId.of("Europe/Stockholm")).toEpochSecond(),
                "switcher_company" to null,
                "is_switcher" to false
            )
        )

        verify { seCustomerIOClient.updateCustomer("1337", any()) }
    }
}
