package com.hedvig.notificationService.customerio

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant

class CreateTmpSignEventTest {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `event name is correct`() {

        val sut = CustomerioEventCreatorImpl(productPricingFacade)
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["name"]).isEqualTo("TmpSignedInsuranceEvent")
    }

    @Test
    fun `norwegian home content`() {

        val sut = CustomerioEventCreatorImpl(productPricingFacade)
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["is_signed_innbo"]).isEqualTo(true)
        assertThat(eventData["activation_date_innbo"]).isEqualTo(null)
    }
}
