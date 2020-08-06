package com.hedvig.notificationService.customerio.web

import assertk.all
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.customerioEvents.jobs.StartDateUpdatedJob
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Date

class OnStartDateUpdatedEventTest {

    lateinit var repo: InMemoryCustomerIOStateRepository

    val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    val scheduler = mockk<Scheduler>()
    lateinit var configuration: ConfigurationProperties
    lateinit var sut: EventHandler

    @BeforeEach
    fun setup() {
        repo = InMemoryCustomerIOStateRepository()
        configuration = ConfigurationProperties()
        sut = EventHandler(
            repo,
            firebaseNotificationService,
            customerioService,
            memberService,
            scheduler
        )
    }

    @Test
    fun `on start date updated event`() {
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val time = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)), time)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isEqualTo(time)
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 5, 3))
    }

    @Test
    fun `post job to quartz`() {
        val callTime = Instant.parse("2020-04-27T14:03:23.337770Z")

        val jobSlot = slot<JobDetail>()
        val triggerSot = slot<Trigger>()

        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSot)) } returns Date()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)),
            callTime
        )

        assertThat(jobSlot.captured).all {
            transform { it.key.group }.isEqualTo("customerio.triggers")
            transform { it.requestsRecovery() }.isTrue()
            transform { it.jobClass }.isEqualTo(StartDateUpdatedJob::class.java)
            transform { it.jobDataMap.get("memberId") }.isEqualTo("aMemberId")
        }
        assertThat(triggerSot.captured).all {
            transform { it.key.group }.isEqualTo("customerio.triggers")
            transform { it.misfireInstruction }.isEqualTo(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW)
            transform { it.startTime }.isEqualTo(Date.from(callTime.plus(10, ChronoUnit.MINUTES)))
        }
    }

    @Test
    fun `on start date updated event when startDate extists`() {

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        repo.save(CustomerioState("aMemberId", null, startDateUpdatedTriggerAt = timeOfFirstCall))

        every { scheduler.scheduleJob(any(), any()) } returns Date()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)),
            timeOfFirstCall.plusMillis(3000)
        )

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isEqualTo(timeOfFirstCall)
    }

    @Test
    fun `with existing state set activation date trigger to startdate`() {

        repo.save(
            CustomerioState(
                memberId = "aMemberId",
                underwriterFirstSignAttributesUpdate = null,
                startDateUpdatedTriggerAt = null,
                activationDateTriggerAt = LocalDate.of(2020, 4, 1)
            )
        )
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val timeOfCall = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 4, 3)
            ), timeOfCall
        )

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isEqualTo(timeOfCall)
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 4, 1))
    }

    @Test
    fun `without existing state set activation date trigger to startdate`() {

        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 4, 3)
            ), timeOfFirstCall.plusMillis(3000)
        )

        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 4, 3))
    }
}
