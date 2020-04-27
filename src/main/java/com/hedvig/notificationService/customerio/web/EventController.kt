package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/events")
class EventController(val customerioService: CustomerioService) {

    @PostMapping("/contractCreated")
    fun contractCreated(@RequestBody event: ContractCreatedEvent): ResponseEntity<Any> {
        customerioService.contractCreatedEvent(event)
        return ResponseEntity.accepted().build()
    }

    @PostMapping("/startDateUpdated")
    fun startDateUpdated(@RequestBody event: StartDateUpdatedEvent): ResponseEntity<Any> {
        customerioService.startDateUpdatedEvent(event)
        return ResponseEntity.accepted().build()
    }
}
