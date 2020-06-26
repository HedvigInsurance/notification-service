package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import org.junit.Test

class NiceSwitcherCompanyNames {

    @Test
    fun `remove NO from company name`() {
        val contract = Contract.from(
            makeContractInfo(
                AgreementType.NorwegianTravel,
                switcherCompany = "Gjensidige NO",
                startDate = null,
                signSource = "IOS",
                partnerCode = "HEDVIG"
            )
        )

        assertThat(contract.switcherCompany).isNotNull().isEqualTo("Gjensidige")
    }

    @Test
    fun `keep name as is`() {
        val contract = Contract.from(
            makeContractInfo(
                AgreementType.NorwegianTravel,
                switcherCompany = "Fremtind",
                startDate = null,
                signSource = "IOS",
                partnerCode = "HEDVIG"
            )
        )

        assertThat(contract.switcherCompany).isNotNull().isEqualTo("Fremtind")
    }
}
