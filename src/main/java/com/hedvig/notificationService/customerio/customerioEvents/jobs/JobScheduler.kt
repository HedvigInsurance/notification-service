package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import org.quartz.DateBuilder
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SimpleScheduleBuilder
import org.quartz.SimpleTrigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.reflect.KClass

@Service
class JobScheduler(private val scheduler: Scheduler) {

    companion object {
        val jobGroup = "customerio.triggers"
    }

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
        triggerKey: TriggerKey,
        newStartTime: Date?
    ): Boolean {
        val existingTrigger = scheduler.getTrigger(triggerKey) ?: return false

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

    fun rescheduleOrTriggerContractCreated(callTime: Instant, memberId: String) {
        val jobName = "onContractCreatedEvent-$memberId"
        val triggerKey = TriggerKey.triggerKey(jobName, jobGroup)

        val jobRescheduled = this.rescheduleJob(
            triggerKey, Date.from(
                callTime.plus(
                    SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES
                )
            )
        )

        if (!jobRescheduled) {
            val jobData = mapOf(
                "memberId" to memberId
            )

            this.scheduleJob(
                jobName,
                jobData,
                ContractCreatedJob::class,
                callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)
            )
        }
    }

    fun rescheduleOrTriggerContractActivatedToday(
        activationDate: LocalDate,
        memberId: String,
        contractId: String
    ) {

        val jobName = "contractActivatedTodayJob-$memberId"
        val triggerKey = TriggerKey(jobName, jobGroup)

        val triggerTime = activationDate.atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant()
        val jobExisted = scheduler.getTrigger(triggerKey) != null

        if (!jobExisted) {
            val jobData = mapOf(
                "memberId" to memberId
            )

            this.scheduleJob(
                jobName,
                jobData,
                ContractActivatedTodayJob::class,
                triggerTime
            )
        }
    }

    fun rescheduleOrTriggerStartDateUpdated(
        callTime: Instant,
        memberId: String
    ) {
        val jobData = mapOf(
            "memberId" to memberId
        )
        val jobName = "onStartDateUpdatedEvent+$memberId"

        val successfullRescheduling = rescheduleJob(
            TriggerKey.triggerKey(jobName, jobGroup),
            Date.from(callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES))
        )

        if (!successfullRescheduling) {
            this.scheduleJob(
                jobName,
                jobData,
                StartDateUpdatedJob::class,
                callTime.plus(SIGN_EVENT_WINDOWS_SIZE_MINUTES, ChronoUnit.MINUTES)
            )
        }
    }

    fun rescheduleOrTriggerContractTerminated(
        contractId: String,
        memberId: String,
        terminationDate: LocalDate?,
        finalContract: Boolean
    ) {
        // ContractId stored as strings in jobdata map
        // 1. Create job schedule 30 min in future
        // 2. Update job datamap reschedule job 30 min into future

        val job = scheduler.getJobDetail(JobKey.jobKey("")) ?: JobBuilder.newJob(ContractTerminatedEventJob::class.java)
            .build()
        val existingContracts = job.jobDataMap.getString("contracts") ?: ""
        val updatedContracts = existingContracts.split(',').plus(contractId)
        job.jobDataMap.put("contracts", updatedContracts.joinToString(","))

        scheduler.addJob(job, true)

        rescheduleJob(
            TriggerKey.triggerKey(""), DateBuilder.futureDate(30, DateBuilder.IntervalUnit.MINUTE)
        )
    }
}
