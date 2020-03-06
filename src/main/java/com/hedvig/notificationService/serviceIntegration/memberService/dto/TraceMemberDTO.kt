package com.hedvig.notificationService.serviceIntegration.memberService.dto

import java.time.LocalDateTime

data class TraceMemberDTO (
     val date: LocalDateTime?,
     val oldValue: String?,
     val newValue: String?,
     val fieldName: String?,
     val memberId: String?,
     val userId: String?
)