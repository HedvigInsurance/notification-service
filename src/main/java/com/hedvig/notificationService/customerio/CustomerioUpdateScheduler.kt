package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.Instant
import java.time.temporal.ChronoUnit

const val SIGN_EVENT_WINDOWS_SIZE_MINUTES = 10L

@DisallowConcurrentExecution
open class CustomerioUpdateScheduler(
    private val eventCreator: CustomerioEventCreator,
    private val stateRepository: CustomerIOStateRepository,
    private val contractLoader: ContractLoader,
    private val customerioService: CustomerioService
) : QuartzJobBean() {

    private val logger =
        LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    fun sendUpdates(timeNow: Instant = Instant.now()) {
        val windowEndTime = timeNow.minus(
            SIGN_EVENT_WINDOWS_SIZE_MINUTES,
            ChronoUnit.MINUTES
        )

        for (customerioState in this.stateRepository.shouldUpdate(windowEndTime)) {
            logger.info("Running update for ${customerioState.memberId}")
            customerioService.doUpdate(customerioState, eventCreator, contractLoader)
        }
    }

    override fun executeInternal(context: JobExecutionContext) {
        sendUpdates()
    }
}
