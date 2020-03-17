package com.hedvig.notificationService.web

import com.fasterxml.jackson.databind.JsonNode
import com.hedvig.customerio.CustomerioClient
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/_/customerio")
class CustomerIOProxyController(private val customerioClient: CustomerioClient) {

    @PostMapping("{memberId}")
    fun post(@PathVariable memberId: String, @RequestBody body: JsonNode): ResponseEntity<Any> {
        customerioClient.updateCustomer(memberId, mapOf())

        return ResponseEntity.accepted().build()
    }
}
