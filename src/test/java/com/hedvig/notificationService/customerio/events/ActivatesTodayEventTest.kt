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
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.LocalDate

class ActivatesTodayEventTest {
    @get:Rule
    val thrown = ExpectedException.none()

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
            Contract(
                "innbo", null, LocalDate.of(2020, 1, 23)
            ),
            Contract("reise", null, LocalDate.of(2020, 1, 2))
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
            Contract("innbo", null, LocalDate.of(2020, 1, 2))
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
            Contract("innbo", null, LocalDate.of(2020, 1, 3)),
            Contract("innbo", null, null)
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
            Contract("innbo", null, null)
        )
        assertThat(result.state.activateFirstContractAt).isEqualTo(null)
    }

    @Test
    fun `no contract with activation date today`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        thrown.expectMessage("Cannot send crete event no contracts with activation date today")
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activateFirstContractAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(AgreementType.NorwegianTravel, null, LocalDate.of(2020, 1, 3))
            )
        )

        val event = result.event as ActivationDateTodayEvent
    }
}
