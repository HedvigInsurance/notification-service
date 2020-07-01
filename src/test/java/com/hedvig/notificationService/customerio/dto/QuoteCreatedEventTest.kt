package com.hedvig.notificationService.customerio.dto

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.dto.objects.Partner
import com.hedvig.notificationService.customerio.dto.objects.ProductType
import com.hedvig.notificationService.customerio.dto.objects.QuoteInitiatedFrom
import org.junit.Test
import java.math.BigDecimal
import java.util.UUID

internal class QuoteCreatedEventTest {
    @Test
    internal fun `should send ordinary quote event`() {
        val result = ordinaryEvent.shouldSend()
        assertThat(result).isTrue()
    }

    @Test
    internal fun `should not send event of quote initiated from hope`() {
        val result = ordinaryEvent.copy(initiatedFrom = QuoteInitiatedFrom.HOPE).shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `should not send event of quote with originating product id`() {
        val result = ordinaryEvent.copy(originatingProductId = UUID.randomUUID()).shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `should not send event of quote with unknown product type`() {
        val result = ordinaryEvent.copy(productType = ProductType.UNKNOWN).shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `toMap does not include social security or email information`() {
        val result = ordinaryEvent.toMap("123")
        assertThat(result.values).doesNotContain(SSN)
        assertThat(result.values).doesNotContain(EMAIL)
    }

    companion object {
        private const val SSN = "123456789"
        private const val EMAIL = "test@hedvig.com"
        private val ordinaryEvent = QuoteCreatedEvent(
            quoteId = UUID.randomUUID(),
            email = EMAIL,
            ssn = SSN,
            initiatedFrom = QuoteInitiatedFrom.WEBONBOARDING,
            attributedTo = Partner.HEDVIG,
            productType = ProductType.APARTMENT,
            currentInsurer = null,
            price = BigDecimal("99"),
            currency = "SEK",
            originatingProductId = null,
            address = "Testvägen 1"
        )
    }
}