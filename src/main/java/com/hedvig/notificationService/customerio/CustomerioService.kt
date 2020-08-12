package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.IdempotenceHashRepository
import okhttp3.internal.toHexString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import javax.transaction.Transactional

@Service
class CustomerioService(
    private val workspaceSelector: WorkspaceSelector,
    private val stateRepository: CustomerIOStateRepository,
    private val clients: Map<Workspace, CustomerioClient>,
    private val idempotenceHashRepository: IdempotenceHashRepository
) {

    private val logger = LoggerFactory.getLogger(CustomerioService::class.java)

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
        attributes: Map<String, Any?>,
        now: Instant = Instant.now()
    ) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        if (marketForMember == Workspace.NORWAY && isSignUpdateFromUnderwriter(attributes)) {
            return
        }

        clients[marketForMember]?.updateCustomer(memberId, attributes)
    }

    private fun isSignUpdateFromUnderwriter(attributes: Map<String, Any?>): Boolean {
        return attributes.containsKey("partner_code") ||
            attributes.containsKey("switcher_company") ||
            attributes.containsKey("sign_source")
    }

    fun deleteCustomer(memberId: String) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        clients[marketForMember]?.deleteCustomer(memberId)
    }

    fun sendEvent(memberId: String, body: Map<String, Any?>) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)
        val mutableMap = body.toMutableMap()
        val hash = body.hashCode().toHexString()
        mutableMap["hash"] = hash
        clients[marketForMember]?.sendEvent(memberId, mutableMap.toMap())
        idempotenceHashRepository.save(memberId, hash)
    }

    @Transactional
    fun sendEventAndUpdateState(
        customerioState: CustomerioState,
        event: Map<String, Any?>
    ) {
        try {
            logger.info("Sending event ${event["name"]} to member ${customerioState.memberId}")
            this.stateRepository.save(customerioState)
            sendEvent(customerioState.memberId, event)
        } catch (ex: RuntimeException) {
            logger.error("Could not send event to customerio", ex)
        }
    }

    @Transactional
    fun doUpdate(
        customerioState: CustomerioState,
        eventCreator: CustomerioEventCreator,
        contractLoader: ContractLoader
    ) {
        try {
        } catch (ex: RuntimeException) {
            logger.error("Could not create event from customerio state", ex)
        }
    }

    @Transactional
    fun removeIdempotenceHash(memberId: String, hash: String) {
        try {
            val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

            clients[marketForMember]?.sendEvent(
                memberId, mapOf(
                    "name" to "RemoveIdempotenceHash",
                    "data" to mapOf(
                        "hash" to hash
                    )
                )
            )
            idempotenceHashRepository.delete(memberId, hash)
        } catch (ex: RuntimeException) {
            logger.error("Could not remove idempotence hash: $hash, memberId: $memberId", ex)
        }
    }
}
