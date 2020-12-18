package com.hedvig.notificationService.utils

import com.hedvig.notificationService.customerio.Workspace
import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test

class PhonNumberUtilTest {

    @Test
    fun `format swedish phonenumber`() {
        val unFormattedSwedishPhoneNumber = "081231313"

        val formattedPhoneNumber = e146FormatPhoneNumber(unFormattedSwedishPhoneNumber, Workspace.SWEDEN)

        assertThat(formattedPhoneNumber).isEqualTo("+4681231313")
    }
}
