package com.hedvig.notificationService.customerio

interface ProductPricingFacade {
    fun getWorkspaceForMember(memberId: String): Workspace
    fun getContractTypeForMember(memberId: String): List<ContractInfo>
}
