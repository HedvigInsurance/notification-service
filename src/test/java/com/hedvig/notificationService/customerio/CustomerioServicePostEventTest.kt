package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.builders.a
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import okhttp3.internal.toHexString
import org.junit.Test
import org.quartz.JobDetail
import java.util.Date

class CustomerioServicePostEventTest {

    val sweClient = mockk<CustomerioClient>(relaxed = true)
    val noClient = mockk<CustomerioClient>(relaxed = true)
    val workspaceSelector = mockk<WorkspaceSelector>()

    val sut = CustomerioService(
        workspaceSelector,
        InMemoryCustomerIOStateRepository(),
        mapOf(
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
        )
    )

    @Test
    fun `forward test swedish market`() {
        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.SWEDEN

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { sweClient.sendEvent("8080", mapOf("someKey" to 42)) }
    }

    @Test
    fun `forward test norwegian market`() {
        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.NORWAY

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { noClient.sendEvent("8080", mapOf("someKey" to 42)) }
    }

    @Test
    fun `include hash on send event`() {
        val memberId = "1234"
        every { workspaceSelector.getWorkspaceForMember(memberId) } returns Workspace.SWEDEN

        val event = mapOf(
            "name" to "SomeCoolEvent",
            "data" to mapOf(
                "attr1" to "4312",
                "attr2" to 231,
                "memberId" to memberId
            )
        )


        sut.sendEvent(memberId, event)

        val expectedMap = event.toMutableMap()
        expectedMap["hash"] = "da0bfe4d"

        verify { sweClient.sendEvent(memberId, expectedMap) }
    }
}