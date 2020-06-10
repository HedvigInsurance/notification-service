package com.hedvig.notificationService.customerio.customerioEvents

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.junit.jupiter.api.Test
import java.time.Instant

class IncludeSignSourceInNorwegianContractCreatedEventTest {
    @Test
    internal fun `one contract signsource is same`() {

        val sut = CustomerioEventCreatorImpl()

        val customerioState = CustomerioState("aMemberId")
        customerioState.createContract("aContractId", Instant.now(), null)

        val result = sut.execute(
            customerioState, listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    startDate = null,
                    signSource = "RAPIO",
                    partnerCode = "HEDIVG"
                )
            )
        )

        assertThat(result.event)
            .isInstanceOf(NorwegianContractCreatedEvent::class)
            .all {
                transform { it.data.signSource }.isEqualTo("RAPIO")
            }
    }
}
