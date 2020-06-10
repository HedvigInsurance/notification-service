package com.hedvig.notificationService.serviceIntegration.underwriter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    "underwriterClient",
    url = "\${hedvig.underwriter.url:http://underwriter}"
)
interface UnderwriterClient {

    @GetMapping("/_/v1/quotes/contracts/{contractId}")
    fun getQuoteFromContractId(@PathVariable contractId: String): ResponseEntity<QuoteDto>
}
