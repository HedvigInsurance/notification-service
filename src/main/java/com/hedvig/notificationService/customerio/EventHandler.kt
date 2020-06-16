package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
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
    val state = repo.findByMemberId(event.owningMemberId)
      ?: CustomerioState(event.owningMemberId)

    if (!configuration.useNorwayHack) {
      state.triggerStartDateUpdated(callTime)
      state.updateFirstUpcomingStartDate(event.startDate)
      repo.save(state)
    }
  }

  fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
    val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
      ?: CustomerioState(contractCreatedEvent.owningMemberId)

    if (state.underwriterFirstSignAttributesUpdate != null)
      return // This should only happen when we go live or if we rollback to earlier versions

    if (!configuration.useNorwayHack) {
      state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
      repo.save(state)
    }
  }

  fun onContractRenewalQueued(event: ContractRenewalQueuedEvent, callTime: Instant = Instant.now()) {
    val state = repo.findByMemberId(event.memberId)
      ?: throw IllegalStateException("Could not find customerio state for memberId ${event.memberId} while handling ${event.javaClass.simpleName}")

    state.queueContractRenewal(event.contractId, callTime)
    repo.save(state)
  }
}
