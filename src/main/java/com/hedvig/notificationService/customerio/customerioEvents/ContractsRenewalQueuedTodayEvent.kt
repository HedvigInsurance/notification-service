package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.time.LocalDate

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "name", visible = true)
data class ContractsRenewalQueuedTodayEvent(val data: DataObject) {

    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
    data class DataObject(val renewalDate: LocalDate, val type: String)
}
