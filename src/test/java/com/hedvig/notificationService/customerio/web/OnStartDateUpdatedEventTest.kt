package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.isNull
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.JobDetail
import org.quartz.Scheduler
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
        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val time = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)), time)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isNull()
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isNull()
    }

    @Test
    fun `post job to quartz`() {
        val callTime = Instant.parse("2020-04-27T14:03:23.337770Z")

        val jobSlot = mutableListOf<JobDetail>()
        val triggerSot = mutableListOf<Trigger>()

        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(capture(jobSlot), capture(triggerSot)) } returns Date()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)),
            callTime
        )

        assertThat(jobSlot).any {
            it.matches(
                "onStartDateUpdatedEvent+aMemberId",
                StartDateUpdatedJob::class.java,
                mapOf("memberId" to "aMemberId")
            )
        }
        assertThat(triggerSot).any {
            it.matches("onStartDateUpdatedEvent+aMemberId", Date.from(callTime.plus(10, ChronoUnit.MINUTES)))
        }
    }

    @Test
    fun `on start date updated event when startDate extists`() {

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        repo.save(CustomerioState("aMemberId", null, startDateUpdatedTriggerAt = timeOfFirstCall))

        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)),
            timeOfFirstCall.plusMillis(3000)
        )

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isNull()
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
        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val timeOfCall = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 4, 3)
            ), timeOfCall
        )

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isNull()
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isNull()
    }

    @Test
    fun `without existing state set activation date trigger to startdate`() {

        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(any(), any()) } returns Date()

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 4, 3)
            ), timeOfFirstCall.plusMillis(3000)
        )

        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isNull()
    }
}
