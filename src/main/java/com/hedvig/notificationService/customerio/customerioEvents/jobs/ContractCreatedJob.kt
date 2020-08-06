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

class ContractCreatedJob(
    private val contractLoader: ContractLoader,
    private val eventCreator: CustomerioEventCreator,
    private val customerioService: CustomerioService,
    private val customerIOStateRepository: CustomerIOStateRepository
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    override fun executeInternal(jobContext: JobExecutionContext) {

        executeWithRetry(jobContext,
            {
                logger.error("Job ${this::class.simpleName} could not be completed", it)
            }
        ) {
            val memberId = jobContext.mergedJobDataMap.get("memberId") as String
            val customerioState = customerIOStateRepository.findByMemberId(memberId)!!
            val contracts = contractLoader.getContractInfoForMember(customerioState.memberId)
            val eventAndState = eventCreator.contractCreatedEvent(customerioState, contracts)
            customerioService.sendEventAndUpdateState(customerioState, eventAndState.asMap)
        }
    }
}
