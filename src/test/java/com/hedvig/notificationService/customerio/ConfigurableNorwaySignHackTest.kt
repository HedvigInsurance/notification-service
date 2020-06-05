package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.customerio.Customerio
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import java.time.LocalDate

class ConfigurableNorwaySignHackTest {

    @Test
    fun `do not update customerio state table`() {

        val workspaceSelector = mockk<WorkspaceSelector>()
        val stateRepository = InMemoryCustomerIOStateRepository()
        val eventCreator = mockk<CustomerioEventCreator>()
        val productPricingFacade = mockk<ContractLoader>()
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

    @Test
    fun `do not update on contract created event`() {
        val stateRepository = InMemoryCustomerIOStateRepository()
        val configuration = ConfigurationProperties()
        configuration.useNorwayHack = true

        val sut = EventHandler(stateRepository, configuration)

        sut.onContractCreatedEvent(ContractCreatedEvent("contractID", "memberID", null))

        assertThat(stateRepository.data["memberID"]?.contractCreatedTriggerAt).isNull()
    }

    @Test
    fun `do not update on start date updated event`() {
        val stateRepository = InMemoryCustomerIOStateRepository()
        val configuration = ConfigurationProperties()
        configuration.useNorwayHack = true

        val sut = EventHandler(stateRepository, configuration)

        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("contractID", "memberID", LocalDate.parse("2020-03-01")))

        assertThat(stateRepository.data["memberID"]?.startDateUpdatedTriggerAt).isNull()
    }
}
