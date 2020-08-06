package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class OnContractCreatedEventTest {

    @MockK
    lateinit var contractLoader: ContractLoader

    private val repository = InMemoryCustomerIOStateRepository()
    lateinit var sut: EventHandler

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        val configuration = ConfigurationProperties()
        val customerioService = mockk<CustomerioService>()
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>()
        sut = EventHandler(
            repo = repository,
            configuration = configuration,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService
        )
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
        repository.save(
            CustomerioState(
                memberId = "1337",
                underwriterFirstSignAttributesUpdate = null,
                sentTmpSignEvent = false,
                contractCreatedTriggerAt = stateCreatedAt
            )
        )

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
    fun `state already exists`() {

        repository.save(
            CustomerioState(
                memberId = "1337",
                contractCreatedTriggerAt = null
            )
        )

        val callTime = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                null
            ), callTime
        )

        assertThat(repository.data["1337"]?.contractCreatedTriggerAt).isEqualTo(callTime)
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
