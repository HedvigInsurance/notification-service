package com.hedvig.notificationService.customerio.events

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Contract(
    val type: String,
    val switcherCompany: String?,
    val startDate: LocalDate?
)
