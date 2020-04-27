package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("/_/events")
class EventController(val customerioService: CustomerioService) {

    @PostMapping("/contractCreated")
    fun contractCreated(@RequestBody event: ContractCreatedEvent): ResponseEntity<Any> {
        customerioService.contractCreatedEvent(event)
        return ResponseEntity.accepted().build()
    }
}

data class ContractCreatedEvent(
    val contractId: String,
    val owningMemberId: String
)
