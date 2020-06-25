package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import org.springframework.http.ResponseEntity
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

    @PostMapping("/chargeFailed")
    fun chargeFailed(@Valid @RequestBody event: ChargeFailedEvent): ResponseEntity<Any> {
        eventHandler.onFailedChargeEvent(event)
        return ResponseEntity.accepted().build()
    }
}
