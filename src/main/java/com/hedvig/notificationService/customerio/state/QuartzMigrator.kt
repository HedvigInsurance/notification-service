package com.hedvig.notificationService.customerio.state

import com.hedvig.notificationService.customerio.customerioEvents.jobs.JobScheduler
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import datadog.trace.api.Trace
import org.slf4j.LoggerFactory
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
    val log = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun migrate(now: Instant) {
        val statesToMigrate = repo.statesWithTriggers()

        for (state in statesToMigrate) {
            migrateMember(state, now)

            repo.save(state)
        }
    }

    @Trace
    private fun migrateMember(
        state: CustomerioState,
        now: Instant
    ) {
        log.info("Migrating ${state.memberId}")
        val contracts = contractLoader.getContractInfoForMember(state.memberId)

        if (state.startDateUpdatedTriggerAt != null) {
            log.info("Migrating startDateUpdatedTriggerAt for member ${state.memberId}")
            jobScheduler.rescheduleOrTriggerStartDateUpdated(
                now, state.memberId
            )
            state.sentStartDateUpdatedEvent()
        }

        if (state.contractCreatedTriggerAt != null) {
            log.info("Migrating rescheduleOrTriggerContractCreated for member ${state.memberId}")
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
                    log.info("Migrating startDate for member ${state.memberId} and contract ${contract.contractId}")
                    jobScheduler.rescheduleOrTriggerContractActivatedToday(
                        activationDate = contract.startDate,
                        memberId = state.memberId,
                        contractId = contract.contractId.toString()
                    )
                }
            }
            state.sentActivatesTodayEvent(null)
        }
    }
}
