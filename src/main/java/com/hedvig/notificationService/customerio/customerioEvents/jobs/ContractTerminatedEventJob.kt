package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.common.quartz.executeWithRetry
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.CustomerioUpdateScheduler
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean

class ContractTerminatedEventJob(
    private val customerIOStateRepository: CustomerIOStateRepository,
    private val contractLoader: ContractLoader,
    private val eventCreator: CustomerioEventCreator,
    private val customerioService: CustomerioService
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    override fun executeInternal(jobContext: JobExecutionContext) {
        executeWithRetry(jobContext,
            {
                logger.error("Job ${this::class.simpleName} could not be completed", it)
            }
        ) {
            val memberId = jobContext.mergedJobDataMap.get("memberId") as String

            logger.info("Running ContractTerminatedEventJob.executeInternal with member $memberId")

            val customerioState = customerIOStateRepository.findByMemberId(memberId)!!
            val contracts = contractLoader.getContractInfoForMember(customerioState.memberId)

            val contractsAsString = jobContext.mergedJobDataMap.getString("contracts")
                ?: throw NullPointerException("Found no contracts in ContractTerminatedEventJob for member $memberId")

            val terminatedContracts = contractsAsString.split(",")
                .filter { it.isNotEmpty() }

            val event = eventCreator.contractsTerminatedEvent(contracts, terminatedContracts)

            if (event != null) {
                customerioService.sendEventAndUpdateState(customerioState, event)
            }
        }
    }
}
