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
        const val jobGroup = "customerio.triggers"
    }

    fun <T : Job> scheduleJob(
        id: String,
        jobData: Map<String, String>,
        jobClass: KClass<T>,
        startTime: Instant
    ) {
        val jobDetail = createJob(id, jobData, jobClass.java)

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
        jobData: Map<String, String> = mapOf(),
        jobClass: Class<T>
    ): JobDetail {
        return JobBuilder.newJob()
            .withIdentity(jobName, jobGroup)
            .ofType(jobClass)
            .requestRecovery()
            .setJobData(JobDataMap(jobData))
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
        terminationDate: LocalDate?
    ) {
        val jobKey = JobKey.jobKey("onContractTerminatedEvent-$memberId", jobGroup)
        val job =
            scheduler.getJobDetail(jobKey) ?: createJob(
                jobName = jobKey.name,
                jobClass = ContractTerminatedEventJob::class.java
            )

        val existingContracts = job.jobDataMap.getString("contracts") ?: ""
        val updatedContracts = existingContracts.split(',').plus(contractId).filter { it.isNotEmpty() }
        job.jobDataMap["contracts"] = updatedContracts.joinToString(",")
        job.jobDataMap["memberId"] = memberId

        scheduler.addJob(job, true, true)

        val newStartTime = DateBuilder.futureDate(30, DateBuilder.IntervalUnit.MINUTE)
        val triggerKey = TriggerKey.triggerKey("onContractTerminatedEvent-$memberId", jobGroup)
        val triggerExisted = rescheduleJob(
            triggerKey,
            newStartTime
        )
        if (!triggerExisted) {
            scheduler.scheduleJob(
                job,
                TriggerBuilder
                    .newTrigger()
                    .withIdentity(triggerKey)
                    .startAt(newStartTime)
                    .withSchedule(
                        SimpleScheduleBuilder
                            .simpleSchedule()
                            .withMisfireHandlingInstructionFireNow()
                    )
                    .build()
            )
        }
    }
}
