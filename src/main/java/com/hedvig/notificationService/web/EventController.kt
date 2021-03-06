package com.hedvig.notificationService.web

import com.hedvig.notificationService.service.event.EventHandler
import com.hedvig.notificationService.service.event.ChargeFailedEvent
import com.hedvig.notificationService.service.event.ContractCreatedEvent
import com.hedvig.notificationService.service.event.ContractRenewalQueuedEvent
import com.hedvig.notificationService.service.event.QuoteCreatedEvent
import com.hedvig.notificationService.service.event.EventRequest
import com.hedvig.notificationService.service.event.StartDateUpdatedEvent
import com.hedvig.notificationService.web.dto.ChargeFailedEventDto
import com.hedvig.notificationService.service.request.EventRequestHandler
import com.hedvig.notificationService.web.dto.ContractCreatedEventDto
import com.hedvig.notificationService.web.dto.ContractRenewalQueuedEventDto
import com.hedvig.notificationService.web.dto.QuoteCreatedEventDto
import com.hedvig.notificationService.web.dto.StartDateUpdatedEventDto
import org.slf4j.LoggerFactory
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
        logger.info("[Handling event ${event.javaClass.simpleName}] - RequestId: $requestId Event: $event")
        eventRequestHandler.onEventRequest(requestId, event)
        return ResponseEntity.accepted().build()
    }

    @Deprecated(message = "use /event")
    @PostMapping("/events/contractCreated")
    fun contractCreated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: ContractCreatedEventDto
    ): ResponseEntity<Any> {
        logger.warn("A deprecated event, /events/contractCreated, is being used.")
        eventHandler.onContractCreatedEventHandleRequest(
            contractCreatedEvent = ContractCreatedEvent(
                contractId = event.contractId,
                owningMemberId = event.owningMemberId,
                startDate = event.startDate,
                signSource = event.signSource
            ),
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @Deprecated(message = "use /event")
    @PostMapping("/events/startDateUpdated")
    fun startDateUpdated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: StartDateUpdatedEventDto
    ): ResponseEntity<Any> {
        logger.warn("A deprecated event, /events/startDateUpdated, is being used.")
        eventHandler.onStartDateUpdatedEventHandleRequest(
            event = StartDateUpdatedEvent(
                contractId = event.contractId,
                owningMemberId = event.owningMemberId,
                startDate = event.startDate,
                carrierWillBeSwitched = event.carrierWillBeSwitched,
                currentCarrier = event.currentCarrier,
                carrierOnStartDate = event.carrierOnStartDate
            ),
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @Deprecated(message = "use /event")
    @PostMapping("/events/contractRenewalQueued")
    fun contractRenewalQueued(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: ContractRenewalQueuedEventDto
    ): ResponseEntity<Any> {
        logger.warn("A deprecated event, /events/contractRenewalQueue, is being used.")
        eventHandler.onContractRenewalQueuedHandleRequest(
            event = ContractRenewalQueuedEvent(
                contractId = event.contractId,
                contractType = event.contractType,
                memberId = event.memberId,
                renewalQueuedAt = event.renewalQueuedAt,
                carrierWillBeSwitched = event.carrierWillBeSwitched,
                currentCarrier = event.currentCarrier,
                carrierOnRenewal = event.carrierOnRenewal
            ),
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    @Deprecated(message = "use /event")
    @PostMapping("/events/{memberId}/chargeFailed")
    fun chargeFailed(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @PathVariable memberId: String,
        @Valid @RequestBody eventDto: ChargeFailedEventDto
    ): ResponseEntity<Any> {
        logger.warn("A deprecated event, /events/{memberId}/chargeFailed, is being used.")
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

    @Deprecated(message = "use /event")
    @PostMapping("/events/quoteCreated")
    fun quoteCreated(
        @RequestHeader(value = "Request-Id", required = false) requestId: String?,
        @RequestBody event: QuoteCreatedEventDto
    ): ResponseEntity<Any> {
        logger.warn("A deprecated event, /events/quoteCreated, is being used.")
        eventHandler.onQuoteCreatedHandleRequest(
            event = QuoteCreatedEvent(
                memberId = event.memberId,
                quoteId = event.quoteId,
                firstName = event.firstName,
                lastName = event.lastName,
                postalCode = event.postalCode,
                street = event.street,
                email = event.email,
                ssn = event.ssn,
                initiatedFrom = event.initiatedFrom,
                attributedTo = event.attributedTo,
                productType = event.productType,
                insuranceType = event.insuranceType,
                currentInsurer = event.currentInsurer,
                price = event.price,
                currency = event.currency,
                originatingProductId = event.originatingProductId
            ),
            requestId = requestId
        )
        return ResponseEntity.accepted().build()
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
