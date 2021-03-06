package com.hedvig.notificationService.customerio.state

import com.hedvig.notificationService.customerio.ContractInfo
import java.time.Instant
import java.time.LocalDate

class CustomerioState(
    val memberId: String,
    underwriterFirstSignAttributesUpdate: Instant? = null,
    sentTmpSignEvent: Boolean = false,
    contractCreatedTriggerAt: Instant? = null,
    startDateUpdatedTriggerAt: Instant? = null,
    activationDateTriggerAt: LocalDate? = null,
    var contracts: MutableList<ContractState> = mutableListOf()
) {
    var underwriterFirstSignAttributesUpdate: Instant? = underwriterFirstSignAttributesUpdate
        private set
    var sentTmpSignEvent: Boolean = sentTmpSignEvent
        private set
    var contractCreatedTriggerAt: Instant? = contractCreatedTriggerAt
        private set
    var startDateUpdatedTriggerAt: Instant? = startDateUpdatedTriggerAt
        private set
    var activationDateTriggerAt: LocalDate? = activationDateTriggerAt
        private set

    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null && !sentTmpSignEvent
    fun sentTmpSignedEvent() {
        this.sentTmpSignEvent = true
    }

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedTriggerAt != null
    fun sentContractCreatedEvent() {
        this.contractCreatedTriggerAt = null
    }

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedTriggerAt != null
    fun sentStartDateUpdatedEvent() {
        this.startDateUpdatedTriggerAt = null
    }

    fun shouldSendActivatesTodayEvent(): Boolean = activationDateTriggerAt != null
    fun sentActivatesTodayEvent(nextActivationDate: LocalDate?) {
        this.activationDateTriggerAt = nextActivationDate
    }

    fun updateFirstUpcomingStartDate(newDate: LocalDate?) {
        val newFirstUpcomingStartDate =
            if (newDate == null || (activationDateTriggerAt != null && newDate.isAfter(activationDateTriggerAt))) {
                activationDateTriggerAt
            } else {
                newDate
            }
        this.activationDateTriggerAt = newFirstUpcomingStartDate
    }

    fun updateFirstUpcomingStartDate(contracts: List<ContractInfo>) {
        for (contract in contracts) {
            this.updateFirstUpcomingStartDate(contract.startDate)
        }
    }

    fun createContract(contractId: String, calltime: Instant, startDate: LocalDate?) {
        if (this.contracts.none { it.contractId == contractId }) {
            this.contracts.add(ContractState(contractId))
        }
    }
}
