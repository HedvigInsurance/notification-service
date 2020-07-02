package com.hedvig.notificationService.serviceIntegration.memberService

import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member
import com.hedvig.notificationService.serviceIntegration.memberService.dto.PersonHasSignedBeforeRequest
import org.springframework.http.ResponseEntity

class FakeMemberServiceClient : MemberServiceClient {
    override fun profile(memberId: String): ResponseEntity<Member> {
        return ResponseEntity.ok(
            Member(
                1337,
                "191212121212",
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                "en-SE",
                listOf()
            )
        )
    }

    override fun pickedLocale(memberId: String): ResponseEntity<PickedLocale> {
        return ResponseEntity.ok(PickedLocale("nb_NO"))
    }

    override fun personHasSignedBefore(request: PersonHasSignedBeforeRequest): Boolean = false
}
