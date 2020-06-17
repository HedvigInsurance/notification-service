package com.hedvig.notificationService.customerio.customerioEvents

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.makeCustomerioState
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class QueuedContractRenewalEventTest {

    @Test
    internal fun `remove trigger after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()

        val contractId = UUID.randomUUID()
        state.queueContractRenewal(contractId.toString(), aInstant)

        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    type = AgreementType.NorwegianHomeContent,
                    contractId = contractId,
                    renewalDate = LocalDate.of(2020, 9, 24)
                )
            )
        )

        assertThat(state.contracts[0].contractRenewalQueuedTriggerAt).isNull()
    }

    @Test
    internal fun `remove only triggered trigger after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()
        val triggeredContractId = UUID.randomUUID()
        state.queueContractRenewal(triggeredContractId.toString(), aInstant)
        state.queueContractRenewal("theContractId2", aInstant)

        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    type = AgreementType.NorwegianHomeContent,
                    contractId = triggeredContractId,
                    renewalDate = LocalDate.of(2020, 6, 1)
                )
            )
        )

        assertThat(state.contracts[0].contractRenewalQueuedTriggerAt).isNull()
        assertThat(state.contracts[1].contractRenewalQueuedTriggerAt).isNotNull()
    }

    @Test
    internal fun `create contractRenewalQueuedEvent after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()
        val contractId = UUID.randomUUID()
        state.queueContractRenewal(contractId.toString(), aInstant)

        val renewalDate = LocalDate.of(2021, 7, 1)
        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    type = AgreementType.NorwegianHomeContent,
                    renewalDate = renewalDate,
                    contractId = contractId
                )
            )
        )

        assertThat(result.event).isInstanceOf(ContractsRenewalQueuedTodayEvent::class.java).all {
            this.transform { it.renewalDate }.isEqualTo(renewalDate)
            this.transform { it.type }.isEqualTo(AgreementType.NorwegianHomeContent.typeName)
        }
    }

    @Test
    internal fun `create contractRenewalQueuedEvent for multiple contracts after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()
        val aContractId = UUID.randomUUID()
        state.queueContractRenewal(aContractId.toString(), aInstant)

        val renewalDate = LocalDate.of(2021, 7, 11)
        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    type = AgreementType.NorwegianTravel,
                    renewalDate = renewalDate,
                    contractId = aContractId
                ),
                ContractInfo(
                    type = AgreementType.NorwegianHomeContent,
                    contractId = UUID.randomUUID()
                )
            )
        )

        assertThat(result.event).isInstanceOf(ContractsRenewalQueuedTodayEvent::class.java).all {
            this.transform { it.renewalDate }.isEqualTo(renewalDate)
            this.transform { it.type }.isEqualTo(AgreementType.NorwegianTravel.typeName)
        }
    }
}
