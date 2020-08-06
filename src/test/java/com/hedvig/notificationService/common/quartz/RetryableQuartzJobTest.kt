package com.hedvig.notificationService.common.quartz

import assertk.assertThat
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.makeJobExecutionContext
import io.mockk.mockk
import org.junit.Test
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.impl.JobExecutionContextImpl

class RetryableQuartzJobTest {

    class TestableJob() : Job {
        override fun execute(p0: JobExecutionContext?) {
        }
    }

    @Test
    fun `executionWithoutExecutesPassedLambda`() {

        val scheduler = mockk<Scheduler>()
        val job = TestableJob()
        val jobContext = makeJobExecutionContext(scheduler, job, JobDataMap())
        var changeMe = false
        executeWithRetry(jobContext) {
            changeMe = true
        }

        assertThat(changeMe).isTrue()
    }

    private fun executeWithRetry(context: JobExecutionContextImpl, function: () -> Unit) {
        function()
    }
}
