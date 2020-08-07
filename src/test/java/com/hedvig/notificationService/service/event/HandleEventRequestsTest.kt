package com.hedvig.notificationService.service.event

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.service.request.EventRequestHandler
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HandleEventRequestsTest {

    val eventHandler = mockk<EventHandler>(relaxed = true)

    val mapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())

    val serviceToTest = EventRequestHandler(eventHandler, mapper)

    @Test
    fun `start date update event`() {
        val contractId = "contractId"
        val memberId = "memberId"
        val startDate = "2020-11-02"
        val map = mapOf(
            "name" to "StartDateUpdatedEvent",
            "data" to mapOf(
                "contractId" to contractId,
                "owningMemberId" to memberId,
                "startDate" to startDate
            )
        )

        val jsonNode: JsonNode = mapper.valueToTree(map)

        serviceToTest.onEventRequest(
            "unhandled request", jsonNode
        )

        verify { eventHandler.onStartDateUpdatedEvent(StartDateUpdatedEvent(contractId, memberId, LocalDate.parse(startDate)), any()) }
    }
}