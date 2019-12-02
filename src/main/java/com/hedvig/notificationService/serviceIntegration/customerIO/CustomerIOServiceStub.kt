package com.hedvig.notificationService.serviceIntegration.customerIO

import com.hedvig.notificationService.web.dto.CustomerIOEventDto
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!customer.io")
class CustomerIOServiceStub : CustomerIOService {
    override fun postEvent(userId: String, customerIOEvent: CustomerIOEventDto) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun putTraits(userId: String, traits: Map<String, Any>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
