package com.hedvig.notificationService.customerio

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import java.util.Locale

class MemberServiceImpl(private val memberServiceClient: MemberServiceClient) {
    fun getPickedLocale(memberId: String): Locale {
        return Locale.forLanguageTag(memberServiceClient.pickedLocale(memberId).body.pickedLocale.replace('_', '-'))
    }
}
