package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.customerio.state.InMemoryCustomerIOStateRepository
import com.hedvig.notificationService.service.FirebaseNotificationService
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test

class OnChargeFailedEventTriggerCustomerioEventTest {

    @Test
    internal fun `first test`() {
        val configurationProperties = ConfigurationProperties()
        val customerioService = mockk<CustomerioService>(relaxed = true)
        val memberService = mockk<MemberServiceImpl>()
        val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)

        val repo = InMemoryCustomerIOStateRepository()
        val sut = EventHandler(
            repo = repo,
            firebaseNotificationService = firebaseNotificationService,
            customerioService = customerioService,
            memberService = memberService,
            scheduler = mockk()
        )

        sut.onFailedChargeEvent(
            "1227",
            ChargeFailedEvent(
                null,
                1,
                2,
                ChargeFailedReason.INSUFFICIENT_FUNDS
            )
        )

        val slot = slot<Map<String, Any>>()
        verify { customerioService.sendEvent("1227", capture(slot)) }
        assertThat(slot.captured["name"]).isEqualTo("ChargeFailedEvent")
    }
}
