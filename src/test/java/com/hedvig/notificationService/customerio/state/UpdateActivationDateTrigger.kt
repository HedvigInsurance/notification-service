package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import org.junit.Test

class UpdateActivationDateTrigger() {

    @Test
    fun `no previous activation date`() {
        val sut = CustomerioState("aMemberID")

        val contracts = listOf(
            makeContractInfo(
                AgreementType.NorwegianHomeContent,
                switcherCompany = null,
                startDate = null,
                signSource = "IOS",
                partnerCode = "HEDVIG"
            )
        )
        sut.updateFirstUpcomingStartDate(contracts)

        assertThat(sut.activationDateTriggerAt).isNull()
    }
}
