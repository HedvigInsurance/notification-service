package com.hedvig.notificationService.customerio.web

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isGreaterThan
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.SIGN_EVENT_WINDOWS_SIZE_MINUTES
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.ContractLoader
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Disabled
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

class OnContractCreatedEventTest {

    @MockK
    lateinit var contractLoader: ContractLoader

    private val repository = InMemoryCustomerIOStateRepository()
    lateinit var sut: EventHandler
    val scheduler: Scheduler = mockk()

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        ConfigurationProperties()
        val customerioService = mockk<CustomerioService>()
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>()
        sut = EventHandler(
            repo = repository,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService,
            scheduler = scheduler
        )
    }

    @Test
    fun onContractCreatedEvent() {

        val jobSlot = slot<JobDetail>()
        val triggerSot = slot<Trigger>()

        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSot)) } returns Date()

        val callTime = Instant.parse("2020-04-27T09:20:42.815351Z")

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                null
            ), callTime
        )

        assertThat(repository.data["1337"]?.contractCreatedTriggerAt).isEqualTo(callTime)

        assertThat(jobSlot.captured).all {
            transform { it.key.group }.isEqualTo("customerio.triggers")
            transform { it.key.name }.isEqualTo("onContractCreatedEvent-1337")
            transform { it.requestsRecovery() }.isTrue()
            transform { it.jobClass }.isEqualTo(ContractCreatedJob::class.java)
            transform { it.jobDataMap.get("memberId") }.isEqualTo("1337")
        }
        assertThat(triggerSot.captured).all {
            transform { it.key.group }.isEqualTo("customerio.triggers")
            transform { it.misfireInstruction }.isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
            transform { it.startTime }.isEqualTo(
                Date.from(
                    callTime.plus(
                        SIGN_EVENT_WINDOWS_SIZE_MINUTES,
                        ChronoUnit.MINUTES
                    )
                )
            )
        }
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

        val oldTrigger = TriggerBuilder
            .newTrigger()
            .startNow()
            .build()
        every { scheduler.getTrigger(any()) } returns oldTrigger

        val oldTriggerKeySlot = slot<TriggerKey>()
        val newTriggerSlot = slot<Trigger>()
        every { scheduler.rescheduleJob(capture(oldTriggerKeySlot), capture(newTriggerSlot)) } returns Date()

        val time = Instant.parse("2020-04-27T09:20:42.815351Z")
        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "someEventId",
                "1337",
                null
            ), time
        )

        assertThat(repository.data["1337"]?.contractCreatedTriggerAt).isEqualTo(stateCreatedAt)
        assertThat(oldTriggerKeySlot.captured).isEqualTo(
            TriggerKey(
                "onContractCreatedEvent-1337",
                "customerio.triggers"
            )
        )
        assertThat(newTriggerSlot.captured.startTime).isGreaterThan(oldTrigger.startTime)
    }

    @Test
    @Disabled
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
    @Disabled
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
    @Disabled
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
    @Disabled
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
