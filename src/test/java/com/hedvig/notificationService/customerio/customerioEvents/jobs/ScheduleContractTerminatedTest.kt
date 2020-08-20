package com.hedvig.notificationService.customerio.customerioEvents.jobs

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.TriggerKey

class ScheduleContractTerminatedTest {

    val scheduler: Scheduler = mockk(relaxed = true)

    @Test
    fun `with no previous job exists add contractId to jobDataMap`() {

        val jobScheduler = JobScheduler(scheduler)

        every { scheduler.getJobDetail(any()) } returns null

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "1",
            null
        )

        val jobDetail = slot<JobDetail>()
        verify { scheduler.addJob(capture(jobDetail), true) }

        assertThat(jobDetail.captured.jobDataMap).containsAll(
            "contracts" to "aContractId",
            "memberId" to "1"
        )
    }

    @Test
    fun `with existing job add contractId to jobDataMap`() {

        val jobScheduler = JobScheduler(scheduler)

        every { scheduler.getJobDetail(any()) } returns JobBuilder
            .newJob(ContractTerminatedEventJob::class.java)
            .setJobData(
                JobDataMap(
                    mapOf(
                        "contracts" to "anExistingContractId",
                        "memberId" to "2"
                    )
                )
            )
            .build()

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "2",
            null
        )

        val jobDetail = slot<JobDetail>()
        verify { scheduler.addJob(capture(jobDetail), true) }

        assertThat(jobDetail.captured.jobDataMap).containsAll(
            "contracts" to "anExistingContractId,aContractId",
            "memberId" to "2"
        )
    }

    @Test
    fun `with existing job use memberId in jobKey`() {

        val jobScheduler = JobScheduler(scheduler)

        every {
            scheduler.getJobDetail(any())
        } returns JobBuilder
            .newJob(ContractTerminatedEventJob::class.java)
            .setJobData(JobDataMap(mapOf("contracts" to "anExistingContractId")))
            .build()

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "1337",
            null
        )

        val jobDetail = slot<JobDetail>()

        val expectedJobKey = JobKey.jobKey("onContractTerminatedEvent-1337", JobScheduler.jobGroup)
        verify { scheduler.getJobDetail(expectedJobKey) }
        verify { scheduler.addJob(capture(jobDetail), true) }
    }

    @Test
    fun `with no existing job use memberId in jobKey`() {

        val jobScheduler = JobScheduler(scheduler)

        every {
            scheduler.getJobDetail(any())
        } returns null

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "1337",
            null
        )

        val jobDetail = slot<JobDetail>()

        val expectedJobKey = JobKey.jobKey("onContractTerminatedEvent-1337", JobScheduler.jobGroup)
        verify { scheduler.addJob(capture(jobDetail), true) }
        assertThat(jobDetail.captured.key).isEqualTo(expectedJobKey)
    }

    @Test
    fun `memberId in triggerKey`() {

        val jobScheduler = JobScheduler(scheduler)

        every {
            scheduler.getJobDetail(any())
        } returns null

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "1337",
            null
        )

        val expectedTriggerKey = TriggerKey.triggerKey("onContractTerminatedEvent-1337", JobScheduler.jobGroup)
        verify { scheduler.getTrigger(expectedTriggerKey) }
    }
}
