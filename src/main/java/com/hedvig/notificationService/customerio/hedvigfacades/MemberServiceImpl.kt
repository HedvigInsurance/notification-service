package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Flag
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasSignedBeforeRequest
import java.util.Locale

class MemberServiceImpl(private val memberServiceClient: MemberServiceClient) {
    fun getPickedLocale(memberId: String): Locale {
        return Locale.forLanguageTag(memberServiceClient.pickedLocale(memberId).body.pickedLocale.replace('_', '-'))
    }

    fun hasPersonSignedBefore(memberId: String, ssn: String?, email: String): Boolean {
        return memberServiceClient.hasPersonSignedBefore(
            HasSignedBeforeRequest(
                memberId = memberId,
                ssn = ssn,
                email = email
            )
        )
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
