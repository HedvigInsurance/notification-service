package com.hedvig.notificationService.customerio

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.notificationService.customerio.customerioEvents.CustomerioEventCreatorImpl
import com.hedvig.notificationService.customerio.customerioEvents.NorwegianContractCreatedEvent
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.junit.jupiter.api.Test
import java.time.Instant

class UseStateContractsForContractCreatedEventTest {

    @Test
    internal fun `a first test`() {

        val sut = CustomerioEventCreatorImpl()

        val customerioState = CustomerioState("aMemberId")
        customerioState.createContract("aContractId", Instant.now(), null)

        val result = sut.execute(
            customerioState, listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, startDate = null, signSource = "RAPIO")
            )
        )

        assertThat(result.event)
            .isInstanceOf(NorwegianContractCreatedEvent::class)
            .all {
                transform { it.data.signSource }.isEqualTo("RAPIO")
            }
    }
}
