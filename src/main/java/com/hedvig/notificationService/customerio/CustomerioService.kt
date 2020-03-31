package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient

class CustomerioService(
    private val productPricingFacade: ProductPricingFacade,
    private val memberServiceImpl: MemberServiceImpl,
    vararg clients: Pair<Workspace, CustomerioClient>
) {

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
        var marketForMember = productPricingFacade.getWorkspaceForMember(memberId)
        if (marketForMember == Workspace.NOT_FOUND) {
            val pickedLocale = memberServiceImpl.getPickedLocale(memberId)

            marketForMember =
                Workspace.getWorkspaceFromLocale(pickedLocale)
            if (marketForMember == Workspace.NOT_FOUND)
                throw RuntimeException("Retrived unsupported locale from member-service: $pickedLocale")
        }

        clients[marketForMember]?.updateCustomer(memberId, convertValue)
    }

    fun deleteCustomer(memberId: String) {
        clients[Workspace.SWEDEN]?.deleteCustomer(memberId)
    }
}
