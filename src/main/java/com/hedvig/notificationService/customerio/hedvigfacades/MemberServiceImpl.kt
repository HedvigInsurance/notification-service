package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Flag
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasSignedBeforeRequest
import java.util.Locale

class MemberServiceImpl(private val memberServiceClient: MemberServiceClient) {
    fun getPickedLocale(memberId: String): Locale {
        return Locale.forLanguageTag(memberServiceClient.pickedLocale(memberId).body.pickedLocale.replace('_', '-'))
    }

    fun hasPersonSignedBefore(request: HasSignedBeforeRequest): Boolean {
        return memberServiceClient.hasPersonSignedBefore(request)
    }

    fun hasRedFlag(memberId: String): Boolean {
        try {
            val personStatus = memberServiceClient.getPersonStatusByMemberId(memberId)
            if (personStatus.isWhitelisted) {
                return false
            }
            if (personStatus.flag == Flag.RED) {
                return true
            }
            return false
        } catch (exception: Exception) {
            return false
        }
    }
}
