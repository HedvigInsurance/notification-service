package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OnUpdatePhoneNumberTest {

    private val customerioService = mockk<CustomerioService>()
    private val workspaceSelector = mockk<WorkspaceSelector>()

    val sut = EventHandler(
        repo = mockk(),
        firebaseNotificationService = mockk(),
        customerioService = customerioService,
        memberService = mockk(),
        scheduler = mockk(),
        handledRequestRepository = mockk(),
        workspaceSelector = workspaceSelector
    )

    @Test
    fun `on not formatted phone number update event send formatted phone number`() {
        every {
            workspaceSelector.getWorkspaceForMember(any())
        } returns Workspace.SWEDEN
        val slot = slot<Map<String, Any?>>()
        every {
            customerioService.updateCustomerAttributes(
                any(),
                capture(slot),
                any()
            )
        } returns Unit

        sut.onPhoneNumberUpdatedEvent(
            PhoneNumberUpdatedEvent(
                "123",
                "08123456"
            )
        )

        assertThat(slot.captured).containsKey("phone_number")
        assertThat(slot.captured["phone_number"]).isEqualTo("+468123456")
    }

    @Test
    fun `on formatted phone number update event send formatted phone number`() {
        every {
            workspaceSelector.getWorkspaceForMember(any())
        } returns Workspace.SWEDEN
        val slot = slot<Map<String, Any?>>()
        every {
            customerioService.updateCustomerAttributes(
                any(),
                capture(slot),
                any()
            )
        } returns Unit

        sut.onPhoneNumberUpdatedEvent(
            PhoneNumberUpdatedEvent(
                "123",
                "+468123456"
            )
        )

        assertThat(slot.captured).containsKey("phone_number")
        assertThat(slot.captured["phone_number"]).isEqualTo("+468123456")
    }

    @Test
    fun `on phone number with - and spaces update event send formatted phone number`() {
        every {
            workspaceSelector.getWorkspaceForMember(any())
        } returns Workspace.SWEDEN
        val slot = slot<Map<String, Any?>>()
        every {
            customerioService.updateCustomerAttributes(
                any(),
                capture(slot),
                any()
            )
        } returns Unit

        sut.onPhoneNumberUpdatedEvent(
            PhoneNumberUpdatedEvent(
                "123",
                "070 - 123 45 63"
            )
        )

        assertThat(slot.captured).containsKey("phone_number")
        assertThat(slot.captured["phone_number"]).isEqualTo("+46701234563")
    }

    @Test
    fun `on not formatted phone number in norway update event send formatted norwegian phone number`() {
        every {
            workspaceSelector.getWorkspaceForMember(any())
        } returns Workspace.NORWAY
        val slot = slot<Map<String, Any?>>()
        every {
            customerioService.updateCustomerAttributes(
                any(),
                capture(slot),
                any()
            )
        } returns Unit

        sut.onPhoneNumberUpdatedEvent(
            PhoneNumberUpdatedEvent(
                "123",
                "08123456"
            )
        )

        assertThat(slot.captured).containsKey("phone_number")
        assertThat(slot.captured["phone_number"]).isEqualTo("+478123456")
    }
}
