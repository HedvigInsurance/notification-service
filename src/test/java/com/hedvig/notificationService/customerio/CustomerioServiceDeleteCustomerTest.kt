package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CustomerioServiceDeleteCustomerTest {

    private val productPricingFacade = mockk<ContractLoader>()
    private val memberServiceImpl = mockk<MemberServiceImpl>()
    private val repository =
        InMemoryCustomerIOStateRepository()

    @Test
    fun deleteCustomerNorway() {
        val sweClient = mockk<CustomerioClient>(relaxed = true)
        val noClient = mockk<CustomerioClient>(relaxed = true)
        val dkClient = mockk<CustomerioClient>(relaxed = true)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val cut = CustomerioService(
            WorkspaceSelector(productPricingFacade, memberServiceImpl),
            repository,
            mapOf(
                Workspace.SWEDEN to sweClient,
                Workspace.NORWAY to noClient,
                Workspace.DENMARK to dkClient
            ),
            mockk(),
            mockk()
        )
        cut.deleteCustomer("asdad")
        verify { noClient.deleteCustomer(any()) }
    }

    @Test
    fun deleteCustomerSweden() {
        val sweClient = mockk<CustomerioClient>(relaxed = true)
        val noClient = mockk<CustomerioClient>(relaxed = true)
        val dkClient = mockk<CustomerioClient>(relaxed = true)

        every { productPricingFacade.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        val cut = CustomerioService(
            WorkspaceSelector(productPricingFacade, memberServiceImpl),
            repository,
            mapOf(
                Workspace.SWEDEN to sweClient,
                Workspace.NORWAY to noClient,
                Workspace.DENMARK to dkClient
            ),
            mockk(),
            mockk()
        )
        cut.deleteCustomer("asdad")
        verify { sweClient.deleteCustomer(any()) }
    }
}
