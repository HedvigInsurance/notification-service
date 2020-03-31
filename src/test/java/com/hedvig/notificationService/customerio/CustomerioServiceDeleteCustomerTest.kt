package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CustomerioServiceDeleteCustomerTest {

    private val productPricingFacade = mockk<ProductPricingFacade>()
    private val memberServiceImpl = mockk<MemberServiceImpl>()

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

    @Test
    fun deleteCustomerSweden() {
        val sweClient = mockk<CustomerioClient>()
        val noClient = mockk<CustomerioClient>()

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        val cut = CustomerioService(
            productPricingFacade,
            memberServiceImpl,
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
        )
        cut.deleteCustomer("asdad")
        verify { sweClient.deleteCustomer(any()) }
    }
}
