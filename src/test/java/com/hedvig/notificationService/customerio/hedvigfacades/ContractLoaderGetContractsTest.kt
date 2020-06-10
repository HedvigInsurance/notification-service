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
import com.hedvig.notificationService.serviceIntegration.productPricing.underwriter.makeQuoteDto
import com.hedvig.notificationService.serviceIntegration.underwriter.UnderwriterClient
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

    @MockK
    lateinit var underwriterClient: UnderwriterClient

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
        every { underwriterClient.getQuoteFromContractId(any()) } returns ResponseEntity.ok(makeQuoteDto())

        val sut =
            ContractLoaderImpl(
                productPricingClient,
                underwriterClient
            )

        val contractInfo = sut.getContractInfoForMember("someId")
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
        every { underwriterClient.getQuoteFromContractId(any()) } returns ResponseEntity.ok(makeQuoteDto())

        val sut =
            ContractLoaderImpl(
                productPricingClient,
                underwriterClient
            )

        val contractInfo = sut.getContractInfoForMember("someId")
        assertThat(contractInfo.first().type).isEqualTo(AgreementType.NorwegianHomeContent)
        assertThat(contractInfo.first().switcherCompany).isEqualTo("someName")
    }

    @Test
    fun `sets sign source`() {
        val contractId = UUID.fromString("b43a96b2-a70c-11ea-ac39-3af9d3902f96")
        every { productPricingClient.getContractsForMember(any()) } returns ResponseEntity.ok(
            listOf(
                makeContract(makeNorwegianHomeContentAgreement(), signSource = "RAPIO", contractId = contractId)
            )
        )

        val quote = makeQuoteDto("A_PARTNER")
        every { underwriterClient.getQuoteFromContractId(contractId.toString()) } returns ResponseEntity.ok(quote)

        val sut =
            ContractLoaderImpl(
                productPricingClient,
                underwriterClient
            )

        val contractInfo = sut.getContractInfoForMember("someId")
        assertThat(contractInfo.first().partnerCode).isEqualTo("A_PARTNER")
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
        signSource: String? = null,
        contractId: UUID = UUID.randomUUID()
    ): Contract {
        return Contract(
            contractId,
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
