package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import org.junit.jupiter.api.Test

class IncludePartnerCodeInNorwegianContractCreatedEventTest {
    @Test
    fun foo() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "ANDROID",
                    "A_PARTNER_CODE"
                )
            )
        )

        assertThat(result.data.partnerCode).isEqualTo("A_PARTNER_CODE")
    }
}
