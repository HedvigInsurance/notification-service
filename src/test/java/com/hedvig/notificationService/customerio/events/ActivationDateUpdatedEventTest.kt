package com.hedvig.notificationService.customerio.events

import assertk.assertThat
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
    fun `first test`() {
        val sut = CustomerioEventCreatorImpl()

        val contracts =
            listOf(ContractInfo(AgreementType.NorwegianHomeContent, "someCompany", LocalDate.parse("2020-03-04")))

        val callTime = Instant.parse("2020-04-27T18:50:41.760555Z")
        val customerioState = CustomerioState("amember", null, startDateUpdatedAt = callTime)

        val eventAndState = sut.execute(customerioState, contracts)

        assertThat(eventAndState.second.startDateUpdatedAt).isNull()
    }
}
