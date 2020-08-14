package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.hasSize
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class CreateContractsTerminatedEventTest {

    @Test
    fun `one terminated contract`() {

        val creator = CustomerioEventCreatorImpl()

        val aRandomContractId = UUID.randomUUID()
        val result = creator.contractsTerminatedEvent(
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    contractId = aRandomContractId,
                    terminationDate = LocalDate.of(2020, 9, 22)
                )
            ),
            listOf(aRandomContractId.toString())
        )

        assertThat(result.data.terminatedContracts).hasSize(1)
    }
}
