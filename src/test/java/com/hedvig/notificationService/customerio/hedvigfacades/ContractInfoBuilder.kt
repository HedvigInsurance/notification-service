package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import java.time.LocalDate
import java.util.UUID

fun makeContractInfo(
    agreementType: AgreementType = AgreementType.NorwegianHomeContent,
    switcherCompany: String? = null,
    startDate: LocalDate? = null,
    signSource: String = "IOS",
    partnerCode: String = "HEDVIG",
    contractId: UUID = UUID.randomUUID()
): ContractInfo {
    return ContractInfo(
        agreementType,
        switcherCompany = switcherCompany,
        startDate = startDate,
        signSource = signSource,
        partnerCode = partnerCode,
        contractId = contractId
    )
}
