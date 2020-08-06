package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.customerioEvents.UpdateStartDateJob
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SchedulerContext
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.impl.JobExecutionContextImpl
import org.quartz.spi.OperableTrigger
import org.quartz.spi.TriggerFiredBundle
import java.util.Date

class RetryableJobTest {

    var job: Job
    private val customerioService: CustomerioService = mockk()
    private val scheduler: Scheduler = mockk()
    private val customerIOStateRepository: CustomerIOStateRepository = InMemoryCustomerIOStateRepository()

    init {

        job = UpdateStartDateJob(
            mockk(),
            mockk(),
            customerioService,
            customerIOStateRepository
        )
        every { scheduler.context } returns SchedulerContext()
        customerIOStateRepository.save(CustomerioState("1234"))
    }

    @Test
    fun successfulRunDoesNothing() {

        every { customerioService.doUpdate(any(), any(), any()) } returns Unit

        val jobData = JobDataMap()
        jobData["memberId"] = "1234"
        job.execute(makeJobExecutionContext(scheduler, job, jobData))

        verify(inverse = true) { scheduler.scheduleJob(any(), any()) }
    }

    @Test
    fun exceptionReschdulesJob() {

        val slot1 = slot<JobDetail>()
        val slot2 = slot<Trigger>()
        every { scheduler.scheduleJob(capture(slot1), capture(slot2)) } returns Date()

        val jobData = JobDataMap()
        jobData["memberId"] = "1234"
        job.execute(makeJobExecutionContext(scheduler, job, jobData))
        assertThat(slot1.captured).isNotNull()
    }
}

fun makeJobExecutionContext(
    scheduler: Scheduler,
    job: Job,
    jobData: JobDataMap
): JobExecutionContextImpl {
    val jobDetail = JobBuilder
        .newJob(job::class.java)
        .setJobData(jobData)
        .build()

    return JobExecutionContextImpl(
        scheduler,
        TriggerFiredBundle(
            jobDetail,
            TriggerBuilder.newTrigger().build() as OperableTrigger,
            null,
            false,
            Date(),
            Date(),
            null,
            null
        ),
        job
    )
}
