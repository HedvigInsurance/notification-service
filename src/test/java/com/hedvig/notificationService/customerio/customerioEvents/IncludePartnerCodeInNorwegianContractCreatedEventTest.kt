package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import org.junit.jupiter.api.Test

class IncludePartnerCodeInNorwegianContractCreatedEventTest {
    @Test
    fun foo() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                makeContractInfo(
                    AgreementType.NorwegianHomeContent,
                    switcherCompany = null,
                    startDate = null,
                    signSource = "ANDROID",
                    partnerCode = "A_PARTNER_CODE"
                )
            )
        )

        assertThat(result.data.partnerCode).isEqualTo("A_PARTNER_CODE")
    }
}
