package com.hedvig.notificationService.common.quartz

import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.SchedulerException
import org.quartz.TriggerBuilder
import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles
import java.time.temporal.ChronoUnit
import java.util.Date

val MAX_RETRIES = 5

private val logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass().`package`.name)

fun executeWithRetry(
    context: JobExecutionContext,
    errorFunction: (Exception) -> Unit = {},
    function: () -> Unit
) {
    try {
        function()
    } catch (e: RuntimeException) {
        logger.warn("Caught exception in job will retry", e)

        val retryCount = context.jobDetail.jobDataMap.getIntOrNull("RETRY_COUNT") ?: 0
        context.jobDetail.jobDataMap.putAsString("RETRY_COUNT", retryCount + 1)

        if (retryCount < MAX_RETRIES) {
            rescheduleJob(context, errorFunction)
        } else {
            errorFunction(e)
        }
    }
}

fun rescheduleJob(context: JobExecutionContext, errorFunction: (ex: Exception) -> Unit) {
    try {
        val originalStartTime = context.trigger.startTime.toInstant()
        val newStartTime = Date.from(
            originalStartTime.plus(
                SIGN_EVENT_WINDOWS_SIZE_MINUTES,
                ChronoUnit.MINUTES
            )
        )

        context.scheduler.scheduleJob(
            context.jobDetail,
            TriggerBuilder.newTrigger().startAt(newStartTime).build()
        )
    } catch (ex: SchedulerException) {
        errorFunction(ex)
    }
}

fun JobDataMap.getIntOrNull(key: String): Int? =
    this.getString(key)?.toInt()
