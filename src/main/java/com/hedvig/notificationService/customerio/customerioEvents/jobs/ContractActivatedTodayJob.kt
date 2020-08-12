package com.hedvig.notificationService.customerio.customerioEvents.jobs

import com.hedvig.notificationService.common.quartz.executeWithRetry
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import datadog.trace.api.Trace
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.quartz.PersistJobDataAfterExecution
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.LocalDate

@DisallowConcurrentExecution
@PersistJobDataAfterExecution
class ContractActivatedTodayJob(
    private val contractLoader: ContractLoader,
    private val eventCreator: CustomerioEventCreator,
    private val customerioService: CustomerioService,
    private val customerIOStateRepository: CustomerIOStateRepository
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(ContractActivatedTodayJob::class.java)

    @Trace
    override fun executeInternal(jobContext: JobExecutionContext) {
        executeWithRetry(jobContext,
            {
                logger.error("Job ${this::class.simpleName} could not be completed", it)
            }
        ) {
            val memberId = jobContext.mergedJobDataMap.get("memberId") as String

            logger.info("Running ContractActivatedTodayJob.executeInternal with member $memberId")

            val customerioState = customerIOStateRepository.findByMemberId(memberId)!!
            val contracts = contractLoader.getContractInfoForMember(customerioState.memberId)
            TODO()
            val eventAndState = eventCreator.sendActivatesToday(customerioState, contracts, LocalDate.of(2020, 1, 2))
            customerioService.sendEventAndUpdateState(customerioState, eventAndState.asMap)
        }
    }
}
