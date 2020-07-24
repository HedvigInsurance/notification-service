package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import java.time.Instant
import java.time.temporal.ChronoUnit

open class CustomerioUpdateScheduler(
    private val eventCreator: CustomerioEventCreator,
    private val stateRepository: CustomerIOStateRepository,
    private val contractLoader: ContractLoader,
    private val customerioService: CustomerioService
) {

    private val logger =
        LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    // @Scheduled functions cannot have any arguments
    // so this is a bit of a hack
    @Scheduled(fixedDelay = 1000 * 30)
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
                customerioService.sendEventAndUpdateState(customerioState, eventAndState.asMap)
            } catch (ex: RuntimeException) {
                logger.error("Could not create event from customerio state", ex)
            }
        }
    }
}
