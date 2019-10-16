package com.hedvig.notificationService.serviceIntegration.memberService

import org.springframework.stereotype.Service

@Service
class MemberService {
    fun getPreferredLanguage(memberId: String): PreferredLanguage = PreferredLanguage.sv_SE // TODO: Call member-service and ask it for the actual preferred language
}

enum class PreferredLanguage {
    sv_SE,
    en_SE
}