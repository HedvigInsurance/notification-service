package com.hedvig.notificationService.customerio.state

import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class QuartzMigrator(
    private val repo: CustomerIOStateRepository,
    private val jobScheduler: JobScheduler,
    private val contractLoader: ContractLoader
) {
    fun migrate(now: Instant) {
        val statesToMigrate = repo.statesWithTriggers()

        for (state in statesToMigrate) {
            state.sentActivatesTodayEvent(null)
            repo.save(state)

            for (contract in contractLoader.getContractInfoForMember(state.memberId)) {
                if (contract.startDate != null && contract.startDate.isAfter(
                        now.truncatedTo(ChronoUnit.DAYS).atZone(ZoneId.of("Europe/Stockholm")).toLocalDate()
                    )
                ) {
                    jobScheduler.rescheduleOrTriggerContractActivatedToday(
                        activationDate = contract.startDate,
                        memberId = state.memberId,
                        contractId = contract.contractId.toString()
                    )
                }
            }
        }
    }
}
