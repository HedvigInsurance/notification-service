package com.hedvig.notificationService.service.request

import com.hedvig.notificationService.service.event.ChargeFailedEvent
import com.hedvig.notificationService.service.event.ClaimClosedEvent
import com.hedvig.notificationService.service.event.ContractCreatedEvent
import com.hedvig.notificationService.service.event.ContractRenewalQueuedEvent
import com.hedvig.notificationService.service.event.ContractTerminatedEvent
import com.hedvig.notificationService.service.event.EventHandler
import com.hedvig.notificationService.service.event.EventRequest
import com.hedvig.notificationService.service.event.QuoteCreatedEvent
import com.hedvig.notificationService.service.event.StartDateUpdatedEvent
import com.hedvig.notificationService.service.event.PhoneNumberUpdatedEvent
import org.springframework.stereotype.Service

@Service
class EventRequestHandler(
    private val eventHandler: EventHandler,
    private val handledRequestRepository: HandledRequestRepository
) {

    fun onEventRequest(
        requestId: String,
        event: EventRequest
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
            is ContractTerminatedEvent -> eventHandler.onContractTerminatedEvent(event)
            is PhoneNumberUpdatedEvent -> eventHandler.onPhoneNumberUpdatedEvent(event)
            is ClaimClosedEvent ->  eventHandler.onClaimClosedEvent(event)
        }
        handledRequestRepository.storeHandledRequest(requestId)
    }
}
