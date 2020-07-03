package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.dto.PersonHasSignedBeforeRequest
import java.util.Locale

class MemberServiceImpl(private val memberServiceClient: MemberServiceClient) {
    fun getPickedLocale(memberId: String): Locale {
        return Locale.forLanguageTag(memberServiceClient.pickedLocale(memberId).body.pickedLocale.replace('_', '-'))
    }

    fun personHasSignedBefore(request: PersonHasSignedBeforeRequest): Boolean {
        return memberServiceClient.personHasSignedBefore(request)
    }
}
