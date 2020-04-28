package com.hedvig.notificationService.customerio.events

import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
data class Contract(
    val id: String,
    val type: String,
    val switcherCompany: String
)
