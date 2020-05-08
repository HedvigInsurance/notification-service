package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.Workspace

interface ProductPricingFacade {
    fun getWorkspaceForMember(memberId: String): Workspace
    fun getContractTypeForMember(memberId: String): List<ContractInfo>
}
