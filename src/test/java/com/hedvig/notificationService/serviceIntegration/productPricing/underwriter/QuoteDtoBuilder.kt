package com.hedvig.notificationService.serviceIntegration.productPricing.underwriter

import com.hedvig.notificationService.serviceIntegration.underwriter.NorwegianHomeContentsData
import com.hedvig.notificationService.serviceIntegration.underwriter.ProductType
import com.hedvig.notificationService.serviceIntegration.underwriter.QuoteDto
import com.hedvig.notificationService.serviceIntegration.underwriter.QuoteState
import com.hedvig.notificationService.serviceIntegration.underwriter.SignMethod
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class QuoteDtoBuilder

fun makeQuoteDto(attributedTo: String = "HEDVIG"): QuoteDto =
    QuoteDto(
        UUID.randomUUID(),
        Instant.parse("2020-06-09T13:35:07.351264Z"),
        BigDecimal.TEN,
        ProductType.HOME_CONTENT,
        QuoteState.SIGNED,
        "IOS",
        attributedTo,
        NorwegianHomeContentsData(
            UUID.randomUUID(),
            null,
            LocalDate.of(1970, 1, 1),
            "Herr Norsk",
            "Noen",
            "noen@norsk.no",
            "A street",
            null,
            "1234",
            42,
            0,
            false
        ),
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
        SignMethod.NORWEGIAN_BANK_ID
    )
