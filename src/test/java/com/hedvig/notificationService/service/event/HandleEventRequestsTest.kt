package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.service.request.EventRequestHandler
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HandleEventRequestsTest {

    val eventHandler = mockk<EventHandler>(relaxed = true)
    val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)

    val serviceToTest = EventRequestHandler(eventHandler, handledRequestRepository)

    @Test
    fun `do nothing on handled request`() {
        val contractId = "contractId"
        val memberId = "memberId"
        val startDate = LocalDate.parse("2020-11-02")

        val requestId = "handled request"
        every { handledRequestRepository.isRequestHandled(requestId) } returns true
        serviceToTest.onEventRequest(
            requestId, StartDateUpdatedEvent(contractId, memberId, startDate)
        )

        verify(exactly = 0) { eventHandler.onStartDateUpdatedEvent(any(), any()) }
        verify(exactly = 0) { handledRequestRepository.storeHandledRequest(requestId) }
    }

    @Test
    fun `start date update event`() {
        val contractId = "contractId"
        val memberId = "memberId"
        val startDate = LocalDate.now()

        val requestId = "unhandled request"
        serviceToTest.onEventRequest(
            requestId, StartDateUpdatedEvent(contractId, memberId, startDate)
        )

        verify {
            eventHandler.onStartDateUpdatedEvent(
                StartDateUpdatedEvent(
                    contractId,
                    memberId,
                    LocalDate.parse(startDate)
                ), any()
            )
        }
        verify { handledRequestRepository.storeHandledRequest(requestId) }
    }
}
