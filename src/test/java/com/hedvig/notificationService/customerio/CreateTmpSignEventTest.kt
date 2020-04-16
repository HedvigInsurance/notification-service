package com.hedvig.notificationService.customerio

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class CreateTmpSignEventTest() {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade
    lateinit var sut: CustomerioEventCreatorImpl

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sut = CustomerioEventCreatorImpl(productPricingFacade)
    }

    @Test
    fun `event name is correct`() {
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

    @Test
    fun `norwegian home content switcher`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                "folksam",
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["is_switcher"]).isEqualTo(true)
        assertThat(eventData["switcher_company"]).isEqualTo("folksam")
    }

    @Test
    fun `norwegian home content activation date`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                LocalDate.of(2020, 3, 13)
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["activation_date_innbo"]).isEqualTo("2020-03-13")
    }

    @Test
    fun `norwegian travel`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianTravel,
                null,
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["is_signed_travel"]).isEqualTo(true)
        assertThat(eventData["is_signed_innbo"]).isEqualTo(null)
        assertThat(eventData["activation_date_travel"]).isEqualTo(null)
    }

    @Test
    fun `norwegian travel switcher`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianTravel,
                "a new company",
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["is_switcher"]).isEqualTo(true)
        assertThat(eventData["switcher_company"]).isEqualTo("a new company")
    }

    @Test
    fun `norwegian travel activation date`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianTravel,
                "a new company",
                LocalDate.of(2020, 1, 1)
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)

        assertThat(eventData["activation_date_travel"]).isEqualTo("2020-01-01")
    }

    @Test
    fun `norwegian travel and content`() {
        every { productPricingFacade.getContractTypeForMember(any()) } returns listOf(
            ContractInfo(
                AgreementType.NorwegianTravel,
                null,
                null
            ),
            ContractInfo(
                AgreementType.NorwegianHomeContent,
                null,
                null
            )
        )

        val customerioState = CustomerioState("42", Instant.now(), false)

        val eventData = sut.createTmpSignedInsuranceEvent(customerioState)
        assertThat(eventData["is_signed_travel"]).isEqualTo(true)
        assertThat(eventData["is_signed_innbo"]).isEqualTo(true)
    }
}
