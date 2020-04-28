package com.hedvig.notificationService.customerio.events

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotEmpty
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

        assertThat(result.state.activateFirstContractAt).isNull()
    }

    @Test
    fun `test naming`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)))
        )

        assertThat(result.asMap).contains("name", "ActivationDateTodayEvent")
        assertThat(result.asMap).transform { it["active_today"] as List<Map<String, Any?>> }.isNotEmpty()
    }

    @Test
    fun `two contracts active today`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)),
                ContractInfo(AgreementType.NorwegianHomeContent, null, LocalDate.of(2020, 1, 2))
            )
        )

        val event = result.event as ActivationDateTodayEvent
        assertThat(event.activeToday).containsAll(
            Contract("", "innbo", null),
            Contract("", "reise", null)
        )
    }

    @Test
    fun `one contract with future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)),
                ContractInfo(AgreementType.NorwegianHomeContent, null, LocalDate.of(2020, 1, 3))
            )
        )

        val event = result.event as ActivationDateTodayEvent
        assertThat(event.activeInFuture).containsAll(
            Contract("", "innbo", null)
        )
        assertThat(result.state.activateFirstContractAt).isEqualTo(LocalDate.of(2020, 1, 3))
    }

    @Test
    fun `one future activation date one without future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)),
                ContractInfo(AgreementType.NorwegianHomeContent, null, LocalDate.of(2020, 1, 3)),
                ContractInfo(AgreementType.NorwegianHomeContent, null, null)
            )
        )

        val event = result.event as ActivationDateTodayEvent
        assertThat(event.activeInFuture).containsAll(
            Contract("", "innbo", null),
            Contract("", "innbo", null)
        )
        assertThat(result.state.activateFirstContractAt).isEqualTo(LocalDate.of(2020, 1, 3))
    }

    @Test
    fun `one contract without future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 2)),
                ContractInfo(AgreementType.NorwegianHomeContent, null, null)
            )
        )

        val event = result.event as ActivationDateTodayEvent
        assertThat(event.activeInFuture).containsAll(
            Contract("", "innbo", null)
        )
        assertThat(result.state.activateFirstContractAt).isEqualTo(null)
    }
}
