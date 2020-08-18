package com.hedvig.notificationService.customerio.customerioEvents.jobs

import assertk.assertThat
import assertk.assertions.contains
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.quartz.JobDetail
import org.quartz.Scheduler

class ScheduleContractTerminatedTest {

    val scheduler: Scheduler = mockk(relaxed = true)

    @Test
    fun `with no previous job exists add contractId to jobDataMap`() {

        val jobScheduler = JobScheduler(scheduler)

        every { scheduler.getJobDetail(any()) } returns null

        jobScheduler.rescheduleOrTriggerContractTerminated(
            "aContractId",
            "",
            null,
            false
        )

        val jobDetail = slot<JobDetail>()
        verify { scheduler.addJob(capture(jobDetail), true) }

        assertThat(jobDetail.captured.jobDataMap).contains("contracts" to "aContractId")
    }
}
