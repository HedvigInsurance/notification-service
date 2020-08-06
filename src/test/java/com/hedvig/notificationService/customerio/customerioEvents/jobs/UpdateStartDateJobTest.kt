package com.hedvig.notificationService.customerio.customerioEvents.jobs

import assertk.assertThat
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
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
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class UpdateStartDateJobTest {

    var job: Job
    private val customerioService: CustomerioService = mockk()
    private val scheduler: Scheduler = mockk()
    private val customerIOStateRepository: CustomerIOStateRepository = InMemoryCustomerIOStateRepository()
    private val contractLoader: ContractLoader = mockk()

    init {

        job = UpdateStartDateJob(
            contractLoader,
            CustomerioEventCreatorImpl(),
            customerioService,
            customerIOStateRepository
        )
        every { scheduler.context } returns SchedulerContext()
        customerIOStateRepository.save(CustomerioState("1234"))
    }

    @Test
    fun successfulRunDoesNothing() {

        every { customerioService.sendEventAndUpdateState(any(), any()) } returns Unit
        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                LocalDate.of(2020, 8, 13),
                contractId = UUID.fromString("4c1b5d7c-d822-11ea-aab8-735d900f8217")
            )
        )

        val jobData = JobDataMap()
        jobData["memberId"] = "1234"
        job.execute(
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )
        )

        verify(inverse = true) { scheduler.scheduleJob(any(), any()) }
    }

    @Test
    fun exceptionReschedulesJob() {

        val slot1 = slot<JobDetail>()
        val slot2 = slot<Trigger>()
        every { scheduler.scheduleJob(capture(slot1), capture(slot2)) } returns Date()

        val jobData = JobDataMap()
        jobData["memberId"] = "1234"

        every { customerioService.doUpdate(any(), any(), any()) } throws RuntimeException()
        job.execute(
            makeJobExecutionContext(
                scheduler,
                job,
                jobData
            )
        )
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
