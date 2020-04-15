package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class NorwaySignEventTest {

    @Test
    fun singleSignAttributeSendsCustomerIOUpdate() {
        val productPricingFacade = mockk<ProductPricingFacade>(relaxed = true)
        val memberServiceImpl = mockk<MemberServiceImpl>()
        val seCustomerIOClient = mockk<CustomerioClient>()
        val noCustomerIOClient = mockk<CustomerioClient>()

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

        verify { noCustomerIOClient.updateCustomer("1337", mapOf()) }
    }
}
