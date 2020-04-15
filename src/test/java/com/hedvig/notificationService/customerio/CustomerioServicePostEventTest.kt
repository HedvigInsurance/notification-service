package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
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

        val sut = CustomerioService(
            workspaceSelector,
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
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

        val sut = CustomerioService(
            workspaceSelector,
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
        )

        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.NORWAY

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { noClient.sendEvent("8080", mapOf("someKey" to 42)) }
    }
}
