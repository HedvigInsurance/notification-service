package com.hedvig.notificationService.customerio.web

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.WorkspaceNotFound
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.server.ResponseStatusException

@Controller
@RequestMapping("/_/customerio")
class CustomerioController(
    private val customerioCustomerioService: CustomerioService,
    private val objectMapper: ObjectMapper
) {

    val log = LoggerFactory.getLogger(CustomerioController::class.java)

    @PostMapping("{memberId}")
    fun post(@PathVariable memberId: String, @RequestBody body: JsonNode): ResponseEntity<Any> {

        try {
            customerioCustomerioService.updateCustomerAttributes(
                memberId,
                objectMapper.convertValue(body)
            )
        } catch (ex: WorkspaceNotFound) {
            log.error("Exception from router: ${ex.message}", ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find workspace for member", ex)
        }

        return ResponseEntity.accepted().build()
    }

    @DeleteMapping("{memberId}")
    fun delete(@PathVariable memberId: String): ResponseEntity<Any> {
        try {
            customerioCustomerioService.deleteCustomer(memberId)
        } catch (ex: WorkspaceNotFound) {
            log.error("Exception from router: ${ex.message}", ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not find workspace for member", ex)
        }
        return ResponseEntity.accepted().build()
    }

    @PostMapping("{memberId}/events")
    fun postEvent(@PathVariable memberId: String, @RequestBody body: JsonNode): ResponseEntity<Any> {
        customerioCustomerioService.sendEvent(memberId, objectMapper.convertValue(body))
        return ResponseEntity.accepted().build()
    }
}
