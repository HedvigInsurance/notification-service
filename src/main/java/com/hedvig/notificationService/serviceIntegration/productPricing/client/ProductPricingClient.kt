package com.hedvig.notificationService.serviceIntegration.productPricing.client

import com.hedvig.notificationService.serviceIntegration.memberService.FeignConfiguration
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    "productPricingClient",
    url = "\${hedvig.product-pricing.url:http://product-pricing}", configuration = [FeignConfiguration::class]
)
interface ProductPricingClient {

    @GetMapping("/_/contracts/members/{memberId}/contract/market/info")
    fun getContractMarketInfo(@PathVariable memberId: String): ResponseEntity<ContractMarketInfo>
}
