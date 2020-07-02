package com.hedvig.notificationService.serviceIntegration.memberService

import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member
import com.hedvig.notificationService.serviceIntegration.memberService.dto.PersonHasSignedBeforeRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@FeignClient(
    name = "memberServiceClient",
    url = "\${hedvig.member-service.url:member-service}",
    configuration = [FeignConfiguration::class],
    primary = false
)
interface MemberServiceClient {

    @GetMapping("/_/member/{memberId}")
    fun profile(@PathVariable("memberId") memberId: String): ResponseEntity<Member>

    @GetMapping("/_/member/{memberId}/pickedLocale")
    fun pickedLocale(@PathVariable("memberId") memberId: String): ResponseEntity<PickedLocale>

    @PostMapping("/_/person/has/signed")
    fun personHasSignedBefore(request: PersonHasSignedBeforeRequest): Boolean
}
