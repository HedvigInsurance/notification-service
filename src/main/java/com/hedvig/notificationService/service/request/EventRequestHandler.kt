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
    private val handledRequestRepository: HandledRequestRepository
) {

    fun onEventRequest(
        requestId: String,
        event: RequestEvent
    ) {
        if (handledRequestRepository.isRequestHandled(requestId)) {
            return
        }
        when (event) {
            is ChargeFailedEvent -> eventHandler.onFailedChargeEvent(event)
            is ContractCreatedEvent -> eventHandler.onContractCreatedEvent(event)
            is ContractRenewalQueuedEvent -> eventHandler.onContractRenewalQueued(event)
            is QuoteCreatedEvent -> eventHandler.onQuoteCreated(event)
            is StartDateUpdatedEvent -> eventHandler.onStartDateUpdatedEvent(event)
        }
        handledRequestRepository.storeHandledRequest(requestId)
    }
}

abstract class ParseEventException(message: String) : Throwable(message)

class NoNameOnEventException(jsonNode: JsonNode) : ParseEventException("No name on event [$jsonNode]")
class NoDataOnEventException(jsonNode: JsonNode) : ParseEventException("No data on event [$jsonNode]")
