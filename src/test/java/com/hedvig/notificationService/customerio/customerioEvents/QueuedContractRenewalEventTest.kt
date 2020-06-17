package com.hedvig.notificationService.customerio.customerioEvents

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.makeCustomerioState
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class QueuedContractRenewalEventTest {

    @Test
    internal fun `remove trigger after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()
        state.queueContractRenewal("theContractId", aInstant)

        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent, null, null, null, null
                )
            )
        )

        assertThat(state.contracts[0].contractRenewalQueuedTriggerAt).isNull()
    }

    @Test
    internal fun `create contractRenewalQueuedEvent after execution`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        val state = makeCustomerioState()
        val aInstant = Instant.now()
        state.queueContractRenewal("theContractId", aInstant)

        val renewalDate = LocalDate.of(2021, 7, 1)
        val result = eventCreatorImpl.execute(
            state, listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    null,
                    null,
                    renewalDate
                )
            )
        )

        assertThat(result.event).isInstanceOf(ContractsRenewalQueuedTodayEvent::class.java).all {
            this.transform { it.renewalDate }.isEqualTo(renewalDate)
        }
    }
}
