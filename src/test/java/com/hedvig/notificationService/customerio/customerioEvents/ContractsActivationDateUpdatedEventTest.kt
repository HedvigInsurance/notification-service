package com.hedvig.notificationService.customerio.customerioEvents

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.makeContractInfo
import com.hedvig.notificationService.customerio.state.CustomerioState
import io.mockk.impl.annotations.MockK
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.Instant
import java.time.LocalDate

class ContractsActivationDateUpdatedEventTest {

    @get:Rule
    val thrown = ExpectedException.none()

    @MockK
    lateinit var contractLoader: ContractLoader

    @Test
    fun `execute removes startDateUpdatedAt`() {
        val sut = CustomerioEventCreatorImpl()

        val contracts =
            listOf(
                makeContractInfo(
                    AgreementType.NorwegianHomeContent,
                    switcherCompany = "someCompany",

                    startDate = LocalDate.parse("2020-03-04")
                )
            )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedTriggerAt = callTime)

        sut.startDateUpdatedEvent(customerioState, contracts)

        assertThat(customerioState.startDateUpdatedTriggerAt).isNull()
    }

    @Test
    fun `one contract with start date`() {
        val contracts = listOf(
            makeContractInfo(
                AgreementType.NorwegianHomeContent,
                switcherCompany = "companyName",
                startDate = LocalDate.of(2020, 5, 1)
            )
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedTriggerAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.startDateUpdatedEvent(customerioState, contracts)

        val data = eventAndState.asMap["data"] as Map<String, Any?>
        val hasStartDate = data["contracts_with_start_date"] as List<Map<String, Any?>>?
        assertThat(hasStartDate?.first()).isNotNull().containsAll(
            "type" to "innbo",
            "start_date" to "2020-05-01",
            "switcher_company" to "companyName"
        )
        assertThat(eventAndState.asMap["name"]).isEqualTo("ContractsActivationDateUpdatedEvent")
    }

    @Test
    fun `two contracts with start date`() {
        val contracts = listOf(
            makeContractInfo(
                AgreementType.NorwegianHomeContent,
                switcherCompany = "companyName",
                startDate = LocalDate.of(2020, 5, 1)
            ),

            makeContractInfo(
                AgreementType.NorwegianTravel,
                switcherCompany = "anotherCompany",
                startDate = LocalDate.of(2020, 5, 13)
            )
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedTriggerAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.startDateUpdatedEvent(customerioState, contracts)

        val data = eventAndState.asMap["data"] as Map<String, Any?>
        val hasStartDate = data["contracts_with_start_date"] as List<Map<String, Any?>>?
        assertThat(hasStartDate)
            .isNotNull().containsAll(
                mapOf(
                    "type" to "innbo",
                    "start_date" to "2020-05-01",
                    "switcher_company" to "companyName"
                ),
                mapOf(
                    "type" to "reise",
                    "start_date" to "2020-05-13",
                    "switcher_company" to "anotherCompany"
                )
            )
    }

    @Test
    fun `one contract with one without start date`() {
        val contracts = listOf(
            makeContractInfo(
                agreementType = AgreementType.NorwegianHomeContent,
                switcherCompany = "companyName",
                startDate = LocalDate.of(2020, 5, 1)
            ),
            makeContractInfo(
                AgreementType.NorwegianTravel,
                switcherCompany = "anotherCompany",
                startDate = null
            )
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedTriggerAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.startDateUpdatedEvent(customerioState, contracts)

        val data = eventAndState.asMap["data"] as Map<String, Any?>
        val withoutStartDate = data["contracts_without_start_date"] as List<Map<String, Any?>>?
        assertThat(withoutStartDate)
            .isNotNull().containsAll(
                mapOf(
                    "type" to "reise",
                    "switcher_company" to "anotherCompany"
                )
            )
    }

    @Test
    fun `no contract with one with start date`() {
        val contracts = listOf(
            makeContractInfo(
                AgreementType.NorwegianTravel,
                switcherCompany = "anotherCompany",
                startDate = null
            )
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedTriggerAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        thrown.expectMessage("Cannot create ActivationDateUpdatedEvent no contracts with start date")
        sut.startDateUpdatedEvent(customerioState, contracts)
    }
}
