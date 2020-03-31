package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CustomerioServiceDeleteCustomerTest {

    val productPricingFacade = mockk<ProductPricingFacade>()
    val memberServiceImpl = mockk<MemberServiceImpl>()

    @Test
    fun deleteCustomerNorway() {
        val sweClient = mockk<CustomerioClient>()
        val noClient = mockk<CustomerioClient>()

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val cut = CustomerioService(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
        )
        cut.deleteCustomer("asdad")
        verify { noClient.deleteCustomer(any()) }
    }
}
