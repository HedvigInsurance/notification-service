package com.hedvig.notificationService.customerio.dto

import assertk.assertThat
import assertk.assertions.doesNotContain
import com.hedvig.notificationService.customerio.builders.EMAIL
import com.hedvig.notificationService.customerio.builders.SSN
import com.hedvig.notificationService.customerio.builders.a
import org.junit.Test

internal class QuoteCreatedEventTest {
    @Test
    internal fun `toMap does not include social security or email information`() {
        val result = a.quoteCreatedEvent.build().toMap()
        assertThat(result.values).doesNotContain(SSN)
        assertThat(result.values).doesNotContain(EMAIL)
    }
}
