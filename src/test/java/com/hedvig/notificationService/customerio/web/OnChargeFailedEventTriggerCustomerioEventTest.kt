package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class OnChargeFailedEventTriggerCustomerioEventTest {

    @Test
    internal fun `on unhandled failed charge event post to customer io`() {
        val customerioService = mockk<CustomerioService>(relaxed = true)
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
        val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)

        val repo = InMemoryCustomerIOStateRepository()
        val sut = EventHandler(
            repo = repo,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService,
            scheduler = mockk(),
            handledRequestRepository = handledRequestRepository
        )
        val requestId = "unhandled request id"

        sut.onFailedChargeEventHandleRequest(
            ChargeFailedEvent(
                null,
                1,
                2,
                ChargeFailedReason.INSUFFICIENT_FUNDS,
                "1227"
            ),
            requestId
        )

        val slot = slot<Map<String, Any>>()
        verify { customerioService.sendEvent("1227", capture(slot)) }
        verify { handledRequestRepository.storeHandledRequest(requestId) }
        assertThat(slot.captured["name"]).isEqualTo("ChargeFailedEvent")
    }

    @Test
    internal fun `on handled failed charge event dose nothing`() {
        val customerioService = mockk<CustomerioService>(relaxed = true)
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
        val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)

        val repo = InMemoryCustomerIOStateRepository()
        val sut = EventHandler(
            repo = repo,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService,
            scheduler = mockk(),
            handledRequestRepository = handledRequestRepository
        )
        val requestId = "handled request id"
        every { handledRequestRepository.isRequestHandled(requestId) } returns true

        sut.onFailedChargeEventHandleRequest(
            ChargeFailedEvent(
                null,
                1,
                2,
                ChargeFailedReason.INSUFFICIENT_FUNDS,
                "1227"
                ),
            requestId
        )

        verify(exactly = 0) { customerioService.sendEvent(any(), any()) }
        verify(exactly = 0) { handledRequestRepository.storeHandledRequest(requestId) }
    }
}
