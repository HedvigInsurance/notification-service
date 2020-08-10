package com.hedvig.notificationService.customerio

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.QuartzMigrator
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class QuartzMigratorTest {

    val repo = InMemoryCustomerIOStateRepository()
    val jobScheduler = mockk<JobScheduler>(relaxed = true)
    val contractLoader = mockk<ContractLoader>()

    @Test
    fun `migrate_states_with_contracts with activation dates`() {

        val aRandomStartDate = LocalDate.of(2020, 8, 15)
        val contractId = UUID.fromString("258c203c-dafd-11ea-b8d7-a72cade86094")

        repo.save(CustomerioState("someMemberId", activationDateTriggerAt = aRandomStartDate))

        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.SwedishApartment,
                "",
                startDate = aRandomStartDate,
                contractId = contractId
            )
        )

        val migrator = QuartzMigrator(repo, jobScheduler, contractLoader)
        migrator.migrate(Instant.parse("2020-08-10T11:41:38.479483Z"))

        assertThat(repo.data["someMemberId"]!!.activationDateTriggerAt).isNull()
        verify {
            jobScheduler.rescheduleOrTriggerContractActivatedToday(
                aRandomStartDate,
                "someMemberId",
                contractId.toString()
            )
        }
    }

    @Test
    fun `do not migrate states activation date in past`() {

        val aRandomStartDate = LocalDate.of(2020, 8, 9)
        val contractId = UUID.fromString("258c203c-dafd-11ea-b8d7-a72cade86094")

        repo.save(CustomerioState("someMemberId", activationDateTriggerAt = aRandomStartDate))

        every { contractLoader.getContractInfoForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.SwedishApartment,
                "",
                startDate = aRandomStartDate,
                contractId = contractId
            )
        )

        val migrator = QuartzMigrator(repo, jobScheduler, contractLoader)
        migrator.migrate(Instant.parse("2020-08-10T11:41:38.479483Z"))

        assertThat(repo.data["someMemberId"]!!.activationDateTriggerAt).isNull()
        verify(inverse = true) {
            jobScheduler.rescheduleOrTriggerContractActivatedToday(any(), any(), any())
        }
    }
}
