package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import org.junit.Test

class NiceSwitcherCompanyNames {

    @Test
    fun `remove NO from company name`() {
        val contract = Contract.from(ContractInfo(AgreementType.NorwegianTravel, "Gjensidige NO", null))

        assertThat(contract.switcherCompany).isNotNull().isEqualTo("Gjensidige")
    }

    @Test
    fun `keep name as is`() {
        val contract = Contract.from(ContractInfo(AgreementType.NorwegianTravel, "Fremtind", null))

        assertThat(contract.switcherCompany).isNotNull().isEqualTo("Fremtind")
    }
}
