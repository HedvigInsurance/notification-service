package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.service.event.EventHandler
import com.hedvig.notificationService.service.event.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class OnContractRenewalQuuedEventTest {

    private val customerioService = mockk<CustomerioService>(relaxed = true)
    private val memberService = mockk<MemberServiceImpl>()
    private val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    private val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)
    private val repo: InMemoryCustomerIOStateRepository = InMemoryCustomerIOStateRepository()

    val sut = EventHandler(
        repo = repo,
        firebaseNotificationService = firebaseNotificationService,
        customerioService = customerioService,
        memberService = memberService,
        scheduler = mockk(),
        handledRequestRepository = handledRequestRepository
    )

    @Test
    fun `renewal queued test`() {

        val requestId = "unhandled request"
        sut.onContractRenewalQueuedHandleRequest(
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

        sut.onContractRenewalQueuedHandleRequest(
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
