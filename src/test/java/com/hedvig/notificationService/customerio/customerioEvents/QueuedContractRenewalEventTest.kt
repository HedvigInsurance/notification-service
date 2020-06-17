package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.makeCustomerioState
import org.junit.jupiter.api.Test
import java.time.Instant

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
}
