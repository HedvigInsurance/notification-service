package com.hedvig.notificationService.serviceIntegration.memberService

import org.springframework.stereotype.Service
import java.util.*

@Service
class MemberService {
    fun getPreferredLanguage(memberId: String): Locale = Locale.forLanguageTag("sv-SE") // TODO: Call member-service and ask it for the actual preferred language
}
