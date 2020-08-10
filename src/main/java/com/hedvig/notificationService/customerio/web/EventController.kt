package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.EventRequest
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.service.request.EventRequestHandler
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/_/")
class EventController(
    private val eventHandler: EventHandler,
    private val eventRequestHandler: EventRequestHandler
) {

    @PostMapping("/event")
    fun event(
        @RequestHeader(value = "Request-Id") requestId: String,
        @RequestBody event: EventRequest
    ): ResponseEntity<Any> {
        eventRequestHandler.onEventRequest(requestId, event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/events/contractCreated")
    fun contractCreated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: ContractCreatedEvent
    ): ResponseEntity<Any> {
        eventHandler.onContractCreatedEventHandleRequest(
            contractCreatedEvent = event,
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/events/startDateUpdated")
    fun startDateUpdated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: StartDateUpdatedEvent
    ): ResponseEntity<Any> {
        eventHandler.onStartDateUpdatedEventHandleRequest(
            event = event,
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/events/contractRenewalQueued")
    fun contractRenewalQueued(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: ContractRenewalQueuedEvent
    ): ResponseEntity<Any> {
        eventHandler.onContractRenewalQueuedHandleRequest(
            event = event,
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/events/{memberId}/chargeFailed")
    fun chargeFailed(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @PathVariable memberId: String,
        @Valid @RequestBody eventDto: ChargeFailedEventDto
    ): ResponseEntity<Any> {
        val event = ChargeFailedEvent(
            terminationDate = eventDto.terminationDate,
            numberOfFailedCharges = eventDto.numberOfFailedCharges,
            chargesLeftBeforeTermination = eventDto.chargesLeftBeforeTermination,
            chargeFailedReason = eventDto.chargeFailedReason,
            memberId = memberId
        )
        eventHandler.onFailedChargeEventHandleRequest(event, requestId)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/events/quoteCreated")
    fun quoteCreated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: QuoteCreatedEvent
    ): ResponseEntity<Any> {
        eventHandler.onQuoteCreatedHandleRequest(
            event = event,
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }
}
