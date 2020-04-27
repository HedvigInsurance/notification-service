package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import java.time.Instant

open class EventHandler(val repo: CustomerIOStateRepository) {
    open fun startDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant
    ) {
        repo.save(CustomerioState(event.owningMemberId, null, false, null, startDateUpdatedAt = callTime))
    }
}
