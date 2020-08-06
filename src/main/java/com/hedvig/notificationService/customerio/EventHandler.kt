package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractCreatedJob
import com.hedvig.notificationService.customerio.customerioEvents.jobs.UpdateStartDateJob
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
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val customerioService: CustomerioService,
    private val memberService: MemberServiceImpl,
    private val scheduler: Scheduler
) {
    val jobGroup = "customerio.triggers"

    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {

        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        state.triggerStartDateUpdated(callTime)
        state.updateFirstUpcomingStartDate(event.startDate)
        repo.save(state)

        try {
            val jobName = "onStartDateUpdatedEvent+${event.contractId}"

            val jobData = JobDataMap()
            jobData["memberId"] = event.owningMemberId

            val jobDetail = JobBuilder.newJob()
                .withIdentity(jobName, jobGroup)
                .ofType(UpdateStartDateJob::class.java)
                .requestRecovery()
                .setJobData(jobData)
                .build()

            val trigger = TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey(jobName, jobGroup))
                .forJob(jobName, jobGroup)
                .startNow()
                .withSchedule(
                    SimpleScheduleBuilder
                        .simpleSchedule()
                        .withMisfireHandlingInstructionFireNow()
                )
                .startAt(Date.from(callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)))
                .build()

            scheduler.scheduleJob(
                jobDetail,
                trigger
            )
        } catch (e: SchedulerException) {
            throw RuntimeException(e.message, e)
        }
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
                val newStartTime = Date.from(
                    Instant.now().plus(
                        SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES
                    )
                )
                scheduler.rescheduleJob(
                    triggerKey, existingTrigger.triggerBuilder.startAt(
                        newStartTime
                    ).build()
                )
            } else {
                val jobData = JobDataMap()
                jobData["memberId"] = contractCreatedEvent.owningMemberId

                val jobDetail = createJob(jobName, jobData, ContractCreatedJob::class.java)

                val trigger = TriggerBuilder.newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobName, jobGroup)
                    .startNow()
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withMisfireHandlingInstructionFireNow()
                    )
                    .startAt(Date.from(callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)))
                    .build()

                scheduler.scheduleJob(
                    jobDetail,
                    trigger
                )
            }
        } catch (e: SchedulerException) {
            throw RuntimeException(e.message, e)
        }
    }

    private fun <T : Job> createJob(
        jobName: String,
        jobData: JobDataMap,
        jobClass: Class<T>
    ): JobDetail? {
        return JobBuilder.newJob()
            .withIdentity(jobName, jobGroup)
            .ofType(jobClass)
            .requestRecovery()
            .setJobData(jobData)
            .build()
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
