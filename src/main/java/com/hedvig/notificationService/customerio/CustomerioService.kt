package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient

class CustomerioService(
    productPricingFacade: ProductPricingFacade,
    memberServiceImpl: MemberServiceImpl,
    vararg clients: Pair<Workspace, CustomerioClient>
) {
    private val workspaceSelector = WorkspaceSelector(productPricingFacade, memberServiceImpl)

    private val clients = mapOf(*clients)

    init {
        if (this.clients.isEmpty()) {
            throw IllegalArgumentException("You must provide workspaces")
        }

        Workspace.values().map {
            if (it.requiresImplementation && this.clients[it] == null) {
                throw IllegalArgumentException("You must provide a customer.io client for workspace $it")
            }
        }
    }

    fun updateCustomerAttributes(memberId: String, convertValue: Map<String, Any?>) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        clients[marketForMember]?.updateCustomer(memberId, convertValue)
    }

    fun deleteCustomer(memberId: String) {
        clients[Workspace.SWEDEN]?.deleteCustomer(memberId)
    }
}
