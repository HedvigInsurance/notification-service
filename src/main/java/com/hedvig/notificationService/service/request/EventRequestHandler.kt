package com.hedvig.notificationService.service.request

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.RequestEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import org.springframework.stereotype.Service

@Service
class EventRequestHandler(
    private val eventHandler: EventHandler,
    private val objectMapper: ObjectMapper,
    private val handledRequestRepository: HandledRequestRepository
) {

    fun onEventRequest(
        requestId: String,
        eventJson: JsonNode
    ) {
        if (handledRequestRepository.isRequestHandled(requestId)) {
            return
        }
        when (val event = getRequestEvent(eventJson)) {
            is ChargeFailedEvent -> eventHandler.onFailedChargeEvent(event)
            is ContractCreatedEvent -> eventHandler.onContractCreatedEvent(event)
            is ContractRenewalQueuedEvent -> eventHandler.onContractRenewalQueued(event)
            is QuoteCreatedEvent -> eventHandler.onQuoteCreated(event)
            is StartDateUpdatedEvent -> eventHandler.onStartDateUpdatedEvent(event)
        }
        handledRequestRepository.storeHandledRequest(requestId)
    }

    private fun getRequestEvent(eventJson: JsonNode): RequestEvent {
        val name = eventJson["name"]?.asText()
            ?: throw NoNameOnEventException(eventJson)
        val data = eventJson["data"]
            ?: throw NoDataOnEventException(eventJson)
        return objectMapper.treeToValue(
            data,
            Class.forName("$EVENT_PACKAGE$name")
        ) as RequestEvent
    }

    companion object {
        private val EVENT_PACKAGE = "com.hedvig.notificationService.customerio.dto."
    }
}

abstract class ParseEventException(message: String) : Throwable(message)

class NoNameOnEventException(jsonNode: JsonNode) : ParseEventException("No name on event [$jsonNode]")
class NoDataOnEventException(jsonNode: JsonNode) : ParseEventException("No data on event [$jsonNode]")
