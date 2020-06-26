package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.customerio.CustomerioClient
import com.hedvig.notificationService.customerio.ConfigurationProperties
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ChargeFailedReason
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
        configurationProperties.useNorwayHack = false
        val firebaseNotificationService = mockk<FirebaseNotificationService>(relaxed = true)
        val workspaceSelector = mockk<WorkspaceSelector>()

        val sweClient = mockk<CustomerioClient>(relaxed = true)
        val noClient = mockk<CustomerioClient>(relaxed = true)

        val repo = InMemoryCustomerIOStateRepository()
        val sut = EventHandler(
            repo,
            configurationProperties,
            mapOf(
                Workspace.SWEDEN to sweClient,
                Workspace.NORWAY to noClient
            ),
            firebaseNotificationService,
            workspaceSelector
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
        verify { sweClient.sendEvent("1227", capture(slot)) }
        assertThat(slot.captured["name"]).isEqualTo("ChargeFailedEvent")
    }
}
