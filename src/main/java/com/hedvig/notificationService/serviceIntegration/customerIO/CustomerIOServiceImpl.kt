package com.hedvig.notificationService.serviceIntegration.customerIO

import com.hedvig.notificationService.serviceIntegration.customerIO.dto.CustomerIOEvent
import com.hedvig.notificationService.web.dto.CustomerIOEventDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("customer.io")
@ConditionalOnProperty(value = ["customerIO.siteId", "customerIO.apiKey"], matchIfMissing = false)
class CustomerIOServiceImpl(
    private val customerIOClient: CustomerIOClient
) : CustomerIOService {

    val logger: Logger = LoggerFactory.getLogger(CustomerIOServiceImpl::class.java)

    override fun postEvent(userId: String, customerIOEvent: CustomerIOEventDto) {
        customerIOClient.postUserEvent(userId, CustomerIOEvent.from(customerIOEvent))
    }

    override fun putTraits(userId: String, traits: Map<String, Any>) {
        customerIOClient.putTraits(userId, traits)
    }
}
