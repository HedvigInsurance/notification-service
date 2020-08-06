//TODO: I think we should move this one to package com.hedvig.notificationService.service hence its not really just customer io
// but I'm thinking of the merge conflict, will move it before opening the pr
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
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasPersonSignedBeforeRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val configuration: ConfigurationProperties,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val customerioService: CustomerioService,
    private val memberService: MemberServiceImpl,
    private val handledRequestRepository: HandledRequestRepository
) {
    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }

        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        state.triggerStartDateUpdated(callTime)
        state.updateFirstUpcomingStartDate(event.startDate)
        repo.save(state)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onContractCreatedEvent(
        contractCreatedEvent: ContractCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
            ?: CustomerioState(contractCreatedEvent.owningMemberId)

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
        repo.save(state)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onFailedChargeEvent(
        memberId: String,
        chargeFailedEvent: ChargeFailedEvent,
        requestId: String?
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }

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
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onContractRenewalQueued(
        event: ContractRenewalQueuedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        customerioService.sendEvent(event.memberId, event.toMap())
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onQuoteCreated(
        event: QuoteCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        val shouldNotSendEvent = event.initiatedFrom == "HOPE" ||
                event.originatingProductId != null ||
                event.productType == "UNKNOWN"
        if (shouldNotSendEvent) {
            logger.info("Will not send QuoteCreatedEvent to customer.io for member=${event.memberId} (event=$event)")
            return
        }
        val hasSignedBefore = memberService.hasPersonSignedBefore(
            HasPersonSignedBeforeRequest(
                ssn = event.ssn,
                email = event.email
            )
        )
        if (hasSignedBefore) {
            logger.info("Will not send QuoteCreatedEvent to customer.io for member=${event.memberId} since the person signed before")
            return
        }
        customerioService.updateCustomerAttributes(
            event.memberId, mapOf(
                "email" to event.email,
                "first_name" to event.firstName,
                "last_name" to event.lastName
            ), callTime
        )
        customerioService.sendEvent(event.memberId, event.toMap())
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
