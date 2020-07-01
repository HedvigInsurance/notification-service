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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/_/events")
class EventController(val eventHandler: EventHandler) {

    @PostMapping("/contractCreated")
    fun contractCreated(@RequestBody event: ContractCreatedEvent): ResponseEntity<Any> {
        eventHandler.onContractCreatedEvent(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/startDateUpdated")
    fun startDateUpdated(@RequestBody event: StartDateUpdatedEvent): ResponseEntity<Any> {
        eventHandler.onStartDateUpdatedEvent(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("contractRenewalQueued")
    fun contractRenewalQueued(@RequestBody event: ContractRenewalQueuedEvent): ResponseEntity<Any> {
        eventHandler.onContractRenewalQueued(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("{memberId}/chargeFailed")
    fun chargeFailed(@PathVariable memberId: String, @Valid @RequestBody event: ChargeFailedEvent): ResponseEntity<Any> {
        eventHandler.onFailedChargeEvent(memberId, event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("{memberId}/quoteCreated")
    fun firstQuoteCreated(@PathVariable memberId: String, @Valid @RequestBody  event: QuoteCreatedEvent): ResponseEntity<Any> {
        eventHandler.onQuoteCreated(memberId, event)
        return ResponseEntity.accepted().build()
    }
}
