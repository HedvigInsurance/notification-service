package com.hedvig.notificationService.customerio.events

import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.CustomerioState
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.time.Instant
import java.time.LocalDate

@RunWith(Parameterized::class)
class TmpSignAndContractCreatedEventTest(
    val contracts: List<ContractInfo>,
    val values: Map<String, Any>
) {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade
    lateinit var sut: CustomerioEventCreatorImpl

    @get:Rule
    var thrown = ExpectedException.none()

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        null,
                        null
                    )
                ),
                mapOf("is_signed_innbo" to true)
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        "folksam",
                        null
                    )
                ), mapOf(
                    "is_signed_innbo" to true,
                    "is_switcher_innbo" to true,
                    "switcher_company_innbo" to "folksam"
                )
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        null,
                        LocalDate.of(2020, 3, 13)
                    )
                ),
                mapOf(
                    "is_signed_innbo" to true,
                    "activation_date_innbo" to "2020-03-13"
                )
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianTravel,
                        null,
                        null
                    )
                ), mapOf(
                    "is_signed_reise" to true
                )
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianTravel,
                        "a new company",
                        null
                    )
                ),
                mapOf(
                    "is_signed_reise" to true,
                    "is_switcher_reise" to true,
                    "switcher_company_reise" to "a new company"
                )
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianTravel,
                        "a new company",
                        LocalDate.of(2020, 1, 1)
                    )
                ),
                mapOf(
                    "is_signed_reise" to true,
                    "is_switcher_reise" to true,
                    "switcher_company_reise" to "a new company",
                    "activation_date_reise" to "2020-01-01"
                )
            ),
            arrayOf(
                listOf(
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
                ),
                mapOf(
                    "is_signed_reise" to true,
                    "is_signed_innbo" to true
                )
            )
        )
    }

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        sut =
            CustomerioEventCreatorImpl()
    }

    @Test
    fun `test TmpSignedInsuranceEvent`() {
        val customerioState = CustomerioState(
            "42",
            Instant.now(),
            false
        )

        val event = sut.createTmpSignedInsuranceEvent(customerioState, contracts)

        assertThat(event["name"]).isEqualTo("TmpSignedInsuranceEvent")
        val eventData = event["data"] as Map<String, Any?>
        assertThat(eventData["is_signed_innbo"]).isEqualTo(values["is_signed_innbo"])
        assertThat(eventData["activation_date_innbo"]).isEqualTo(values["activation_date_innbo"])
        assertThat(eventData["is_switcher_innbo"]).isEqualTo(values["is_switcher_innbo"])
        assertThat(eventData["switcher_company_innbo"]).isEqualTo(values["switcher_company_innbo"])
        assertThat(eventData["activation_date_innbo"]).isEqualTo(values["activation_date_innbo"])
        assertThat(eventData["is_switcher_reise"]).isEqualTo(values["is_switcher_reise"])
        assertThat(eventData["switcher_company_reise"]).isEqualTo(values["switcher_company_reise"])
        assertThat(eventData["activation_date_reise"]).isEqualTo(values["activation_date_reise"])
    }

    @Test
    fun `test ContractCreatedEvent`() {
        val customerioState = CustomerioState(
            "42",
            Instant.now(),
            false
        )

        val event = sut.createContractCreatedEvent(customerioState, contracts)

        assertThat(event["name"]).isEqualTo("NorwegianContractCreatedEvent")
        val eventData = event["data"] as Map<String, Any?>
        assertThat(eventData["is_signed_innbo"]).isEqualTo(values["is_signed_innbo"])
        assertThat(eventData["activation_date_innbo"]).isEqualTo(values["activation_date_innbo"])
        assertThat(eventData["is_switcher_innbo"]).isEqualTo(values["is_switcher_innbo"])
        assertThat(eventData["switcher_company_innbo"]).isEqualTo(values["switcher_company_innbo"])
        assertThat(eventData["activation_date_innbo"]).isEqualTo(values["activation_date_innbo"])
        assertThat(eventData["is_switcher_reise"]).isEqualTo(values["is_switcher_reise"])
        assertThat(eventData["switcher_company_reise"]).isEqualTo(values["switcher_company_reise"])
        assertThat(eventData["activation_date_reise"]).isEqualTo(values["activation_date_reise"])
    }

    @Test
    fun `swedish house throws exception`() {
        val customerioState = CustomerioState(
            "42",
            Instant.now(),
            false
        )
        val contracts = listOf(
            ContractInfo(
                AgreementType.SwedishHouse,
                null,
                null
            )
        )

        val java = RuntimeException::class.java
        thrown.expect(java)

        sut.createTmpSignedInsuranceEvent(customerioState, contracts)
    }
}
