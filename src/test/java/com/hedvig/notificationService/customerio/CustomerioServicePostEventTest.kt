package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreator
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class CustomerioServicePostEventTest {

    @Test
    fun `forward test swedish market`() {
        val sweClient = mockk<CustomerioClient>(relaxed = true)
        val noClient = mockk<CustomerioClient>(relaxed = true)
        val workspaceSelector = mockk<WorkspaceSelector>()
        val eventCreator = mockk<CustomerioEventCreator>()

        val sut = CustomerioService(
            workspaceSelector,
            InMemoryCustomerIOStateRepository(),
            eventCreator,
            mapOf(
                Workspace.SWEDEN to sweClient,
                Workspace.NORWAY to noClient
            )
        )

        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.SWEDEN

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { sweClient.sendEvent("8080", mapOf("someKey" to 42)) }
    }

    @Test
    fun `forward test norwegian market`() {
        val sweClient = mockk<CustomerioClient>(relaxed = true)
        val noClient = mockk<CustomerioClient>(relaxed = true)
        val workspaceSelector = mockk<WorkspaceSelector>()
        val eventCreator = mockk<CustomerioEventCreator>()

        val sut = CustomerioService(
            workspaceSelector,
            InMemoryCustomerIOStateRepository(),
            eventCreator,

            mapOf(
                Workspace.SWEDEN to sweClient,
                Workspace.NORWAY to noClient
            )
        )

        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.NORWAY

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { noClient.sendEvent("8080", mapOf("someKey" to 42)) }
    }
}
