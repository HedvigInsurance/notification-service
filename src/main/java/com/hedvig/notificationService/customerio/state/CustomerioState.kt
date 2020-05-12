package com.hedvig.notificationService.customerio.state

import com.hedvig.notificationService.customerio.ContractInfo
import java.time.Instant
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class CustomerioState(
    @Id
    val memberId: String,
    val underwriterFirstSignAttributesUpdate: Instant? = null,
    val sentTmpSignEvent: Boolean = false,
    val contractCreatedTriggerAt: Instant? = null,
    val startDateUpdatedTriggerAt: Instant? = null,
    val activationDateTriggerAt: LocalDate? = null
) {
    private fun copy(
        underwriterFirstSignAttributesUpdate: Instant? = this.underwriterFirstSignAttributesUpdate,
        sentTmpSignEvent: Boolean = this.sentTmpSignEvent,
        contractCreatedTriggerAt: Instant? = this.contractCreatedTriggerAt,
        startDateUpdatedTriggerAt: Instant? = this.startDateUpdatedTriggerAt,
        activationDateTriggerAt: LocalDate? = this.activationDateTriggerAt
    ): CustomerioState {
        return CustomerioState(
            memberId = this.memberId,
            underwriterFirstSignAttributesUpdate = underwriterFirstSignAttributesUpdate,
            sentTmpSignEvent = sentTmpSignEvent,
            contractCreatedTriggerAt = contractCreatedTriggerAt,
            startDateUpdatedTriggerAt = startDateUpdatedTriggerAt,
            activationDateTriggerAt = activationDateTriggerAt
        )
    }

    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null
    fun sentTmpSignedEvent(): CustomerioState = copy(sentTmpSignEvent = true)

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedTriggerAt != null
    fun sentContractCreatedEvent(): CustomerioState = copy(contractCreatedTriggerAt = null)

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedTriggerAt != null
    fun sentStartDateUpdatedEvent(): CustomerioState = copy(startDateUpdatedTriggerAt = null)

    fun shouldSendActivatesTodayEvent(): Boolean = activationDateTriggerAt != null
    fun sentActivatesTodayEvent(nextActivationDate: LocalDate?): CustomerioState =
        copy(activationDateTriggerAt = nextActivationDate)

    fun updateFirstUpcomingStartDate(newDate: LocalDate?): CustomerioState {
        val newFirstUpcomingStartDate =
            if (newDate == null || (activationDateTriggerAt != null && newDate.isAfter(activationDateTriggerAt))) {
                activationDateTriggerAt
            } else {
                newDate
            }
        return copy(activationDateTriggerAt = newFirstUpcomingStartDate)
    }

    fun updateFirstUpcomingStartDate(contracts: List<ContractInfo>): CustomerioState {
        return contracts.foldRight(this) { contract, state -> state.updateFirstUpcomingStartDate(contract.startDate) }
    }

    fun triggerStartDateUpdated(callTime: Instant): CustomerioState {
        return this.copy(startDateUpdatedTriggerAt = callTime)
    }

    fun triggerContractCreated(callTime: Instant): CustomerioState {
        return this.copy(contractCreatedTriggerAt = callTime)
    }
}
