package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
import java.time.LocalDate
import java.util.UUID

class FakeContractLoader :
    ContractLoader {
    override fun getWorkspaceForMember(memberId: String): Workspace {
        return Workspace.NORWAY
    }

    override fun getContractInfoForMember(memberId: String): List<ContractInfo> {
        return listOf(
            ContractInfo(
                type = AgreementType.NorwegianHomeContent,
                switcherCompany = null,
                startDate = LocalDate.now(),
                signSource = "IOS",
                partnerCode = "HEDVIG",
                renewalDate = LocalDate.of(2020, 6, 30),
                contractId = UUID.fromString("75868246-b0a3-11ea-8443-3af9d3902f96"),
                terminationDate = null
            )
        )
    }
}
