package com.hedvig.notificationService.customerio.customerioEvents

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.customerio.AgreementType
import com.hedvig.notificationService.customerio.ContractInfo
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
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
    lateinit var contractLoader: ContractLoader
    lateinit var sut: CustomerioEventCreatorImpl

    @get:Rule
    var thrown = ExpectedException.none()

    companion object {
        fun asMap(any: Any): Map<String, Any?> {
            return objectMapper.convertValue(any, Map::class.java)!! as Map<String, Any?>
        }

        val objectMapper: ObjectMapper = ObjectMapper()

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        null,
                        null,
                        "IOS",
                        "HEDVIG"
                    )
                ),
                mapOf("is_signed_innbo" to true)
            ),
            arrayOf(
                listOf(
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        "folksam",
                        null,
                        "IOS",
                        "HEDVIG"
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
                        LocalDate.of(2020, 3, 13),
                        "IOS",
                        "HEDVIG"
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
                        null,
                        "IOS",
                        "HEDVIG"
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
                        null,
                        "IOS",
                        "HEDVIG"
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
                        LocalDate.of(2020, 1, 1),
                        "IOS",
                        "HEDVIG"
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
                        null,
                        "IOS",
                        "HEDVIG"
                    ),
                    ContractInfo(
                        AgreementType.NorwegianHomeContent,
                        null,
                        null,
                        "IOS",
                        "HEDVIG"
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

        val event = asMap(sut.createTmpSignedInsuranceEvent(contracts))

        assertThat(event["name"]).isEqualTo("TmpSignedInsuranceEvent")
        val eventData = event["data"] as Map<String, Any?>

        assertAttributeWithValues(eventData, "is_signed_innbo")
        assertAttributeWithValues(eventData, "activation_date_innbo")
        assertAttributeWithValues(eventData, "is_switcher_innbo")
        assertAttributeWithValues(eventData, "switcher_company_innbo")
        assertAttributeWithValues(eventData, "activation_date_innbo")
        assertAttributeWithValues(eventData, "is_switcher_reise")
        assertAttributeWithValues(eventData, "switcher_company_reise")
        assertAttributeWithValues(eventData, "activation_date_reise")
    }

    @Test
    fun `test ContractCreatedEvent`() {
        val customerioState = CustomerioState(
            "42",
            Instant.now(),
            false
        )

        val event = asMap(sut.createContractCreatedEvent(contracts))

        assertThat(event["name"]).isEqualTo("NorwegianContractCreatedEvent")
        val eventData = event["data"] as Map<String, Any?>
        assertAttributeWithValues(eventData, "is_signed_innbo")
        assertAttributeWithValues(eventData, "activation_date_innbo")
        assertAttributeWithValues(eventData, "is_switcher_innbo")
        assertAttributeWithValues(eventData, "switcher_company_innbo")
        assertAttributeWithValues(eventData, "activation_date_innbo")
        assertAttributeWithValues(eventData, "is_switcher_reise")
        assertAttributeWithValues(eventData, "switcher_company_reise")
        assertAttributeWithValues(eventData, "activation_date_reise")
    }

    private fun assertAttributeWithValues(
        eventData: Map<String, Any?>,
        attributename: String
    ) {
        if (values[attributename] != null)
            assertThat(eventData[attributename]).isEqualTo(values[attributename])
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
                null,
                "IOS",
                "HEDVIG"
            )
        )

        val java = RuntimeException::class.java
        thrown.expect(java)

        sut.createTmpSignedInsuranceEvent(contracts)
    }
}
