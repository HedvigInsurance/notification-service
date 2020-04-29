package com.hedvig.notificationService.customerio.state

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import org.junit.Test

class UpdateActivationDateTrigger() {

    @Test
    fun `no previous activation date`() {
        val sut = CustomerioState("aMemberID")

        val contracts = listOf(ContractInfo(AgreementType.NorwegianHomeContent, null, null))
        val result = sut.updateFirstUpcomingStartDate(contracts)

        assertThat(result.firstUpcomingStartDate).isNull()
    }
}
