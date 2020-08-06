package com.hedvig.notificationService.common.quartz

import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.SchedulerException
import org.quartz.TriggerBuilder
import java.time.temporal.ChronoUnit
import java.util.Date

val MAX_RETRIES = 5
fun executeWithRetry(
    context: JobExecutionContext,
    errorFunction: () -> Unit = {},
    function: () -> Unit
) {
    try {
        function()
    } catch (e: RuntimeException) {
        val retryCount = context.jobDetail.jobDataMap.getIntOrNull("RETRY_COUNT") ?: 0
        context.jobDetail.jobDataMap.putAsString("RETRY_COUNT", retryCount + 1)

        if (retryCount < MAX_RETRIES) {
            rescheduleJob(context, errorFunction)
        } else {
            errorFunction()
        }
    }
}

fun rescheduleJob(context: JobExecutionContext, errorFunction: () -> Unit) {
    try {
        val originalStartTime = context.trigger.startTime.toInstant()
        val newStartTime = Date.from(
            originalStartTime.plus(
                1,
                ChronoUnit.MINUTES
            )
        )

        context.scheduler.scheduleJob(
            context.jobDetail,
            TriggerBuilder.newTrigger().startAt(newStartTime).build()
        )
    } catch (ex: SchedulerException) {
        errorFunction()
    }
}

fun JobDataMap.getIntOrNull(key: String): Int? =
    this.getString(key)?.toIntOrNull()
