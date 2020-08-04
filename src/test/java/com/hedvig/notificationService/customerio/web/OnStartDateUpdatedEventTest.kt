package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class OnStartDateUpdatedEventTest {

    lateinit var repo: InMemoryCustomerIOStateRepository

    val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    lateinit var configuration: ConfigurationProperties
    lateinit var sut: EventHandler

    @BeforeEach
    fun setup() {
        repo = InMemoryCustomerIOStateRepository()
        configuration = ConfigurationProperties()
        sut = EventHandler(
            repo,
            configuration,
            firebaseNotificationService,
            customerioService,
            memberService
        )
    }

    @Test
    fun `on start date updated event`() {
        val time = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEvent(StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)), time)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isEqualTo(time)
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 5, 3))
    }

    @Test
    fun `on start date updated event when startDate extists`() {

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        repo.save(CustomerioState("aMemberId", null, startDateUpdatedTriggerAt = timeOfFirstCall))

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
