package com.hedvig.notificationService.serviceIntegration.customerIO.dto

import com.hedvig.notificationService.web.dto.CustomerIOEventDto

data class CustomerIOEvent(
    val name: String,
    val data: Map<String, Any>?
) {
    companion object {
        fun from(customerIOEventDto: CustomerIOEventDto): CustomerIOEvent {
            val data = mutableMapOf<String, Any>()
            if (customerIOEventDto.eventData != null) {
                data.putAll(customerIOEventDto.eventData)
            }
            if (customerIOEventDto.attachments != null) {
                data["attachments"] = customerIOEventDto.attachments
            }
            return CustomerIOEvent(
                customerIOEventDto.eventName,
                data = data
            )
        }
    }
}
