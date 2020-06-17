package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace
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
                startDate = null,
                signSource = "IOS",
                partnerCode = "HEDVIG",
                contractId = UUID.randomUUID()
            )
        )
    }
}
