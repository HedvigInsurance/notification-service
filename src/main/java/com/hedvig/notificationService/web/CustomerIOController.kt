package com.hedvig.notificationService.web

import com.hedvig.notificationService.serviceIntegration.customerIO.CustomerIOService
import com.hedvig.notificationService.web.dto.CustomerIOEventDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/_/customerIO")
class CustomerIOController @Autowired constructor(
    private val customerIOService: CustomerIOService
) {
    @PostMapping("/event/{memberId}")
    fun postEvent(
        @PathVariable memberId: String,
        @RequestBody customerIOEvent: CustomerIOEventDto
    ) {
        customerIOService.postEvent(memberId, customerIOEvent)
    }

    @PostMapping("/traits/{memberId}")
    fun putAttribute(
        @PathVariable memberId: String,
        @RequestBody traits: Map<String, Any>
    ) {
        customerIOService.putTraits(memberId, traits)
    }
}