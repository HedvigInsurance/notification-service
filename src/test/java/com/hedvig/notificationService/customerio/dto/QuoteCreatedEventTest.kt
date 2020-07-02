package com.hedvig.notificationService.customerio.dto

import assertk.assertThat
import assertk.assertions.doesNotContain
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.builders.EMAIL
import com.hedvig.notificationService.customerio.builders.SSN
import com.hedvig.notificationService.customerio.builders.a
import org.junit.Test
import java.util.UUID

internal class QuoteCreatedEventTest {
    @Test
    internal fun `should send ordinary quote event`() {
        val result = a.quoteCreatedEvent.build().shouldSend()
        assertThat(result).isTrue()
    }

    @Test
    internal fun `should not send event of quote initiated from hope`() {
        val result = a.quoteCreatedEvent.copy(initiatedFrom = "HOPE").build().shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `should not send event of quote with originating product id`() {
        val result = a.quoteCreatedEvent.copy(originatingProductId = UUID.randomUUID()).build().shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `should not send event of quote with unknown product type`() {
        val result = a.quoteCreatedEvent.copy(productType = "UNKNOWN").build().shouldSend()
        assertThat(result).isFalse()
    }

    @Test
    internal fun `toMap does not include social security or email information`() {
        val result = a.quoteCreatedEvent.build().toMap()
        assertThat(result.values).doesNotContain(SSN)
        assertThat(result.values).doesNotContain(EMAIL)
    }
}