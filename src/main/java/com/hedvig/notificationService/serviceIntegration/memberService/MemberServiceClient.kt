package com.hedvig.notificationService.serviceIntegration.memberService

import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod

@FeignClient(
    name = "memberServiceClient",
    url = "\${hedvig.member-service.url:member-service}",
    configuration = [FeignConfiguration::class]
)
interface MemberServiceClient {

    @RequestMapping(value = ["/member/{memberId}"], method = [RequestMethod.GET])
    fun profile(@PathVariable("memberId") memberId: String): ResponseEntity<Member>
}
