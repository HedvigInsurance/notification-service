package com.hedvig.notificationService.customerio.web

import assertk.Assert
import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.isEqualTo
import assertk.assertions.none
import assertk.assertions.support.expected
import assertk.assertions.support.show
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractActivatedTodayJob
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.event.ContractCreatedEvent
import com.hedvig.notificationService.service.event.EventHandler
import com.hedvig.notificationService.service.event.StartDateUpdatedEvent
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class SchedulingOfActivationDateActiveTodayJobTest {

    val repo: InMemoryCustomerIOStateRepository = InMemoryCustomerIOStateRepository(mapOf())
    val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    val scheduler = mockk<Scheduler>(relaxed = true)
    val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)
    val sut: EventHandler =
        EventHandler(
            repo,
            firebaseNotificationService,
            customerioService,
            memberService,
            scheduler,
            handledRequestRepository
        )

    @Test
    fun `contract created without start date do not schedule job`() {
        val capturedJobDetails = mutableListOf<JobDetail>()
        val triggerSlot = mutableListOf<Trigger>()
        every { scheduler.scheduleJob(capture(capturedJobDetails), capture(triggerSlot)) } returns Date()

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "aContractId",
                "aMemberId",
                null
            )
        )

        assertThat(capturedJobDetails).none {
            it.transform { jobDetail -> jobDetail.key.name }.isEqualTo("contractActivatedTodayJob-aContractId")
        }
    }

    @Test
    fun `contract created event schedules job`() {

        val capturedJobDetails = mutableListOf<JobDetail>()
        val triggerSlot = mutableListOf<Trigger>()
        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(capture(capturedJobDetails), capture(triggerSlot)) } returns Date()

        sut.onContractCreatedEvent(
            ContractCreatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 9, 1)
            )
        )

        assertThat(capturedJobDetails).any {
            it.matches(
                "contractActivatedTodayJob-aMemberId",
                ContractActivatedTodayJob::class.java,
                mapOf("memberId" to "aMemberId")
            )
        }

        assertThat(triggerSlot).any {
            it.matches(
                "contractActivatedTodayJob-aMemberId",
                Date.from(LocalDate.of(2020, 9, 1).atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant())
            )
        }
    }

    @Test
    fun `start date updated event schedules job`() {

        val capturedJobDetails = mutableListOf<JobDetail>()
        val triggerSlot = mutableListOf<Trigger>()
        every { scheduler.getTrigger(any()) } returns null
        every { scheduler.scheduleJob(capture(capturedJobDetails), capture(triggerSlot)) } returns Date()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 9, 1)
            )
        )

        assertThat(capturedJobDetails).any {
            it.matches(
                "contractActivatedTodayJob-aContractId",
                ContractActivatedTodayJob::class.java,
                mapOf("memberId" to "aMemberId")
            )
        }

        assertThat(triggerSlot).any {
            it.matches(
                "contractActivatedTodayJob-aContractId",
                Date.from(LocalDate.of(2020, 9, 1).atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant())
            )
        }
    }

    @Test
    fun `start date updated event reschedules job`() {

        val capturedJobDetails = mutableListOf<TriggerKey>()
        val triggerSlot = mutableListOf<Trigger>()
        every { scheduler.rescheduleJob(any(), any()) } returns Date()

        every { scheduler.getTrigger(any()) } returns TriggerBuilder
            .newTrigger()
            .withIdentity("contractActivatedTodayJob-aContractId", "customerio.triggers")
            .build()

        sut.onStartDateUpdatedEvent(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 9, 1)
            )
        )

        verify { scheduler.rescheduleJob(capture(capturedJobDetails), capture(triggerSlot)) }
        assertThat(triggerSlot).any {
            it.matches(
                "contractActivatedTodayJob-aContractId",
                Date.from(LocalDate.of(2020, 9, 1).atStartOfDay(ZoneId.of("Europe/Stockholm")).toInstant())
            )
        }
    }
}

fun Assert<Trigger>.matches(
    name: String,
    startTime: Date
) = given { actual ->

    if (
        name == actual.key.name &&
        startTime == actual.startTime
    ) return@given

    expected(
        "Trigger with name ${show(actual.key.name)} and startTime ${show(actual.startTime)} did not match ${show(
            name
        )} and $startTime"
    )
}

fun Assert<JobDetail>.matches(
    name: String,
    clas: Class<*>,
    data: Map<String, String>
) = given { actual ->

    if (
        name == actual.key.name &&
        actual.jobClass == clas &&
        data.all { pair ->
            actual.jobDataMap.getString(pair.key) == pair.value
        }
    ) return@given

    expected("${show(actual)} did not match expected")
}
