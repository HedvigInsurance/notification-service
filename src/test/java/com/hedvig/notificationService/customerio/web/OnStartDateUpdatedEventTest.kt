package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class OnStartDateUpdatedEventTest {

    lateinit var repo: InMemoryCustomerIOStateRepository

    val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)
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
            memberService,
            handledRequestRepository
        )
    }

    @Test
    fun `on start date with requst id updated event and store handled request`() {
        val requestId = "a unhandled request"
        val time = Instant.parse("2020-04-27T14:03:23.337770Z")
        sut.onStartDateUpdatedEventHandleRequest(StartDateUpdatedEvent("aContractId", "aMemberId", LocalDate.of(2020, 5, 3)), time, requestId)

        assertThat(repo.data["aMemberId"]?.startDateUpdatedTriggerAt).isEqualTo(time)
        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 5, 3))
        verify { handledRequestRepository.storeHandledRequest(requestId) }
    }

    @Test
    fun `on start date updated event when startDate extists`() {

        val timeOfFirstCall = Instant.parse("2020-04-27T14:03:23.337770Z")

        repo.save(CustomerioState("aMemberId", null, startDateUpdatedTriggerAt = timeOfFirstCall))

        sut.onStartDateUpdatedEventHandleRequest(
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
        sut.onStartDateUpdatedEventHandleRequest(
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

        sut.onStartDateUpdatedEventHandleRequest(
            StartDateUpdatedEvent(
                "aContractId",
                "aMemberId",
                LocalDate.of(2020, 4, 3)
            ), timeOfFirstCall.plusMillis(3000)
        )

        assertThat(repo.data["aMemberId"]?.activationDateTriggerAt).isEqualTo(LocalDate.of(2020, 4, 3))
    }

    @Test
    fun `on handled request nothing is stored`() {
        val requestId = "a handled request"
        every { handledRequestRepository.isRequestHandled(requestId) } returns true

        val memberId = "handledMemberId"
        sut.onStartDateUpdatedEventHandleRequest(
            StartDateUpdatedEvent(
                "aContractId",
                memberId,
                LocalDate.of(2020, 4, 3)
            ),
            Instant.now(),
            requestId
        )

        assertThat(repo.data[memberId]).isNull()
    }
}
