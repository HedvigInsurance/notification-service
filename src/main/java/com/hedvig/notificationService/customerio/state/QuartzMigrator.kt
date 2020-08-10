package com.hedvig.notificationService.customerio.state

import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

@Service
class QuartzMigrator(
    private val repo: CustomerIOStateRepository,
    private val jobScheduler: JobScheduler,
    private val contractLoader: ContractLoader
) {
    @Transactional
    fun migrate(now: Instant) {
        val statesToMigrate = repo.statesWithTriggers()

        for (state in statesToMigrate) {
            val contracts = contractLoader.getContractInfoForMember(state.memberId)

            if (state.startDateUpdatedTriggerAt != null) {

                jobScheduler.rescheduleOrTriggerStartDateUpdated(
                    now, state.memberId
                )
                state.sentStartDateUpdatedEvent()
            }

            if (state.contractCreatedTriggerAt != null) {
                jobScheduler.rescheduleOrTriggerContractCreated(
                    now, state.memberId
                )
                state.sentContractCreatedEvent()
            }

            if (state.activationDateTriggerAt != null) {
                for (contract in contracts) {
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
                state.sentActivatesTodayEvent(null)
            }

            repo.save(state)
        }
    }
}
