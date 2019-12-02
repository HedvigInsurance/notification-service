package com.hedvig.notificationService.web.dto

import org.springframework.web.multipart.MultipartFile

data class CustomerIOEventDto(
    val eventName: String,
    val eventData: Map<String, Any>?,
    val attachments: Map<String, MultipartFile>?
)
