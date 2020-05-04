package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(val repo: CustomerIOStateRepository) {
    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {
        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(
                event.owningMemberId,
                startDateUpdatedTriggerAt = callTime
            )

        repo.save(state.updateFirstUpcomingStartDate(event.startDate))
    }

    fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId) ?: CustomerioState(
            contractCreatedEvent.owningMemberId,
            contractCreatedTriggerAt = callTime
        )

        repo.save(state.updateFirstUpcomingStartDate(contractCreatedEvent.startDate))
    }
}
