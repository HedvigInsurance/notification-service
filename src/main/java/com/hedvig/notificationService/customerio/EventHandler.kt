package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val configuration: ConfigurationProperties
) {
    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {
        var state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        state = state.triggerStartDateUpdated(callTime)

        if (!configuration.useNorwayHack)
            repo.save(state.updateFirstUpcomingStartDate(event.startDate))
    }

    fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId) ?: CustomerioState(
            contractCreatedEvent.owningMemberId,
            contractCreatedTriggerAt = callTime
        )

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        if (!configuration.useNorwayHack)
            repo.save(state.updateFirstUpcomingStartDate(contractCreatedEvent.startDate))
    }
}
