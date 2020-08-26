package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext

class ContractTerminatedEventJobTest {

    @Test
    internal fun `handle one contract`() {
        val eventCreator = mockk<CustomerioEventCreator>(relaxed = true)
        val cut = ContractTerminatedEventJob(
            mockk(relaxed = true),
            mockk(relaxed = true),
            eventCreator,
            mockk(relaxed = true)
        )

        val jobbContext = mockk<JobExecutionContext>(relaxed = true)
        every { jobbContext.mergedJobDataMap } returns JobDataMap(
            mapOf(
                "memberId" to "",
                "contracts" to "someContractId"
            )
        )
        cut.execute(jobbContext)

        verify { eventCreator.contractsTerminatedEvent(any(), listOf("someContractId")) }
    }

    @Test
    internal fun `handle multiple contracts`() {
        val eventCreator = mockk<CustomerioEventCreator>(relaxed = true)
        val cut = ContractTerminatedEventJob(
            mockk(relaxed = true),
            mockk(relaxed = true),
            eventCreator,
            mockk(relaxed = true)
        )

        val jobbContext = mockk<JobExecutionContext>(relaxed = true)
        every { jobbContext.mergedJobDataMap } returns JobDataMap(
            mapOf(
                "memberId" to "",
                "contracts" to "id1,id2,id3"
            )
        )
        cut.execute(jobbContext)

        verify { eventCreator.contractsTerminatedEvent(any(), listOf("id1", "id2", "id3")) }
    }

    @Test
    internal fun `customerio state does not exists`() {
        val eventCreator = mockk<CustomerioEventCreator>(relaxed = true)
        val customerIOStateRepository = mockk<CustomerIOStateRepository>(relaxed = true)
        val customerioService = mockk<CustomerioService>(relaxed = true)
        val cut = ContractTerminatedEventJob(
            customerIOStateRepository,
            mockk(relaxed = true),
            eventCreator,
            customerioService
        )

        every { customerIOStateRepository.findByMemberId(any()) } returns null

        val jobbContext = mockk<JobExecutionContext>(relaxed = true)
        every { jobbContext.mergedJobDataMap } returns JobDataMap(
            mapOf(
                "memberId" to "1337",
                "contracts" to "id1,id2,id3"
            )
        )
        cut.execute(jobbContext)

        verify { customerioService.sendEventAndUpdateState(match { it.memberId == "1337" }, any()) }
    }
}
