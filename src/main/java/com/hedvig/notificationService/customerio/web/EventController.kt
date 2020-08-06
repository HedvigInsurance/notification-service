package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/_/events")
class EventController(
    private val eventHandler: EventHandler
) {
    @PostMapping("/contractCreated")
    fun contractCreated(@RequestBody event: ContractCreatedEvent): ResponseEntity<Any> {
        eventHandler.onContractCreatedEvent(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/startDateUpdated")
    fun startDateUpdated(@RequestHeader(value = "Request-Id", required = false) requestId: String?, @RequestBody event: StartDateUpdatedEvent): ResponseEntity<Any> {
        eventHandler.onStartDateUpdatedEvent(
            event = event,
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/contractRenewalQueued")
    fun contractRenewalQueued(@RequestBody event: ContractRenewalQueuedEvent): ResponseEntity<Any> {
        eventHandler.onContractRenewalQueued(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/{memberId}/chargeFailed")
    fun chargeFailed(@RequestHeader(value = "Request-Id", required = false) requestId: String?, @PathVariable memberId: String, @Valid @RequestBody event: ChargeFailedEvent): ResponseEntity<Any> {
        eventHandler.onFailedChargeEvent(requestId, memberId, event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/quoteCreated")
    fun quoteCreated(@RequestBody event: QuoteCreatedEvent): ResponseEntity<Any> {
        eventHandler.onQuoteCreated(event)
        return ResponseEntity.accepted().build()
    }
}
