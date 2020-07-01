package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.service.FirebaseNotificationService
import com.hedvig.notificationService.serviceIntegration.memberService.dto.MemberHasSignedBeforeRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class EventHandler(
    private val repo: CustomerIOStateRepository,
    private val configuration: ConfigurationProperties,
    private val clients: Map<Workspace, CustomerioClient>,
    private val firebaseNotificationService: FirebaseNotificationService,
    private val workspaceSelector: WorkspaceSelector,
    private val memberService: MemberServiceImpl,
    private val customerioService: CustomerioService
) {
    fun onStartDateUpdatedEvent(
        event: StartDateUpdatedEvent,
        callTime: Instant = Instant.now()
    ) {
        val state = repo.findByMemberId(event.owningMemberId)
            ?: CustomerioState(event.owningMemberId)

        if (!configuration.useNorwayHack) {
            state.triggerStartDateUpdated(callTime)
            state.updateFirstUpcomingStartDate(event.startDate)
            repo.save(state)
        }
    }

    fun onContractCreatedEvent(contractCreatedEvent: ContractCreatedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(contractCreatedEvent.owningMemberId)
            ?: CustomerioState(contractCreatedEvent.owningMemberId)

        if (state.underwriterFirstSignAttributesUpdate != null)
            return // This should only happen when we go live or if we rollback to earlier versions

        if (!configuration.useNorwayHack) {
            state.createContract(contractCreatedEvent.contractId, callTime, contractCreatedEvent.startDate)
            repo.save(state)
        }
    }

    fun onFailedChargeEvent(memberId: String, chargeFailedEvent: ChargeFailedEvent) {
        val marketForMember = workspaceSelector.getWorkspaceForMember(memberId)
        clients[marketForMember]?.sendEvent(memberId, chargeFailedEvent.toMap(memberId))

        try {
            if (chargeFailedEvent.terminationDate != null) {
                firebaseNotificationService.sendTerminatedFailedChargesNotification(memberId)
                return
            }

            when (chargeFailedEvent.chargeFailedReason) {
                ChargeFailedReason.INSUFFICIENT_FUNDS -> firebaseNotificationService.sendPaymentFailedNotification(
                    memberId
                )
                ChargeFailedReason.NOT_CONNECTED_DIRECT_DEBIT -> firebaseNotificationService.sendConnectDirectDebitNotification(
                    memberId
                )
            }
        } catch (e: Exception) {
            logger.error("onFailedChargeEvent - Can not send notification for $memberId - Exception: ${e.message}")
        }
    }

    fun onContractRenewalQueued(event: ContractRenewalQueuedEvent, callTime: Instant = Instant.now()) {
        val state = repo.findByMemberId(event.memberId)
            ?: CustomerioState(event.memberId)

        state.queueContractRenewal(event.contractId, callTime)
        repo.save(state)
    }

    fun onQuoteCreated(memberId: String, event: QuoteCreatedEvent, callTime: Instant = Instant.now()) {
        if (!event.shouldSend()) return
        val hasSignedBefore = memberService.hasSignedBefore(
            MemberHasSignedBeforeRequest(
                memberId = memberId,
                ssn = event.ssn,
                email = event.email
            )
        )
        if (hasSignedBefore) return
        customerioService.updateCustomerAttributes(memberId, mapOf("email" to event.email), callTime)
        customerioService.sendEvent(memberId, event.toMap(memberId))
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)!!
    }
}
