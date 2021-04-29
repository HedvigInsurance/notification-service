package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import org.junit.jupiter.api.Test

class IncludePartnerCodeInNorwegianContractCreatedEventTest {
    @Test
    fun `creates norwegian home content contract with partner code`() {
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

        assertThat((result as NorwegianContractCreatedEvent).data.partnerCode).isEqualTo("A_PARTNER_CODE")
    }

    @Test
    fun `creates norwegian home travel contract with partner code`() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                makeContractInfo(
                    AgreementType.NorwegianTravel,
                    switcherCompany = null,
                    startDate = null,
                    signSource = "ANDROID",
                    partnerCode = "A_PARTNER_CODE"
                )
            )
        )

        assertThat((result as NorwegianContractCreatedEvent).data.partnerCode).isEqualTo("A_PARTNER_CODE")
    }

    @Test
    fun `creates Danish home content contract with partner code`() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                makeContractInfo(
                    AgreementType.DanishHomeContent,
                    signSource = "ANDROID",
                    partnerCode = "A_DANISH_PARTNER_CODE"
                )
            )
        )

        assertThat((result as DanishContractCreatedEvent).data.partnerCode).isEqualTo("A_DANISH_PARTNER_CODE")
    }

    @Test
    fun `does not create partner code on DanishTravel`() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                makeContractInfo(
                    AgreementType.DanishTravel,
                    signSource = "ANDROID",
                    partnerCode = "A_DANISH_PARTNER_CODE"
                )
            )
        )

        assertThat((result as DanishContractCreatedEvent).data.partnerCode).isNull()
    }

    @Test
    fun `does not create partner code on DanishAccident`() {
        val sut = CustomerioEventCreatorImpl()

        val result = sut.createContractCreatedEvent(
            listOf(
                makeContractInfo(
                    AgreementType.DanishAccident,
                    signSource = "ANDROID",
                    partnerCode = "A_DANISH_PARTNER_CODE"
                )
            )
        )

        assertThat((result as DanishContractCreatedEvent).data.partnerCode).isNull()
    }
}
