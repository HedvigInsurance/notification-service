package com.hedvig.notificationService.customerio.customerioEvents

import assertk.all
import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
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

        val hasStartDate = eventAndState!!.data.contractsWithStartDate
        assertThat(hasStartDate.first()).isNotNull().all {
            transform { it.type }.isEqualTo("innbo")
            transform { it.startDate }.isEqualTo("2020-05-01")
            transform { it.switcherCompany }.isEqualTo("companyName")
        }
        assertThat(eventAndState).isNotNull().isInstanceOf(ContractsActivationDateUpdatedEvent::class)
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

        val data = eventAndState!!.data
        val hasStartDate = data.contractsWithStartDate
        assertThat(hasStartDate)
            .isNotNull().containsAll(
                Contract(
                    "innbo",
                    "companyName",
                    "2020-05-01"
                ),
                Contract(
                    "reise",
                    "anotherCompany",
                    "2020-05-13"
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

        val data = eventAndState!!.data
        val withoutStartDate = data.contractsWithoutStartDate
        assertThat(withoutStartDate)
            .isNotNull().containsAll(
                Contract(
                    "reise",
                    "anotherCompany",
                    null
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

        val result = sut.startDateUpdatedEvent(customerioState, contracts)
        assertThat(result).isNull()
    }
}
