package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.events.CustomerioEventCreatorImpl
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
    lateinit var productPricingFacade: ProductPricingFacade

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
            CustomerioEventCreatorImpl(productPricingFacade),
            mapOf(
                Workspace.NORWAY to noClient,
                Workspace.SWEDEN to seClient
            ),
            productPricingFacade
        )
    }

    @Test
    fun sendContractCreatedEvent() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val slot = slot<Map<String, Any?>>()
        verify { noClient.sendEvent(any(), capture(slot)) }

        assertThat(slot.captured).containsAll(
            "name" to "ContractCreatedEvent"
        )
    }

    @Test
    fun `only send one ContractCreatedEvent after two updates`() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))
        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        val slot = slot<Map<String, Any?>>()
        verify(atMost = 1) { noClient.sendEvent(any(), capture(slot)) }

        assertThat(slot.captured).containsAll(
            "name" to "ContractCreatedEvent"
        )
    }

    @Test
    fun `exception during sending does not update state`() {
        val startTime = Instant.parse("2020-04-23T09:25:13.597224Z")
        repo.save(CustomerioState("someMemberId", null, false, startTime))

        every { noClient.sendEvent(any(), any()) } throws FeignExceptionForTest(500)

        sut.sendUpdates(startTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))

        assertThat(repo.data["someMemberId"]?.contractCreatedAt).isEqualTo(startTime)
    }
}
