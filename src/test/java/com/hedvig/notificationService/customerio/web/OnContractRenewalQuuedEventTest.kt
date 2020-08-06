package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.time.LocalDate

class OnContractRenewalQuuedEventTest {

    private val configurationProperties = ConfigurationProperties()
    private val customerioService = mockk<CustomerioService>(relaxed = true)
    private val memberService = mockk<MemberServiceImpl>()
    private val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    private val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)
    private val repo: InMemoryCustomerIOStateRepository = InMemoryCustomerIOStateRepository()
    var sut: EventHandler

    init {
        sut = EventHandler(
            repo = repo,
            configuration = configurationProperties,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService,
            handledRequestRepository = handledRequestRepository
        )
    }

    @Test
    fun `renewal queued test`() {

        val requestId = "unhandled request"
        sut.onContractRenewalQueued(
            ContractRenewalQueuedEvent(
                "contractOne",
                "contractType",
                "member",
                LocalDate.of(1989, 2, 17)
            ),
            requestId = requestId
        )

        val slot = slot<Map<String, Any>>()
        verify { customerioService.sendEvent("member", capture(slot)) }
        assertThat(slot.captured["name"]).isEqualTo("ContractRenewalQueuedEvent")
        verify { handledRequestRepository.storeHandledRequest(requestId) }
    }

    @Test
    fun `handled request dose nothing`() {
        val requestId = "handled request id"
        every { handledRequestRepository.isRequestHandled(requestId) } returns true

        sut.onContractRenewalQueued(
            ContractRenewalQueuedEvent(
                "contractOne",
                "contractType",
                "member",
                LocalDate.of(1989, 2, 17)
            ),
            requestId = requestId
        )

        verify(exactly = 0) { customerioService.sendEvent(any(), any()) }
    }
}
