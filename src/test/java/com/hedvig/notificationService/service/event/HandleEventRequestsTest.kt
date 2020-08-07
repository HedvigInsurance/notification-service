package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.service.request.EventRequestHandler
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HandleEventRequestsTest {

    val eventHandler = mockk<EventHandler>(relaxed = true)

    val serviceToTest = EventRequestHandler(eventHandler)

    @Test
    fun `start date update event`() {
        val contractId = "contractId"
        val memberId = "contractId"
        val startDate = LocalDate.now()

        serviceToTest.onEventRequest(
            "unhandled request",
            mapOf(
                "name" to "StartDateUpdatedEvent",
                "data" to mapOf(
                    "contractId" to contractId,
                    "memberId" to memberId,
                    "startDate" to startDate
                )
            )
        )

        verify { eventHandler.onStartDateUpdatedEvent(StartDateUpdatedEvent(contractId, memberId, startDate), any()) }
    }
}