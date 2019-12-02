package com.hedvig.notificationService.serviceIntegration.customerIO

import com.hedvig.notificationService.web.dto.CustomerIOEventDto

interface CustomerIOService {
  fun postEvent(userId: String, customerIOEvent: CustomerIOEventDto)
  fun putTraits(userId: String, traits: Map<String, Any>)
}
