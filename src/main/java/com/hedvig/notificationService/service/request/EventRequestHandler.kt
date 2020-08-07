package com.hedvig.notificationService.service.request

import com.hedvig.notificationService.customerio.EventHandler
import org.springframework.stereotype.Service

@Service
class EventRequestHandler(
    private val eventHandler: EventHandler
) {
    fun onEventRequest(
        requestId: String,
        event: Map<String, Any>
    ) {
        //TODO
    }
}