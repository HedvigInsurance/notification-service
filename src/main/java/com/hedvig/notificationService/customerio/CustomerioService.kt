package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import java.time.Instant

class CustomerioService(
    private val workspaceSelector: WorkspaceSelector,
    vararg clients: Pair<Workspace, CustomerioClient>
) {
    constructor(
        productPricingFacade: ProductPricingFacade,
        memberServiceImpl: MemberServiceImpl,
        vararg clients: Pair<Workspace, CustomerioClient>
    ) : this(WorkspaceSelector(productPricingFacade, memberServiceImpl), *clients)

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

    fun updateCustomerAttributes(
        memberId: String,
        convertValue: Map<String, Any?>,
        now: Instant = Instant.now()
    ) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        if (marketForMember == Workspace.NORWAY) {
            if (convertValue.containsKey("partner_code") ||
                convertValue.containsKey("switcher_company") ||
                convertValue.containsKey("sign_source")
            ) {
                return
            }
        }

        clients[marketForMember]?.updateCustomer(memberId, convertValue)
    }

    fun deleteCustomer(memberId: String) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        clients[marketForMember]?.deleteCustomer(memberId)
    }

    fun sendEvent(memberId: String, body: Map<String, Any?>) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)
        clients[marketForMember]?.sendEvent(memberId, body)
    }

    fun sendUpdates(plus: Instant = Instant.now()) {
        clients[Workspace.NORWAY]?.sendEvent("1337", mapOf("name" to "TmpSignedInsuranceEvent"))
    }
}
