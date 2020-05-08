package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace

class FakeProductPricingFacade :
    ProductPricingFacade {
    override fun getWorkspaceForMember(memberId: String): Workspace {
        return Workspace.NORWAY
    }

    override fun getContractTypeForMember(memberId: String): List<ContractInfo> {
        return listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )
    }
}
