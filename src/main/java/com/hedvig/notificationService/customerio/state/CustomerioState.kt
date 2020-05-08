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
        contractCreatedAt: Instant? = this.contractCreatedTriggerAt,
        startDateUpdatedAt: Instant? = this.startDateUpdatedTriggerAt,
        firstUpcomingStartDate: LocalDate? = this.activationDateTriggerAt
    ): CustomerioState {
        return CustomerioState(
            memberId = this.memberId,
            underwriterFirstSignAttributesUpdate = underwriterFirstSignAttributesUpdate,
            sentTmpSignEvent = sentTmpSignEvent,
            contractCreatedTriggerAt = contractCreatedAt,
            startDateUpdatedTriggerAt = startDateUpdatedAt,
            activationDateTriggerAt = firstUpcomingStartDate
        )
    }

    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null
    fun sentTmpSignedEvent(): CustomerioState = copy(sentTmpSignEvent = true)

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedTriggerAt != null
    fun sentContractCreatedEvent(): CustomerioState = copy(contractCreatedAt = null)

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedTriggerAt != null
    fun sentStartDateUpdatedEvent(): CustomerioState = copy(startDateUpdatedAt = null)

    fun shouldSendActivatesTodayEvent(): Boolean = activationDateTriggerAt != null
    fun sentActivatesTodayEvent(nextActivationDate: LocalDate?): CustomerioState =
        copy(firstUpcomingStartDate = nextActivationDate)

    fun updateFirstUpcomingStartDate(newDate: LocalDate?): CustomerioState {
        val newFirstUpcomingStartDate =
            if (newDate == null || (activationDateTriggerAt != null && newDate.isAfter(activationDateTriggerAt))) {
                activationDateTriggerAt
            } else {
                newDate
            }
        return copy(firstUpcomingStartDate = newFirstUpcomingStartDate)
    }

    fun updateFirstUpcomingStartDate(contracts: List<ContractInfo>): CustomerioState {
        return contracts.foldRight(this) { contract, state -> state.updateFirstUpcomingStartDate(contract.startDate) }
    }
}
