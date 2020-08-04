package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.CustomerioUpdateScheduler
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean

class UpdateStartDateJob(
    private val contractLoader: ContractLoader,
    private val eventCreator: CustomerioEventCreator,
    private val customerioService: CustomerioService,
    private val customerIOStateRepository: CustomerIOStateRepository
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    override fun executeInternal(jobContext: JobExecutionContext) {
        val memberId = jobContext.get("memberId") as String
        val customerioState = customerIOStateRepository.findByMemberId(memberId)!!
        customerioService.doUpdate(customerioState, eventCreator, contractLoader)
    }
}
