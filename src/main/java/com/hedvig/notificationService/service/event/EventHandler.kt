package com.hedvig.notificationService.service.event

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
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
    private val handledRequestRepository: HandledRequestRepository,
    private val jobScheduler: JobScheduler = JobScheduler(scheduler),
    private val workspaceSelector: WorkspaceSelector
) {

    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {

        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        state.sentStartDateUpdatedEvent()
        state.sentActivatesTodayEvent(null)

        repo.save(state)

        jobScheduler.rescheduleOrTriggerStartDateUpdated(callTime, event.owningMemberId)
        jobScheduler.rescheduleOrTriggerContractActivatedToday(
            event.startDate,
            event.owningMemberId,
            contractId = event.contractId
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
            jobScheduler.rescheduleOrTriggerContractCreated(callTime, contractCreatedEvent.owningMemberId)

            contractCreatedEvent.startDate?.let {
                jobScheduler.rescheduleOrTriggerContractActivatedToday(
                    it,
                    contractCreatedEvent.owningMemberId,
                    contractCreatedEvent.contractId
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
            memberId = event.memberId,
            ssn = event.ssn,
            email = event.email
        )
        if (hasSignedBefore) {
            logger.info("Will not send QuoteCreatedEvent to customer.io for member=${event.memberId} since the person signed before")
            return
        }

        val hasRedFlag = memberService.hasRedFlag(event.memberId)
        if (hasRedFlag) {
            logger.info("Will not send QuoteCreatedEvent to customer.io for member=${event.memberId} since the person has red flag")
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

    fun onContractTerminatedEvent(event: ContractTerminatedEvent, callTime: Instant = Instant.now()) {
        jobScheduler.rescheduleOrTriggerContractTerminated(
            event.contractId,
            event.owningMemberId,
            event.terminationDate
        )
    }

    fun onPhoneNumberUpdatedEvent(
        event: PhoneNumberUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {
        val workspace = workspaceSelector.getWorkspaceForMember(event.memberId)

        workspace.countryCode?.let { countryCode ->
            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val phoneNumber = phoneNumberUtil.format(
                phoneNumberUtil.parse(event.phoneNumber, countryCode.name),
                PhoneNumberUtil.PhoneNumberFormat.E164
            )

            customerioService.updateCustomerAttributes(
                event.memberId, mapOf(
                    "phone_number" to phoneNumber
                )
            )
        }
    }

    /**
     * Old request handlers
     */
    fun onStartDateUpdatedEventHandleRequest(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) = handleAndStoreUnhandledRequest(requestId) {
        onStartDateUpdatedEvent(event, callTime)
    }

    fun onContractCreatedEventHandleRequest(
        contractCreatedEvent: ContractCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) = handleAndStoreUnhandledRequest(requestId) {
        onContractCreatedEvent(contractCreatedEvent, callTime)
    }

    fun onFailedChargeEventHandleRequest(
        chargeFailedEvent: ChargeFailedEvent,
        requestId: String?
    ) = handleAndStoreUnhandledRequest(requestId) {
        onFailedChargeEvent(chargeFailedEvent)
    }

    fun onContractRenewalQueuedHandleRequest(
        event: ContractRenewalQueuedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) = handleAndStoreUnhandledRequest(requestId) {
        onContractRenewalQueued(event, callTime)
    }

    fun onQuoteCreatedHandleRequest(
        event: QuoteCreatedEvent,
        callTime: Instant = Instant.now(),
        requestId: String? = null
    ) = handleAndStoreUnhandledRequest(requestId) {
        onQuoteCreated(event, callTime)
    }

    private fun handleAndStoreUnhandledRequest(requestId: String?, handle: () -> Unit) {
        requestId?.let {
            if (!handledRequestRepository.isRequestHandled(it)) {
                handle.invoke()
                handledRequestRepository.storeHandledRequest(it)
            }
        } ?: handle.invoke()
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
