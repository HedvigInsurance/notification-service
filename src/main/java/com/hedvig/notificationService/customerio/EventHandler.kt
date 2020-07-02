package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.service.FirebaseNotificationService
import com.hedvig.notificationService.serviceIntegration.memberService.dto.PersonHasSignedBeforeRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val configuration: ConfigurationProperties,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val customerioService: CustomerioService,
    private val memberService: MemberServiceImpl
) {
    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {
        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        if (!configuration.useNorwayHack) {
            state.triggerStartDateUpdated(callTime)
            state.updateFirstUpcomingStartDate(event.startDate)
            repo.save(state)
        }
    }

    fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
            ?: CustomerioState(contractCreatedEvent.owningMemberId)

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        if (!configuration.useNorwayHack) {
            state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
            repo.save(state)
        }
    }

    fun onFailedChargeEvent(memberId: String, chargeFailedEvent: ChargeFailedEvent) {
        customerioService.sendEvent(memberId, chargeFailedEvent.toMap(memberId))

        try {
            if (chargeFailedEvent.terminationDate != null) {
                firebaseNotificationService.sendTerminatedFailedChargesNotification(memberId)
                return
            }

            when (chargeFailedEvent.chargeFailedReason) {
                ChargeFailedReason.INSUFFICIENT_FUNDS -> firebaseNotificationService.sendPaymentFailedNotification(
                    memberId
                )
                ChargeFailedReason.NOT_CONNECTED_DIRECT_DEBIT -> firebaseNotificationService.sendConnectDirectDebitNotification(
                    memberId
                )
            }
        } catch (e: Exception) {
            logger.error("onFailedChargeEvent - Can not send notification for $memberId - Exception: ${e.message}")
        }
    }

    fun onContractRenewalQueued(event: ContractRenewalQueuedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(event.memberId)
            ?: CustomerioState(event.memberId)

        state.queueContractRenewal(event.contractId, callTime)
        repo.save(state)
    }

    fun onQuoteCreated(event: QuoteCreatedEvent, callTime: Instant = Instant.now()) {
        val shouldNotSendEvent = event.initiatedFrom == "HOPE" ||
            event.originatingProductId != null ||
            event.productType == "UNKNOWN"
        if (shouldNotSendEvent) return
        val hasSignedBefore = memberService.hasSignedBefore(
            PersonHasSignedBeforeRequest(
                ssn = event.ssn,
                email = event.email
            )
        )
        if (hasSignedBefore) return
        customerioService.updateCustomerAttributes(event.memberId, mapOf(
            "email" to event.email,
            "first_name" to event.firstName,
            "last_name" to event.lastName
        ), callTime)
        customerioService.sendEvent(event.memberId, event.toMap())
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
