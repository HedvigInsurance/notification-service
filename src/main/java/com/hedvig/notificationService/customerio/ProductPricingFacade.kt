package com.hedvig.notificationService.customerio

interface ProductPricingFacade {
    fun getWorkspaceForMember(memberId: String): Workspace
}
