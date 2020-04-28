package com.hedvig.notificationService.customerio.events

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.junit.Test
import java.time.LocalDate

class ActivatesTodayEventTest {

    @Test
    fun `one contract removed update trigger`() {

        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                null,
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(ContractInfo(AgreementType.NorwegianHomeContent, null, LocalDate.of(2020, 1, 2)))
        )

        assertThat(result.second.activateFirstContractAt).isNull()
    }

    @Test
    fun `one contract sets active contract in event`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                null,
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)))
        )

        assertThat(result.first).contains("name", "ActivationDateTodayEvent")
    }
}
