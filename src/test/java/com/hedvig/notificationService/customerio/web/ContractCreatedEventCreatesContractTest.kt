package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.containsOnly
import assertk.assertions.extracting
import assertk.assertions.isNotNull
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.service.event.EventHandler
import com.hedvig.notificationService.service.event.ContractCreatedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.firebase.FirebaseNotificationService
import com.hedvig.notificationService.service.request.HandledRequestRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.Scheduler
import java.time.Instant
import java.util.Date

class ContractCreatedEventCreatesContractTest {

    val configurationProperties = ConfigurationProperties()
    val repo = InMemoryCustomerIOStateRepository()
    val firebaseNotificationService = mockk<FirebaseNotificationService>()
    val customerioService = mockk<CustomerioService>()
    val memberService = mockk<MemberServiceImpl>()
    val handledRequestRepository = mockk<HandledRequestRepository>()
    val scheduler: Scheduler = mockk(relaxed = true)
    val sut = EventHandler(
        repo = repo,
        firebaseNotificationService = firebaseNotificationService,
        customerioService = customerioService,
        memberService = memberService,
        scheduler = scheduler,
        handledRequestRepository = handledRequestRepository,
        workspaceSelector = mockk()
    )

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { scheduler.scheduleJob(any(), any()) } returns Date()
    }

    @Test
    internal fun `after contract created with no existing contracts a contract exists`() {

        sut.onContractCreatedEventHandleRequest(
            ContractCreatedEvent(
                "theContractId",
                "aMemberId",
                null
            ),
            Instant.parse("2020-06-03T08:02:39.403803Z")
        )

        assertThat(repo.findByMemberId("aMemberId"))
            .isNotNull()
            .transform { it.contracts }
            .extracting { it.contractId }
            .containsExactly("theContractId")
    }

    @Test
    internal fun `after contract created with another existing contract the new contract exists`() {

        val customerioState = CustomerioState("aMemberId")
        customerioState.createContract(
            "theFirstContractId",
            Instant.parse("2020-06-02T08:02:39.403803Z"),
            null
        )
        repo.save(customerioState)

        sut.onContractCreatedEventHandleRequest(
            ContractCreatedEvent(
                "theNewContractId",
                "aMemberId",
                null
            ),
            Instant.parse("2020-06-03T08:02:39.403803Z")
        )

        assertThat(repo.findByMemberId("aMemberId"))
            .isNotNull()
            .transform { it.contracts }
            .extracting { it.contractId }
            .containsOnly("theNewContractId", "theFirstContractId")
    }

    @Test
    internal fun `after contract created with the same contract existing only one of that contract exists`() {

        val customerioState = CustomerioState("aMemberId")
        customerioState.createContract(
            "theFirstContractId",
            Instant.parse("2020-06-02T08:02:39.403803Z"),
            null
        )
        repo.save(customerioState)

        sut.onContractCreatedEventHandleRequest(
            ContractCreatedEvent(
                "theFirstContractId",
                "aMemberId",
                null
            ),
            Instant.parse("2020-06-03T08:02:39.403803Z")
        )

        assertThat(repo.findByMemberId("aMemberId"))
            .isNotNull()
            .transform { it.contracts }
            .extracting { it.contractId }
            .containsExactly("theFirstContractId")
    }

    /**
     * Start date is updated
     *
     */
}
