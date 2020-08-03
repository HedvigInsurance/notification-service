package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

const val SIGN_EVENT_WINDOWS_SIZE_MINUTES = 10L

open class CustomerioUpdateScheduler: Job {

    //TODO: This are still null :(
    lateinit var eventCreator: CustomerioEventCreator
    lateinit var stateRepository: CustomerIOStateRepository
    lateinit var contractLoader: ContractLoader
    lateinit var customerioService: CustomerioService

    private val logger =
        LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    constructor()

    constructor(
        eventCreator: CustomerioEventCreator,
        stateRepository: CustomerIOStateRepository,
        contractLoader: ContractLoader,
        customerioService: CustomerioService
    ) {
        this.eventCreator = eventCreator
        this.stateRepository = stateRepository
        this.contractLoader = contractLoader
        this.customerioService = customerioService
    }

    fun sendUpdates(timeNow: Instant = Instant.now()) {
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

    override fun execute(context: JobExecutionContext) {
        sendUpdates()
    }
}
