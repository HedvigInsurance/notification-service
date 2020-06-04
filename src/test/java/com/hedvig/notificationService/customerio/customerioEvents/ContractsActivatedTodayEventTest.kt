package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.LocalDate

class ContractsActivatedTodayEventTest {
    @get:Rule
    val thrown = ExpectedException.none()

    @Test
    fun `one contract removed update trigger`() {

        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val customerioState = CustomerioState(
            "aMemberId",
            null,
            activationDateTriggerAt = LocalDate.of(2020, 1, 2)
        )
        eventCreatorImpl.execute(
            customerioState,
            listOf(
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                )
            )
        )

        assertThat(customerioState.activationDateTriggerAt).isNull()
    }

    @Test
    fun `test naming`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activationDateTriggerAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                )
            )
        )

        assertThat(result.asMap).contains("name", "ContractsActivatedTodayEvent")
    }

    @Test
    fun `two contracts active today`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activationDateTriggerAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                ),
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                )
            )
        )

        val event = result.event as ContractsActivatedTodayEvent
        assertThat(event.data.activeToday).containsAll(
            Contract(
                "innbo", null, LocalDate.of(2020, 1, 2).toString()
            ),
            Contract("reise", null, LocalDate.of(2020, 1, 2).toString())
        )
    }

    @Test
    fun `one contract with future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val customerioState = CustomerioState(
            "aMemberId",
            activationDateTriggerAt = LocalDate.of(2020, 1, 2)
        )
        val result = eventCreatorImpl.execute(
            customerioState,
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                ),
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    LocalDate.of(2020, 1, 3),
                    "HEDVIG"
                )
            )
        )

        val event = result.event as ContractsActivatedTodayEvent
        assertThat(event.data.activeInFuture).containsAll(
            Contract("innbo", null, LocalDate.of(2020, 1, 3).toString())
        )
        assertThat(customerioState.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 1, 3))
    }

    @Test
    fun `one future activation date one without future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val customerioState = CustomerioState(
            "aMemberId",
            activationDateTriggerAt = LocalDate.of(2020, 1, 2)
        )
        val result = eventCreatorImpl.execute(
            customerioState,
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                ),
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    LocalDate.of(2020, 1, 3),
                    "HEDVIG"
                ),
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "HEDVIG"
                )
            )
        )

        val event = result.event as ContractsActivatedTodayEvent
        assertThat(event.data.activeInFuture).containsAll(
            Contract("innbo", null, LocalDate.of(2020, 1, 3).toString()),
            Contract("innbo", null, null)
        )
        assertThat(customerioState.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 1, 3))
    }

    @Test
    fun `one contract without future activation date`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()
        val customerioState = CustomerioState(
            "aMemberId",
            activationDateTriggerAt = LocalDate.of(2020, 1, 2)
        )
        val result = eventCreatorImpl.execute(
            customerioState,
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 2),
                    "HEDVIG"
                ),
                ContractInfo(
                    AgreementType.NorwegianHomeContent,
                    null,
                    null,
                    "HEDVIG"
                )
            )
        )

        val event = result.event as ContractsActivatedTodayEvent
        assertThat(event.data.activeInFuture).containsAll(
            Contract("innbo", null, null)
        )
        assertThat(customerioState.activationDateTriggerAt).isEqualTo(null)
    }

    @Test
    fun `no contract with activation date today`() {
        val eventCreatorImpl = CustomerioEventCreatorImpl()

        thrown.expectMessage("Cannot send crete event no contracts with activation date today")
        val result = eventCreatorImpl.execute(
            CustomerioState(
                "aMemberId",
                activationDateTriggerAt = LocalDate.of(2020, 1, 2)
            ),
            listOf(
                ContractInfo(
                    AgreementType.NorwegianTravel,
                    null,
                    LocalDate.of(2020, 1, 3),
                    "HEDVIG"
                )
            )
        )

        result.event as ContractsActivatedTodayEvent
    }
}
