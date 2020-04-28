package com.hedvig.notificationService.customerio.state

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
    val activateFirstContractAt: LocalDate? = null
) {
    private fun copy(
        underwriterFirstSignAttributesUpdate: Instant? = this.underwriterFirstSignAttributesUpdate,
        sentTmpSignEvent: Boolean = this.sentTmpSignEvent,
        contractCreatedAt: Instant? = this.contractCreatedAt,
        startDateUpdatedAt: Instant? = this.startDateUpdatedAt,
        activateFirstContractAt: LocalDate? = this.activateFirstContractAt
    ): CustomerioState {
        return CustomerioState(
            memberId = this.memberId,
            underwriterFirstSignAttributesUpdate = underwriterFirstSignAttributesUpdate,
            sentTmpSignEvent = sentTmpSignEvent,
            contractCreatedAt = contractCreatedAt,
            startDateUpdatedAt = startDateUpdatedAt,
            activateFirstContractAt = activateFirstContractAt
        )
    }

    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null
    fun sentTmpSignedEvent(): CustomerioState = copy(sentTmpSignEvent = true)

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedAt != null
    fun sentContractCreatedEvent(): CustomerioState = copy(contractCreatedAt = null)

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedAt != null
    fun sentStartDateUpdatedEvent(): CustomerioState = copy(startDateUpdatedAt = null)

    fun shouldSendActivatesTodayEvent(): Boolean = activateFirstContractAt != null
    fun sentActivatesTodayEvent(nextActivationDate: LocalDate?): CustomerioState =
        copy(activateFirstContractAt = nextActivationDate)
}
