package com.hedvig.notificationService.customerio

class FakeProductPricingFacade : ProductPricingFacade {
    override fun getWorkspaceForMember(memberId: String): Workspace {
        return Workspace.SWEDEN
    }

    override fun getContractTypeForMember(memberId: String): List<ContractInfo> {
        return listOf(ContractInfo(AgreementType.NorwegianHomeContent, null, null))
    }
}
