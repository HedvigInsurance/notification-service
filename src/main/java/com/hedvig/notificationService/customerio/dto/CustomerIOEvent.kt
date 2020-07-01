package com.hedvig.notificationService.customerio.dto

interface CustomerIOEvent {
    fun toMap(memberId: String): Map<String, Any>
}