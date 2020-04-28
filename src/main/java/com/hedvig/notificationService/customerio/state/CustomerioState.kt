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
)
