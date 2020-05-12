package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.hedvig.notificationService.customerio.ContractInfo

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy::class)
@JsonInclude(Include.NON_NULL)
data class Contract(
    val type: String,
    val switcherCompany: String?,
    val startDate: String?
) {
    companion object {
        fun from(contractInfo: ContractInfo): Contract = Contract(
            contractInfo.type.typeName,
            contractInfo.switcherCompany,
            contractInfo.startDate?.toString()
        )
    }
}
