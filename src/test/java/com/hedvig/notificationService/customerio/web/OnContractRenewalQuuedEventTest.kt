package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.CustomerioState
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class OnContractRenewalQuuedEventTest {

    @Test
    internal fun `renewal queued test`() {
        val configurationProperties = ConfigurationProperties()
        configurationProperties.useNorwayHack = false
        val customerioService = mockk<CustomerioService>(relaxed = true)
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)

        val repo = InMemoryCustomerIOStateRepository()
        val sut = EventHandler(
            repo = repo,
            configuration = configurationProperties,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService
        )

        sut.onContractRenewalQueued(
            ContractRenewalQueuedEvent(
                "contractOne",
                "member",
                LocalDate.of(1989,2,17)
            )
        )

        val slot = slot<Map<String, Any>>()
        verify { customerioService.sendEvent("member", capture(slot)) }
        assertThat(slot.captured["name"]).isEqualTo("ContractRenewalQueuedEvent")
    }
}
