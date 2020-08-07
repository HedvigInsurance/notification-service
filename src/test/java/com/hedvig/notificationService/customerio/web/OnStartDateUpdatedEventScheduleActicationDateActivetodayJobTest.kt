package com.hedvig.notificationService.customerio.web

import assertk.Assert
import assertk.assertThat
import assertk.assertions.any
import assertk.assertions.support.expected
import assertk.assertions.support.show
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.customerioEvents.jobs.ContractActivatedTodayJob
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.Trigger
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class SchedulingOfActivationDateActiveTodayJobTest {

    val repo: InMemoryCustomerIOStateRepository = InMemoryCustomerIOStateRepository(mapOf())
    val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    val scheduler = mockk<Scheduler>()
    val sut: EventHandler =
        EventHandler(
            repo,
            firebaseNotificationService,
            customerioService,
            memberService,
            scheduler
        )

    @Test
    fun `startDate updated event schedules job`() {

        val capturedJobDetails = mutableListOf<JobDetail>()
        val triggerSlot = mutableListOf<Trigger>()
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
}

fun Assert<Trigger>.matches(
    name: String,
    startDate: Date
) = given { actual ->

    if (
        name == actual.key.name &&
        startDate == actual.startTime
    ) return@given

    expected("${show(actual)} did not match expected")
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
