package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.customerio.Customerio
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class ConfigurableNorwaySignHackTest {

    @Test
    fun `do not update customerio state table`() {

        val workspaceSelector = mockk<WorkspaceSelector>()
        val stateRepository = InMemoryCustomerIOStateRepository()
        val eventCreator = mockk<CustomerioEventCreator>()
        val productPricingFacade = mockk<ProductPricingFacade>()
        val clients = mapOf(
            Workspace.SWEDEN to mockk<Customerio>(),
            Workspace.NORWAY to mockk<Customerio>()
        )

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY

        val cut =
            CustomerioService(workspaceSelector, stateRepository, eventCreator, clients, productPricingFacade, false)

        cut.updateCustomerAttributes("someId", makeSignFromUnderwriterMap())

        assertThat(stateRepository.data["someId"]?.underwriterFirstSignAttributesUpdate).isNull()
    }
}
