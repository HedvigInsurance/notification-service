package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.CustomerioUpdateScheduler
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreator
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.state.CustomerIOStateRepository
import org.quartz.DateBuilder
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.slf4j.LoggerFactory
import org.springframework.scheduling.quartz.QuartzJobBean

class UpdateStartDateJob(
    private val contractLoader: ContractLoader,
    private val eventCreator: CustomerioEventCreator,
    private val customerioService: CustomerioService,
    private val customerIOStateRepository: CustomerIOStateRepository,
    private val scheduler: Scheduler
) : QuartzJobBean() {
    private val logger = LoggerFactory.getLogger(CustomerioUpdateScheduler::class.java)

    override fun executeInternal(jobContext: JobExecutionContext) {

        try {
            val memberId = jobContext.mergedJobDataMap.get("memberId") as String
            val customerioState = customerIOStateRepository.findByMemberId(memberId)!!
            customerioService.doUpdate(customerioState, eventCreator, contractLoader)
        } catch (x: Exception) {
            jobContext.scheduler.scheduleJob(
                jobContext.jobDetail,
                TriggerBuilder
                    .newTrigger()
                    .startAt(DateBuilder.futureDate(10, DateBuilder.IntervalUnit.MINUTE))
                    .build()
            )
        }
    }
}
