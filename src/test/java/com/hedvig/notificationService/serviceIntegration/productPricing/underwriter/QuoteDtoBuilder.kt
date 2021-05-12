package com.hedvig.notificationService.serviceIntegration.productPricing.underwriter

import com.hedvig.notificationService.serviceIntegration.underwriter.QuoteDto
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class QuoteDtoBuilder

fun makeQuoteDto(attributedTo: String = "HEDVIG"): QuoteDto =
    QuoteDto(
        UUID.randomUUID(),
        Instant.parse("2020-06-09T13:35:07.351264Z"),
        BigDecimal.TEN,
        "HOME_CONTENT",
        "SIGNED",
        "IOS",
        attributedTo,
        null,
        null,
        123,
        "1337",
        listOf(),
        true,
        null,
        UUID.randomUUID(),
        UUID.randomUUID(),
        null,
        "NORWEGIAN_BANK_ID"
    )
