package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasPersonSignedBeforeRequest
import java.util.Locale

class MemberServiceImpl(private val memberServiceClient: MemberServiceClient) {
    fun getPickedLocale(memberId: String): Locale {
        return Locale.forLanguageTag(memberServiceClient.pickedLocale(memberId).body.pickedLocale.replace('_', '-'))
    }

    fun hasPersonSignedBefore(request: HasPersonSignedBeforeRequest): Boolean {
        return memberServiceClient.hasPersonSignedBefore(request)
    }
}
