package com.hedvig.notificationService.customerio.state

import java.time.Instant
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class CustomerioState(
    @Id
    val memberId: String,
    val underwriterFirstSignAttributesUpdate: Instant?,
    val sentTmpSignEvent: Boolean = false,
    val contractCreatedAt: Instant? = null,
    val startDateUpdatedAt: Instant? = null,
    val activateFirstContractAt: LocalDate? = null
) {
    fun shouldSendTmpSignedEvent(): Boolean = underwriterFirstSignAttributesUpdate != null
    fun sentTmpSignedEvent(): CustomerioState = copy(sentTmpSignEvent = true)

    fun shouldSendContractCreatedEvent(): Boolean = contractCreatedAt != null
    fun sentContractCreatedEvent(): CustomerioState = copy(contractCreatedAt = null)

    fun shouldSendStartDateUpdatedEvent(): Boolean = startDateUpdatedAt != null
    fun sentStartDateUpdatedEvent(): CustomerioState = copy(startDateUpdatedAt = null)
    fun shouldSendActivatesTodayEvent(): Boolean = activateFirstContractAt != null
}
