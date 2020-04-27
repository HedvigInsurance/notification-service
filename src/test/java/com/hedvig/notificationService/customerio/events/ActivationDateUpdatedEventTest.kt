package com.hedvig.notificationService.customerio.events

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.CustomerioState
import io.mockk.impl.annotations.MockK
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import java.time.Instant
import java.time.LocalDate

class ActivationDateUpdatedEventTest {

    @get:Rule
    val thrown = ExpectedException.none()

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @Test
    fun `execute removes startDateUpdatedAt`() {
        val sut = CustomerioEventCreatorImpl()

        val contracts =
            listOf(ContractInfo(AgreementType.NorwegianHomeContent, "someCompany", LocalDate.parse("2020-03-04")))

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val eventAndState = sut.execute(customerioState, contracts)

        assertThat(eventAndState.second.startDateUpdatedAt).isNull()
    }

    @Test
    fun `one contract with start date`() {
        val contracts = listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent, "companyName", LocalDate.of(2020, 5, 1)
            )
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.execute(customerioState, contracts)

        val data = eventAndState.first["data"] as Map<String, Any?>
        val hasStartDate = data["contractsWithStartDate"] as List<Map<String, Any?>>?
        assertThat(hasStartDate?.first()).isNotNull().containsAll(
            "type" to "innbo",
            "startDate" to "2020-05-01",
            "switcherCompany" to "companyName"
        )
        assertThat(eventAndState.first["name"]).isEqualTo("ActivationDateUpdatedEvent")
    }

    @Test
    fun `two contracts with start date`() {
        val contracts = listOf(
            ContractInfo(AgreementType.NorwegianHomeContent, "companyName", LocalDate.of(2020, 5, 1)),
            ContractInfo(AgreementType.NorwegianTravel, "anotherCompany", LocalDate.of(2020, 5, 13))
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.execute(customerioState, contracts)

        val data = eventAndState.first["data"] as Map<String, Any?>
        val hasStartDate = data["contractsWithStartDate"] as List<Map<String, Any?>>?
        assertThat(hasStartDate)
            .isNotNull().containsAll(
                mapOf(
                    "type" to "innbo",
                    "startDate" to "2020-05-01",
                    "switcherCompany" to "companyName"
                ),
                mapOf(
                    "type" to "reise",
                    "startDate" to "2020-05-13",
                    "switcherCompany" to "anotherCompany"
                )
            )
    }

    @Test
    fun `one contract with one without start date`() {
        val contracts = listOf(
            ContractInfo(AgreementType.NorwegianHomeContent, "companyName", LocalDate.of(2020, 5, 1)),
            ContractInfo(AgreementType.NorwegianTravel, "anotherCompany", null)
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        val eventAndState = sut.execute(customerioState, contracts)

        val data = eventAndState.first["data"] as Map<String, Any?>
        val withoutStartDate = data["contractsWithoutStartDate"] as List<Map<String, Any?>>?
        assertThat(withoutStartDate)
            .isNotNull().containsAll(
                mapOf(
                    "type" to "reise",
                    "switcherCompany" to "anotherCompany"
                )
            )
    }

    @Test
    fun `no contract with one with start date`() {
        val contracts = listOf(
            ContractInfo(AgreementType.NorwegianTravel, "anotherCompany", null)
        )

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val sut = CustomerioEventCreatorImpl()
        thrown.expectMessage("Cannot create ActivationDateUpdatedEvent no contracts with start date")
        sut.execute(customerioState, contracts)
    }
}
