package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.quartz.JobExecutionContext
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class ContractActivatedTodayJobTest {

    @Test
    fun `current date is set to be in stockholm`() {

        val contractLoader = mockk<ContractLoader>()
        val eventCreator = mockk<CustomerioEventCreator>(relaxed = true)
        val sut = ContractActivatedTodayJob(
            contractLoader = contractLoader,
            eventCreator = eventCreator,
            customerioService = mockk(relaxed = true),
            customerIOStateRepository = mockk(relaxed = true),
            clock = Clock.fixed(
                Instant.parse("2020-08-11T22:00:00Z"), ZoneId.of("UTC")
            )
        )

        val jobExecutionContext = mockk<JobExecutionContext>(relaxed = true)
        every { jobExecutionContext.mergedJobDataMap.get("memberId") } returns "1337"

        every { contractLoader.getContractInfoForMember(any()) } returns listOf()

        sut.execute(jobExecutionContext)

        verify { eventCreator.sendActivatesToday(any(), any(), LocalDate.of(2020, 8, 12)) }
    }
}
