package com.hedvig.notificationService.customerio.customerioEvents.jobs

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
}
