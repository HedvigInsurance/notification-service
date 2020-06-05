package com.hedvig.notificationService.customerio.hedvigfacades

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Address
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Agreement
import com.hedvig.notificationService.serviceIntegration.productPricing.client.AgreementStatus
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Contract
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ContractStatus
import com.hedvig.notificationService.serviceIntegration.productPricing.client.Market
import com.hedvig.notificationService.serviceIntegration.productPricing.client.NorwegianHomeContentLineOfBusiness
import com.hedvig.notificationService.serviceIntegration.productPricing.client.ProductPricingClient
import com.neovisionaries.i18n.CountryCode
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Test
import org.springframework.http.ResponseEntity
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.money.Monetary

class ContractLoaderGetContractsTest {

    @MockK
    lateinit var productPricingClient: ProductPricingClient

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `home content contract`() {

        every { productPricingClient.getContractsForMember(any()) } returns ResponseEntity.ok(
            listOf(
                makeContract(makeNorwegianHomeContentAgreement())
            )
        )

        val sut =
            ContractLoaderImpl(
                productPricingClient
            )

        val contractInfo = sut.getContractTypeForMember("someId")
        assertThat(contractInfo.first().type).isEqualTo(AgreementType.NorwegianHomeContent)
        assertThat(contractInfo.first().startDate).isEqualTo(LocalDate.of(2020, 2, 28))
        assertThat(contractInfo.first().switcherCompany).isNull()
    }

    @Test
    fun `home content contract switcher`() {

        every { productPricingClient.getContractsForMember(any()) } returns ResponseEntity.ok(
            listOf(
                makeContract(makeNorwegianHomeContentAgreement(), switchedFrom = "someName")
            )
        )

        val sut =
            ContractLoaderImpl(
                productPricingClient
            )

        val contractInfo = sut.getContractTypeForMember("someId")
        assertThat(contractInfo.first().type).isEqualTo(AgreementType.NorwegianHomeContent)
        assertThat(contractInfo.first().switcherCompany).isEqualTo("someName")
    }

    @Test
    fun `sets sign source`() {
        every { productPricingClient.getContractsForMember(any()) } returns ResponseEntity.ok(
            listOf(
                makeContract(makeNorwegianHomeContentAgreement(), signSource = "RAPIO")
            )
        )

        val sut =
            ContractLoaderImpl(
                productPricingClient
            )

        val contractInfo = sut.getContractTypeForMember("someId")
        assertThat(contractInfo.first().signSource).isEqualTo("RAPIO")
    }

    private fun makeNorwegianHomeContentAgreement(): Agreement.NorwegianHomeContent {
        return Agreement.NorwegianHomeContent(
            UUID.randomUUID(),
            null,
            null,
            Money.of(33, "NOK"),
            null,
            AgreementStatus.ACTIVE,
            NorwegianHomeContentLineOfBusiness.OWN, Address("", "", "", "", CountryCode.NO), 0, 33
        )
    }

    private fun makeContract(
        vararg agreements: Agreement,
        switchedFrom: String? = null,
        signSource: String? = null
    ): Contract {
        return Contract(
            UUID.randomUUID(),
            "1337",
            switchedFrom,
            LocalDate.of(2020, 2, 28),
            ContractStatus.ACTIVE,
            false,
            null,
            UUID.randomUUID(),
            false,
            listOf(
                *agreements
            ),
            false,
            Monetary.getCurrency("SEK"),
            Market.NORWAY,
            signSource,
            "",
            Instant.now()
        )
    }
}
