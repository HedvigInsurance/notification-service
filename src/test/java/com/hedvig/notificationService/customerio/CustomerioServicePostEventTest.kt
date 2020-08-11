package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

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

        verify { sweClient.sendEvent("8080", mapOf("someKey" to 42, "hash" to "87059ce1")) }
    }

    @Test
    fun `forward test norwegian market`() {
        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.NORWAY

        sut.sendEvent("8080", mapOf("someKey" to 42))

        verify { noClient.sendEvent("8080", mapOf("someKey" to 42, "hash" to "87059ce1")) }
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

        val expectedMap =  createExpectedMap(event, "da0bfe4d")

        verify { sweClient.sendEvent(memberId, expectedMap) }
    }

    @Test
    fun `hash is unique on send event`() {
        val memberOne = "1234"
        val memberTwo = "4321"
        val memberThree = "2234"
        every { workspaceSelector.getWorkspaceForMember(memberOne) } returns Workspace.SWEDEN
        every { workspaceSelector.getWorkspaceForMember(memberTwo) } returns Workspace.SWEDEN
        every { workspaceSelector.getWorkspaceForMember(memberThree) } returns Workspace.SWEDEN

        val eventMemberOne = mapOf("name" to "SomeCoolEvent", "data" to mapOf("attr1" to "4312", "attr2" to 231))
        val eventMemberTwo = mapOf("name" to "OtherCoolEvent", "data" to mapOf("attr1" to 13, "attr2" to 231))
        val eventMemberThree = mapOf("name" to "SomeCoolEvent", "data" to mapOf("attr1" to "different value", "attr2" to 231))

        sut.sendEvent(memberOne, eventMemberOne)
        sut.sendEvent(memberTwo, eventMemberTwo)
        sut.sendEvent(memberThree, eventMemberThree)

        val expectedMapOne = createExpectedMap(eventMemberOne, "c77b42")
        verify { sweClient.sendEvent(memberOne, expectedMapOne) }

        val expectedMapTwo = createExpectedMap(eventMemberTwo, "fddf54a3")
        verify { sweClient.sendEvent(memberTwo, expectedMapTwo) }

        val expectedMapThree = createExpectedMap(eventMemberThree, "ace32530")
        verify { sweClient.sendEvent(memberThree, expectedMapThree) }
    }

    fun createExpectedMap(map: Map<String, Any>, hash: String) =
        map.toMutableMap().also { it["hash"] = hash }
}