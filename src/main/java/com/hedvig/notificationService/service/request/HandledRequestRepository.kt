package com.hedvig.notificationService.service.request

interface HandledRequestRepository {
    fun isRequestHandled(requestId: String): Boolean
    fun storeHandledRequest(requestId: String)
}

