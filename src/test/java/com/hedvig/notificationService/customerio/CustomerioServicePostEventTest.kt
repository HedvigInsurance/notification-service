package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.state.IdempotenceHashRepository
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.time.LocalDate

class CustomerioServicePostEventTest {

    val sweClient = mockk<CustomerioClient>(relaxed = true)
    val noClient = mockk<CustomerioClient>(relaxed = true)
    val workspaceSelector = mockk<WorkspaceSelector>()
    val eventHashRepository = mockk<IdempotenceHashRepository>(relaxed = true)

    val sut = CustomerioService(
        workspaceSelector,
        InMemoryCustomerIOStateRepository(),
        mapOf(
            Workspace.SWEDEN to sweClient,
            Workspace.NORWAY to noClient
        ),
        eventHashRepository,
        mockk()
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

        val expectedHash = "da0bfe4d"
        val expectedMap = createExpectedMap(event, expectedHash)

        verify { sweClient.sendEvent(memberId, expectedMap) }
        verify { eventHashRepository.save(memberId, expectedHash) }
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
        val eventMemberThree =
            mapOf("name" to "SomeCoolEvent", "data" to mapOf("attr1" to "different value", "attr2" to 231))

        sut.sendEvent(memberOne, eventMemberOne)
        sut.sendEvent(memberTwo, eventMemberTwo)
        sut.sendEvent(memberThree, eventMemberThree)

        val hashOne = "c77b42"
        val expectedMapOne = createExpectedMap(eventMemberOne, hashOne)
        verify { sweClient.sendEvent(memberOne, expectedMapOne) }
        verify { eventHashRepository.save(memberOne, hashOne) }

        val hashTwo = "fddf54a3"
        val expectedMapTwo = createExpectedMap(eventMemberTwo, hashTwo)
        verify { sweClient.sendEvent(memberTwo, expectedMapTwo) }
        verify { eventHashRepository.save(memberTwo, hashTwo) }

        val hashThree = "ace32530"
        val expectedMapThree = createExpectedMap(eventMemberThree, hashThree)
        verify { sweClient.sendEvent(memberThree, expectedMapThree) }
        verify { eventHashRepository.save(memberThree, hashThree) }
    }

    @Test
    fun `hash is the same data changed order on send event`() {
        val member = "1234"
        every { workspaceSelector.getWorkspaceForMember(member) } returns Workspace.SWEDEN

        val event =
            mapOf("name" to "SomeCoolEvent", "data" to mapOf("attr1" to "123", "attr2" to "4312", "attr3" to 231))
        val sameEventDifferentOrder =
            mapOf("data" to mapOf("attr3" to 231, "attr2" to "4312", "attr1" to "123"), "name" to "SomeCoolEvent")

        sut.sendEvent(member, event)
        sut.sendEvent(member, sameEventDifferentOrder)

        val expectedHash = "6598188"
        val expectedMap = createExpectedMap(event, expectedHash)
        verify(exactly = 2) { sweClient.sendEvent(member, expectedMap) }
        verify(exactly = 2) { eventHashRepository.save(member, expectedHash) }
    }

    @Test
    fun `local date is replaced on send event`() {
        every { workspaceSelector.getWorkspaceForMember("8080") } returns Workspace.SWEDEN

        sut.sendEvent("8080", mapOf("someKey" to LocalDate.of(2020, 8, 17)))

        verify { sweClient.sendEvent("8080", mapOf("someKey" to 1597615200L)) }
    }

    private fun createExpectedMap(map: Map<String, Any>, hash: String) =
        map.toMutableMap().also {
            @Suppress("UNCHECKED_CAST")
            (it["data"] as MutableMap<String, Any>)["hash"] = hash
        }
}
