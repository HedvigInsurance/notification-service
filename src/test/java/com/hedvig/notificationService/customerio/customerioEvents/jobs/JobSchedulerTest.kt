package com.hedvig.notificationService.customerio.customerioEvents.jobs

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import com.hedvig.notificationService.service.event.StartDateUpdatedEvent
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Test
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

class JobSchedulerTest {

    val scheduler = mockk<Scheduler>()

    @Test
    fun rescheduleOrTriggerStartDateUpdated() {

        val callTime = Instant.now()

        val jobScheduler = JobScheduler(scheduler)

        every { scheduler.getTrigger(any()) } returns
            TriggerBuilder
                .newTrigger()
                .withIdentity("onStartDateUpdatedEvent+contractId")
                .build()
        every { scheduler.rescheduleJob(any(), any()) } returns Date()

        val event = StartDateUpdatedEvent(
            "contractId",
            "someMemberId",
            LocalDate.of(2020, 10, 1),
            false,
            "HDI",
            "HDI"
        )
        jobScheduler.rescheduleOrTriggerStartDateUpdated(
            callTime,
            event.owningMemberId
        )

        val triggerKeySlot = slot<TriggerKey>()
        val triggerSlot = slot<Trigger>()
        verify { scheduler.rescheduleJob(capture(triggerKeySlot), capture(triggerSlot)) }

        assertThat(triggerKeySlot.captured.name).isEqualTo("onStartDateUpdatedEvent+someMemberId")
        assertThat(triggerSlot.captured.startTime).isEqualTo(
            Date.from(
                callTime.plus(
                    SIGN_EVENT_WINDOWS_SIZE_MINUTES,
                    ChronoUnit.MINUTES
                )
            )
        )
    }
}
