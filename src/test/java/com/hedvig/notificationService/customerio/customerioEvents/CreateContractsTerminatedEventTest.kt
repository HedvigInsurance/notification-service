package com.hedvig.notificationService.customerio.customerioEvents

import assertk.all
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
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

        assertThat(result!!.data.terminatedContracts[0]).all {
            transform { it.type }.isEqualTo(AgreementType.NorwegianTravel.typeName)
            transform { it.terminationDate }.isEqualTo("2020-09-22")
        }
    }

    @Test
    fun `terminated contractId is not terminated from contracts agreements`() {
        val creator = CustomerioEventCreatorImpl()

        val aRandomContractId = UUID.randomUUID()
        val result = creator.contractsTerminatedEvent(
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    contractId = aRandomContractId,
                    terminationDate = null
                )
            ),
            listOf(aRandomContractId.toString())
        )

        assertThat(result).isNull()
    }

    @Test
    fun `terminated contractId matches one of members contracts`() {
        val creator = CustomerioEventCreatorImpl()

        val aRandomContractId = UUID.randomUUID()
        val anotherRandomContractId = UUID.randomUUID()
        val result = creator.contractsTerminatedEvent(
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    contractId = aRandomContractId,
                    terminationDate = LocalDate.of(2020, 5, 3)
                ),
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    contractId = anotherRandomContractId,
                    terminationDate = null
                )
            ),
            listOf(aRandomContractId.toString())
        )

        assertThat(result).isNotNull().transform { it.data.terminatedContracts }.hasSize(1)
    }
}
