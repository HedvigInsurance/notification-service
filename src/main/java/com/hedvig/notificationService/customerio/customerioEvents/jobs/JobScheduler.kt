package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.reflect.KClass

class JobScheduler(private val scheduler: Scheduler) {

    val jobGroup = "customerio.triggers"

    fun <T : Job> scheduleJob(
        id: String,
        jobData: Map<String, String>,
        jobClass: KClass<T>,
        startTime: Instant
    ) {
        val jobDataMap = JobDataMap(jobData)
        val jobDetail = createJob(id, jobDataMap, jobClass.java)

        val trigger = createTrigger(id, startTime)

        scheduler.scheduleJob(
            jobDetail,
            trigger
        )
    }

    fun rescheduleJob(
        callTime: Instant,
        triggerKey: TriggerKey
    ): Boolean {
        val existingTrigger = scheduler.getTrigger(triggerKey) ?: return false

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

        return true
    }

    fun <T : Job> createJob(
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

    fun createTrigger(jobName: String, triggerAt: Instant?): SimpleTrigger? {
        return TriggerBuilder.newTrigger()
            .withIdentity(TriggerKey.triggerKey(jobName, jobGroup))
            .forJob(jobName, jobGroup)
            .startNow()
            .withSchedule(
                SimpleScheduleBuilder
                    .simpleSchedule()
                    .withMisfireHandlingInstructionFireNow()
            )
            .startAt(Date.from(triggerAt))
            .build()
    }

    fun rescheduleOrTriggerContractCreated(contractCreatedEvent: ContractCreatedEvent, callTime: Instant) {
        val jobName = "onContractCreatedEvent-${contractCreatedEvent.owningMemberId}"
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)

        val jobRescheduled = this.rescheduleJob(callTime, triggerKey)

        if (!jobRescheduled) {
            val jobData = mapOf(
                "memberId" to contractCreatedEvent.owningMemberId
            )

            this.scheduleJob(
                jobName,
                jobData,
                ContractCreatedJob::class,
                callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)
            )
        }
    }

    fun rescheduleOrTriggerContractActivatedToday(contractCreatedEvent: ContractCreatedEvent, callTime: Instant) {
        if (contractCreatedEvent.startDate != null) {
            val jobData = mapOf(
                "memberId" to contractCreatedEvent.owningMemberId
            )

            this.scheduleJob(
                "contractActivatedTodayJob-aContractId",
                jobData,
                ContractActivatedTodayJob::class,
                contractCreatedEvent.startDate.atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant()
            )
        }
    }
}
