package com.hedvig.notificationService.serviceIntegration.customerIO

import com.hedvig.notificationService.serviceIntegration.customerIO.dto.CustomerIOEvent
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    name = "customer.io.client",
    url = "\${customerIO.url:https://track.customer.io/api}",
    configuration = [CustomerIOFeignConfiguration::class]
)
interface CustomerIOClient {
    @PostMapping("/v1/customers/{userId}/events")
    fun postUserEvent(
        @PathVariable userId: String,
        @RequestBody event: CustomerIOEvent
    )

    @PutMapping("/v1/customers/{userId}")
    fun putTraits(
        @PathVariable userId: String,
        @RequestBody traits: Map<String, Any>
    )
}
