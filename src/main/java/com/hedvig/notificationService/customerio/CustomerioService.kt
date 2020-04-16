package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import java.time.Instant
import java.time.temporal.ChronoUnit

const val SIGN_EVENT_WINDOWS_SIZE_MINUTES = 5L

class CustomerioService(
    private val workspaceSelector: WorkspaceSelector,
    private val stateRespository: CustomerIOStateRepository,
    vararg clients: Pair<Workspace, CustomerioClient>
) {

    private val clients = mapOf(*clients)

    private var memberSignStartsAt = mapOf<String, Instant>()

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
                stateRespository.save(CustomerioState(memberId, now))
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

    fun sendUpdates(timeNow: Instant = Instant.now()) {
        for (customerioState in this.stateRespository.allMembers()) {
            if (timeNow >= customerioState.underwriterSignAttributesStarted.plus(
                    SIGN_EVENT_WINDOWS_SIZE_MINUTES,
                    ChronoUnit.MINUTES
                )
            ) {
                clients[Workspace.NORWAY]?.sendEvent(
                    customerioState.memberId,
                    mapOf("name" to "TmpSignedInsuranceEvent")
                )
            }
        }
    }
}
