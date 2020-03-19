package com.hedvig.notificationService.customerio

class FakeProductPricingFacade : ProductPricingFacade {
    override fun getWorkspaceForMember(memberId: String): Workspace {
        return Workspace.SWEDEN
    }
}
