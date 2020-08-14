package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class OnContractTerminatedEvent {

    @Test
    fun `bla bla bla`() {

        val jobScheduler = mockk<JobScheduler>(relaxed = true)
        val eventHandler = EventHandler(
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            mockk(),
            jobScheduler
        )

        eventHandler.onContractTerminatedEvent(
            ContractTerminatedEvent(
                "aContractId",
                "1234",
                LocalDate.of(2020, 1, 1),
                true
            ),
            Instant.parse("2020-08-14T07:29:00Z")
        )

        verify {
            jobScheduler.rescheduleOrTriggerContractTerminated(
                "aContractId",
                "1234",
                LocalDate.of(2020, 1, 1),
                true
            )
        }
    }
}
