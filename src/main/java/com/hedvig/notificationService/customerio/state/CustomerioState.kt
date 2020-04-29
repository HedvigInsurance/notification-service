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
    val contractCreatedAt: Instant? = null,
    val startDateUpdatedAt: Instant? = null,
    val firstUpcomingStartDate: LocalDate? = null
) {
    private fun copy(
        underwriterFirstSignAttributesUpdate: Instant? = this.underwriterFirstSignAttributesUpdate,
        sentTmpSignEvent: Boolean = this.sentTmpSignEvent,
        contractCreatedAt: Instant? = this.contractCreatedAt,
        startDateUpdatedAt: Instant? = this.startDateUpdatedAt,
        firstUpcomingStartDate: LocalDate? = this.firstUpcomingStartDate
    ): CustomerioState {
        return CustomerioState(
            memberId = this.memberId,
            underwriterFirstSignAttributesUpdate = underwriterFirstSignAttributesUpdate,
            sentTmpSignEvent = sentTmpSignEvent,
            contractCreatedAt = contractCreatedAt,
            startDateUpdatedAt = startDateUpdatedAt,
            firstUpcomingStartDate = firstUpcomingStartDate
        )
    }

    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null
    fun sentTmpSignedEvent(): CustomerioState = copy(sentTmpSignEvent = true)

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedAt != null
    fun sentContractCreatedEvent(): CustomerioState = copy(contractCreatedAt = null)

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedAt != null
    fun sentStartDateUpdatedEvent(): CustomerioState = copy(startDateUpdatedAt = null)

    fun shouldSendActivatesTodayEvent(): Boolean = firstUpcomingStartDate != null
    fun sentActivatesTodayEvent(nextActivationDate: LocalDate?): CustomerioState =
        copy(firstUpcomingStartDate = nextActivationDate)

    fun updateFirstUpcomingStartDate(date: LocalDate?): CustomerioState {
        val newDate = if (date == null || (firstUpcomingStartDate != null && date.isAfter(firstUpcomingStartDate))) {
            firstUpcomingStartDate
        } else {
            date
        }
        return copy(firstUpcomingStartDate = newDate)
    }

    fun updateFirstUpcomingStartDate(contracts: List<ContractInfo>): CustomerioState {
        return contracts.foldRight(this) { contract, state -> state.updateFirstUpcomingStartDate(contract.startDate) }
    }
}
