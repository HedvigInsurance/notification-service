package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.event.ChargeFailedEvent
import com.hedvig.notificationService.service.event.ContractCreatedEvent
import com.hedvig.notificationService.service.event.ContractRenewalQueuedEvent
import com.hedvig.notificationService.service.request.HandledRequestRepository
import com.hedvig.notificationService.service.event.QuoteCreatedEvent
import com.hedvig.notificationService.service.event.StartDateUpdatedEvent
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasPersonSignedBeforeRequest
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val customerioService: CustomerioService,
    private val memberService: MemberServiceImpl,
    scheduler: Scheduler,
    private val handledRequestRepository: HandledRequestRepository
) {
    val jobScheduler = JobScheduler(scheduler)

    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {

        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        state.triggerStartDateUpdated(callTime)
        state.updateFirstUpcomingStartDate(event.startDate)
        repo.save(state)

        jobScheduler.rescheduleOrTriggerStartDateUpdated(event, callTime)
        jobScheduler.rescheduleOrTriggerContractActivatedToday(
            event.startDate,
            event.owningMemberId
        )
    }

    fun onContractCreatedEvent(
        contractCreatedEvent: ContractCreatedEvent,
        callTime: Instant = Instant.now()
    ) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
            ?: CustomerioState(contractCreatedEvent.owningMemberId)

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
        repo.save(state)

        try {
            jobScheduler.rescheduleOrTriggerContractCreated(contractCreatedEvent, callTime)

            contractCreatedEvent.startDate?.let {
                jobScheduler.rescheduleOrTriggerContractActivatedToday(
                    it,
                    contractCreatedEvent.owningMemberId
                )
            }
        } catch (e: SchedulerException) {
            throw RuntimeException(e.message, e)
        }
    }
    fun onFailedChargeEvent(
        chargeFailedEvent: ChargeFailedEvent
    ) {
        customerioService.sendEvent(chargeFailedEvent.memberId, chargeFailedEvent.toMap())

        try {
            if (chargeFailedEvent.terminationDate != null) {
                firebaseNotificationService.sendTerminatedFailedChargesNotification(chargeFailedEvent.memberId)
                return
            }

            when (chargeFailedEvent.chargeFailedReason) {
                ChargeFailedReason.INSUFFICIENT_FUNDS -> firebaseNotificationService.sendPaymentFailedNotification(
                    chargeFailedEvent.memberId
                )
                ChargeFailedReason.NOT_CONNECTED_DIRECT_DEBIT -> firebaseNotificationService.sendConnectDirectDebitNotification(
                    chargeFailedEvent.memberId
                )
            }
        } catch (e: Exception) {
            logger.error("onFailedChargeEvent - Can not send notification for ${chargeFailedEvent.memberId} - Exception: ${e.message}")
        }
    }

    fun onContractRenewalQueued(
        event: ContractRenewalQueuedEvent,
        callTime: Instant = Instant.now()
    ) {
        customerioService.sendEvent(event.memberId, event.toMap())
    }

    fun onQuoteCreated(
        event: QuoteCreatedEvent,
        callTime: Instant = Instant.now()
    ) {
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
    }

    /**
     * Old request handlers
     */
    fun onStartDateUpdatedEventHandleRequest(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        onStartDateUpdatedEvent(event, callTime)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onContractCreatedEventHandleRequest(
        contractCreatedEvent: ContractCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        onContractCreatedEvent(contractCreatedEvent, callTime)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onFailedChargeEventHandleRequest(
        chargeFailedEvent: ChargeFailedEvent,
        requestId: String?
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        onFailedChargeEvent(chargeFailedEvent)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onContractRenewalQueuedHandleRequest(
        event: ContractRenewalQueuedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        onContractRenewalQueued(event, callTime)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    fun onQuoteCreatedHandleRequest(
        event: QuoteCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) {
        requestId?.let {
            if (handledRequestRepository.isRequestHandled(it)) {
                return
            }
        }
        onQuoteCreated(event, callTime)
        requestId?.let {
            handledRequestRepository.storeHandledRequest(it)
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
