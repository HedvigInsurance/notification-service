package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.transaction.Transactional

const val SIGN_EVENT_WINDOWS_SIZE_MINUTES = 10L

open class CustomerioService(
    private val workspaceSelector: WorkspaceSelector,
    private val stateRepository: CustomerIOStateRepository,
    private val eventCreator: CustomerioEventCreator,
    private val clients: Map<Workspace, CustomerioClient>,
    private val contractLoader: ContractLoader,
    private val useNorwayHack: Boolean
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

    open fun updateCustomerAttributes(
        memberId: String,
        attributes: Map<String, Any?>,
        now: Instant = Instant.now()
    ) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        if (marketForMember == Workspace.NORWAY && isSignUpdateFromUnderwriter(attributes)) {
            if (this.useNorwayHack) {
                val customerState = stateRepository.findByMemberId(memberId)
                if (customerState == null) {
                    stateRepository.save(
                        CustomerioState(
                            memberId,
                            now
                        )
                    )
                }
            }
            return
        }

        clients[marketForMember]?.updateCustomer(memberId, attributes)
    }

    private fun isSignUpdateFromUnderwriter(attributes: Map<String, Any?>): Boolean {
        return attributes.containsKey("partner_code") ||
            attributes.containsKey("switcher_company") ||
            attributes.containsKey("sign_source")
    }

    open fun deleteCustomer(memberId: String) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)

        clients[marketForMember]?.deleteCustomer(memberId)
    }

    open fun sendEvent(memberId: String, body: Map<String, Any?>) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)
        clients[marketForMember]?.sendEvent(memberId, body)
    }

    // @Scheduled functions cannot have any arguments
    // so this is a bit of a hack
    @Scheduled(fixedDelay = 1000 * 30)
    @Transactional
    open fun scheduledUpdates() {
        sendUpdates()
    }

    open fun sendUpdates(timeNow: Instant = Instant.now()) {

        val windowEndTime = timeNow.minus(
            SIGN_EVENT_WINDOWS_SIZE_MINUTES,
            ChronoUnit.MINUTES
        )

        for (customerioState in this.stateRepository.shouldUpdate(windowEndTime)) {
            logger.info("Running update for ${customerioState.memberId}")
            try {
                val contracts = this.contractLoader.getContractInfoForMember(customerioState.memberId)
                val eventAndState = eventCreator.execute(customerioState, contracts)
                sendEventAndUpdateState(customerioState, eventAndState.asMap)
            } catch (ex: RuntimeException) {
                logger.error("Could not create event from customerio state", ex)
            }
        }
    }

    private fun sendEventAndUpdateState(
        customerioState: CustomerioState,
        event: Map<String, Any?>
    ) {
        try {
            logger.info("Sending event ${event["name"]} to member ${customerioState.memberId}")
            this.stateRepository.save(customerioState)
            val workspace = workspaceSelector.getWorkspaceForMember(customerioState.memberId)
            clients[workspace]?.sendEvent(
                customerioState.memberId,
                event
            )
        } catch (ex: RuntimeException) {
            logger.error("Could not send event to customerio", ex)
        }
    }
}
