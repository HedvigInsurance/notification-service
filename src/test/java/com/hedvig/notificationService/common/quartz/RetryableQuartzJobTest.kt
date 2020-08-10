package com.hedvig.notificationService.common.quartz

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isGreaterThan
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.customerioEvents.jobs.makeJobExecutionContext
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import java.util.Date

class RetryableQuartzJobTest {

    class TestableJob() : Job {
        override fun execute(p0: JobExecutionContext?) {
        }
    }

    @Test
    fun executionWithoutExceptionRunsLambda() {

        val scheduler = mockk<Scheduler>()
        val job = TestableJob()
        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                JobDataMap()
            )
        var changeMe = false
        executeWithRetry(jobContext) {
            changeMe = true
        }

        assertThat(changeMe).isTrue()
    }

    @Test
    fun `exception during execution causes job to be resceduled`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()
        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                JobDataMap()
            )

        val jobSlot = slot<JobDetail>()
        val triggerSlot = slot<Trigger>()
        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSlot)) } returns Date()
        executeWithRetry(jobContext) {
            throw RuntimeException()
        }

        assertThat(triggerSlot.captured.startTime).isGreaterThan(jobContext.trigger.startTime)
    }

    @Test
    fun `first exception during execution causes job retries to be  set to one`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()
        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                JobDataMap()
            )

        val jobSlot = slot<JobDetail>()
        val triggerSlot = slot<Trigger>()
        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSlot)) } returns Date()
        executeWithRetry(jobContext) {
            throw RuntimeException()
        }

        assertThat(jobContext.jobDetail.jobDataMap).contains("RETRY_COUNT", "1")
    }

    @Test
    fun `second exception durin execution causes job retries to be incremented`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()
        val jobData = JobDataMap()
        jobData.putAsString("RETRY_COUNT", 2)
        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )

        val jobSlot = slot<JobDetail>()
        val triggerSlot = slot<Trigger>()
        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSlot)) } returns Date()
        executeWithRetry(jobContext) {
            throw RuntimeException()
        }

        assertThat(jobContext.jobDetail.jobDataMap).contains("RETRY_COUNT", "3")
    }

    @Test
    fun `do not reschedule when retrycount greater than max_retries`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()

        val jobData = JobDataMap()
        jobData.putAsString("RETRY_COUNT", 5)

        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )

        val jobSlot = slot<JobDetail>()
        val triggerSlot = slot<Trigger>()
        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSlot)) } returns Date()
        executeWithRetry(jobContext) {
            throw RuntimeException()
        }

        verify(inverse = true) { scheduler.scheduleJob(any(), any()) }
    }

    @Test
    fun `call error lambda after max retries`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()

        val jobData = JobDataMap()
        jobData.putAsString("RETRY_COUNT", MAX_RETRIES)

        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )

        every { scheduler.scheduleJob(any(), any()) } returns Date()

        var errorLambdaCalled = false
        executeWithRetry(jobContext, {
            errorLambdaCalled = true
        }) {
            throw RuntimeException()
        }

        assertThat(errorLambdaCalled).isTrue()
    }

    @Test
    fun `call error function if exception happens during scheduling`() {
        val scheduler = mockk<Scheduler>()
        val job = TestableJob()

        val jobData = JobDataMap()
        jobData.putAsString("RETRY_COUNT", 1)

        val jobContext =
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )

        every { scheduler.scheduleJob(any(), any()) } throws SchedulerException()

        var errorLambdaCalled = false
        executeWithRetry(jobContext, {
            errorLambdaCalled = true
        }) {
            throw RuntimeException()
        }

        assertThat(errorLambdaCalled).isTrue()
    }
}
