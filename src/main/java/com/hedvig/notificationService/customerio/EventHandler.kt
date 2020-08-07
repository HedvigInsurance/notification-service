package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractActivatedTodayJob
import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractCreatedJob
import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.customerioEvents.jobs.StartDateUpdatedJob
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
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasPersonSignedBeforeRequest
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.reflect.KClass

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val customerioService: CustomerioService,
    private val memberService: MemberServiceImpl,
    private val scheduler: Scheduler
) {
    val jobGroup = "customerio.triggers"
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

        val jobData = mapOf(
            "memberId" to event.owningMemberId
        )

        jobScheduler.scheduleJob(
            "onStartDateUpdatedEvent+${event.contractId}",
            jobData,
            StartDateUpdatedJob::class,
            callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)
        )
    }

    private fun <T : Job> scheduleJob(
        jobName: String,
        jobData: JobDataMap,
        jobClass: KClass<T>,
        startTime: Instant
    ) {
        val jobDetail = jobScheduler.createJob(jobName, jobData, jobClass.java)

        val trigger = jobScheduler.createTrigger(jobName, startTime)

        scheduler.scheduleJob(
            jobDetail,
            trigger
        )
    }

    fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
            ?: CustomerioState(contractCreatedEvent.owningMemberId)

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
        repo.save(state)

        try {
            val jobName = "onContractCreatedEvent-${contractCreatedEvent.owningMemberId}"
            val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)

            val existingTrigger = scheduler.getTrigger(triggerKey)
            if (existingTrigger != null) {
                rescheduleJob(callTime, triggerKey, existingTrigger)
            } else {
                val jobData = JobDataMap()
                jobData["memberId"] = contractCreatedEvent.owningMemberId

                scheduleJob(
                    jobName,
                    jobData,
                    ContractCreatedJob::class,
                    callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)
                )
            }

            if (contractCreatedEvent.startDate != null) {
                val jobData = JobDataMap()
                jobData["memberId"] = contractCreatedEvent.owningMemberId
                scheduleJob(
                    "contractActivatedTodayJob-aContractId",
                    jobData,
                    ContractActivatedTodayJob::class,
                    contractCreatedEvent.startDate.atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant()
                )
            }
        } catch (e: SchedulerException) {
            throw RuntimeException(e.message, e)
        }
    }

    private fun rescheduleJob(
        callTime: Instant,
        triggerKey: TriggerKey?,
        existingTrigger: Trigger
    ) {
        val newStartTime = Date.from(
            callTime.plus(
                SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES
            )
        )
        scheduler.rescheduleJob(
            triggerKey, existingTrigger.triggerBuilder.startAt(
                newStartTime
            ).build()
        )
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
        customerioService.sendEvent(event.memberId, event.toMap())
    }

    fun onQuoteCreated(event: QuoteCreatedEvent, callTime: Instant = Instant.now()) {
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

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
