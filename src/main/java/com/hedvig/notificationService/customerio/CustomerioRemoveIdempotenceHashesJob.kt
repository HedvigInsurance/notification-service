package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.customerio.state.IdempotenceHashRepository
import org.quartz.DisallowConcurrentExecution
import org.quartz.JobExecutionContext
import org.springframework.scheduling.quartz.QuartzJobBean
import java.time.Instant


@DisallowConcurrentExecution
open class CustomerioRemoveIdempotenceHashesJob(
    private val eventHashRepository: IdempotenceHashRepository,
    private val customerioService: CustomerioService
) : QuartzJobBean() {
    
    override fun executeInternal(context: JobExecutionContext) {
        val hashesToBeRemoved = eventHashRepository.findBefore(Instant.now().minusSeconds(SEVEN_DAYS_IN_SECONDS))
        hashesToBeRemoved.forEach {
            customerioService.removeIdempotenceHash(
                memberId = it.memberId,
                hash = it.hash
            )
        }
    }

    private val SEVEN_DAYS_IN_SECONDS = 86_400L * 7L
}
