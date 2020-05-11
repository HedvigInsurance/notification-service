package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.ProductPricingFacade
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class OnContractCreatedEventTest {

    @MockK
    lateinit var productPricingFacade: ProductPricingFacade

    private val repository = InMemoryCustomerIOStateRepository()
    lateinit var sut: EventHandler

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val configuration = ConfigurationProperties()
        configuration.useNorwayHack = false
        sut = EventHandler(repository, configuration)
    }

    @Test
    fun onContractCreatedEvent() {

        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                null
            ), time
        )

        assertThat(repository.data["1337"]?.contractCreatedTriggerAt).isEqualTo(time)
    }

    @Test
    fun `contract already created`() {

        val stateCreatedAt = Instant.parse("2020-04-27T09:20:42.815351Z").minusMillis(3000)
        repository.save(CustomerioState("1337", null, false, stateCreatedAt))

        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                null
            ), time
        )

        assertThat(repository.data["1337"]?.contractCreatedTriggerAt).isEqualTo(stateCreatedAt)
    }

    @Test
    fun `contract with activation date`() {

        val stateCreatedAt = Instant.parse("2020-04-27T09:20:42.815351Z").minusMillis(3000)
        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                LocalDate.of(2020, 5, 4)
            ), time
        )

        assertThat(repository.data["1337"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 5, 4))
    }

    @Test
    fun `contract with activation date later than existing activation date`() {

        val stateCreatedAt = Instant.parse("2020-04-27T09:20:42.815351Z").minusMillis(3000)
        repository.save(
            CustomerioState(
                "1337",
                contractCreatedTriggerAt = stateCreatedAt,
                activationDateTriggerAt =
                LocalDate.of(2020, 1, 1)
            )
        )
        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                LocalDate.of(2020, 5, 4)
            ), time
        )

        assertThat(repository.data["1337"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 1, 1))
    }

    @Test
    fun `contract with activation date yearlier than existing activation date`() {

        val stateCreatedAt = Instant.parse("2020-04-27T09:20:42.815351Z").minusMillis(3000)
        repository.save(
            CustomerioState(
                "1337",
                contractCreatedTriggerAt = stateCreatedAt,
                activationDateTriggerAt =
                LocalDate.of(2020, 5, 2)
            )
        )
        val time = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                LocalDate.of(2020, 5, 1)
            ), time
        )

        assertThat(repository.data["1337"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 5, 1))
    }

    @Test
    fun `do not send duplicates emails if norwegian sign hack is triggered`() {
        val stateCreatedAt = Instant.parse("2020-04-27T09:20:42.815351Z").minusMillis(3000)
        repository.save(
            CustomerioState(
                "1337",
                underwriterFirstSignAttributesUpdate = stateCreatedAt
            )
        )

        val time = Instant.parse("2020-04-27T09:20:42.815351Z")
        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                LocalDate.of(2020, 5, 1)
            ), time
        )

        assertThat(repository.data["1337"]?.activationDateTriggerAt).isNull()
    }
}
