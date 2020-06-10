package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.serviceIntegration.productPricing.FeignExceptionForTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.temporal.ChronoUnit

class SendNorwegianContractCreatedEventTest {

    @MockK
    lateinit var workspaceSelector: WorkspaceSelector

    @MockK
    lateinit var contractLoader: ContractLoader

    val repo = InMemoryCustomerIOStateRepository()

    @MockK(relaxed = true)
    lateinit var noClient: CustomerioClient

    @MockK(relaxed = true)
    lateinit var seClient: CustomerioClient

    lateinit var sut: CustomerioService

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sut = CustomerioService(
            workspaceSelector,
            repo,
            CustomerioEventCreatorImpl(),
            mapOf(
                Workspace.NORWAY to noClient,
                Workspace.SWEDEN to seClient
            ),
            contractLoader,
            true
        )
    }

    @Test
    fun sendContractCreatedEvent() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { contractLoader.getContractInfoForMember(any()) } returns
            listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "IOS",
                    "HEDVIG"
                )
            )

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val slot = slot<Map<String, Any?>>()
        verify { noClient.sendEvent(any(), capture(slot)) }

        assertThat(slot.captured["name"]).isEqualTo("NorwegianContractCreatedEvent")
        assertThat(slot.captured["data"] as Map<String, Any>).contains("is_signed_innbo", true)
    }

    @Test
    fun sendContractCreatedEventToSwedishMember() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.SWEDEN

        every { contractLoader.getContractInfoForMember(any()) } returns
            listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "IOS",
                    "HEDVIG"
                )
            )

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val slot = slot<Map<String, Any?>>()
        verify { seClient.sendEvent(any(), any()) }
    }

    @Test
    fun `only send one ContractCreatedEvent after two updates`() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))
        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { contractLoader.getContractInfoForMember(any()) } returns
            listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "IOS",
                    "HEDVIG"
                )
            )

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))
        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val slot = slot<Map<String, Any?>>()
        verify(atMost = 1) { noClient.sendEvent(any(), capture(slot)) }

        assertThat(slot.captured).containsAll(
            "name" to "NorwegianContractCreatedEvent"
        )
    }

    @Test
    fun `exception during sending does not update state`() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { workspaceSelector.getWorkspaceForMember(any()) } returns Workspace.NORWAY
        every { noClient.sendEvent(any(), any()) } throws FeignExceptionForTest(500)

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        assertThat(repo.data["someMemberId"]?.contractCreatedTriggerAt).isEqualTo(startTime)
    }
}
