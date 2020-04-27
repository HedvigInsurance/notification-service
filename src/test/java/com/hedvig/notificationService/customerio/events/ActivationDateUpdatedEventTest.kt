package com.hedvig.notificationService.customerio.events

import assertk.assertThat
import assertk.assertions.containsAll
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.CustomerioState
import io.mockk.impl.annotations.MockK
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class ActivationDateUpdatedEventTest {
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

        val hasStartDate = eventAndState.first["contractsWithStartDate"] as List<Map<String, Any?>>?
        assertThat(hasStartDate?.first()).isNotNull().containsAll(
            "type" to "innbo",
            "startDate" to "2020-05-01",
            "switcherCompany" to "companyName"
        )
    }
}
