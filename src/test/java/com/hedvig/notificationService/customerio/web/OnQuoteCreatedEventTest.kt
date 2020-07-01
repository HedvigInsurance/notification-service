package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.Workspace
import com.hedvig.notificationService.customerio.WorkspaceSelector
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.objects.Partner
import com.hedvig.notificationService.customerio.dto.objects.ProductType
import com.hedvig.notificationService.customerio.dto.objects.QuoteInitiatedFrom
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.serviceIntegration.memberService.dto.MemberHasSignedBeforeRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OnQuoteCreatedEventTest {
    private val CALL_TIME = Instant.parse("2020-05-03T20:03:03.23123Z")
    private val MEMBER_ID = "123"
    private val SSN = "123456789"
    private val EMAIL = "test@hedvig.com"
    private val ordinaryEvent = QuoteCreatedEvent(
        quoteId = UUID.randomUUID(),
        email = EMAIL,
        ssn = SSN,
        initiatedFrom = QuoteInitiatedFrom.WEBONBOARDING,
        attributedTo = Partner.HEDVIG,
        productType = ProductType.APARTMENT,
        currentInsurer = null,
        price = BigDecimal("99"),
        currency = "SEK",
        originatingProductId = null,
        address = "Testvägen 1"
    )

    private val customerioService = mockk<CustomerioService>()
    private val memberService = mockk<MemberServiceImpl>()
    private val workspaceSelector = mockk<WorkspaceSelector>()

    lateinit var eventHandlerToTest: EventHandler

    @BeforeEach
    fun setup() {
        eventHandlerToTest = EventHandler(
            repo = mockk(),
            configuration = mockk(),
            clients = mapOf(),
            firebaseNotificationService = mockk(),
            workspaceSelector = workspaceSelector,
            memberService = memberService,
            customerioService = customerioService
        )
        every { workspaceSelector.getWorkspaceForMember(MEMBER_ID) } returns Workspace.SWEDEN
        every { customerioService.updateCustomerAttributes(MEMBER_ID, mapOf("email" to EMAIL), CALL_TIME) } returns Unit
    }

    @Test
    fun `send event when member is not signed and event is ordinary`() {
        every { memberService.hasSignedBefore(MemberHasSignedBeforeRequest(MEMBER_ID, SSN, EMAIL)) } returns false
        every { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) } returns Unit
        eventHandlerToTest.onQuoteCreated(MEMBER_ID, ordinaryEvent, CALL_TIME)
        verify { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) }
    }

    @Test
    fun `does not send event when member signed and event is ordinary`() {
        every { memberService.hasSignedBefore(MemberHasSignedBeforeRequest(MEMBER_ID, SSN, EMAIL)) } returns true
        every { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) } returns Unit
        eventHandlerToTest.onQuoteCreated(MEMBER_ID, ordinaryEvent, CALL_TIME)
        verify (inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, mapOf("email" to EMAIL), CALL_TIME) }
        verify (inverse = true) { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) }
    }

    @Test
    fun `does not send event when member has not signed and quote is created from hope`() {
        every { memberService.hasSignedBefore(MemberHasSignedBeforeRequest(MEMBER_ID, SSN, EMAIL)) } returns false
        every { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) } returns Unit
        val eventWithQuoteCreatedFromHope = ordinaryEvent.copy(initiatedFrom = QuoteInitiatedFrom.HOPE)
        eventHandlerToTest.onQuoteCreated(MEMBER_ID, eventWithQuoteCreatedFromHope, CALL_TIME)
        verify (inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify (inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }

    @Test
    fun `does not send event when member has not signed and quote has originating productId`() {
        every { memberService.hasSignedBefore(MemberHasSignedBeforeRequest(MEMBER_ID, SSN, EMAIL)) } returns false
        every { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) } returns Unit
        val eventWithQuoteWithOriginatingProductId = ordinaryEvent.copy(originatingProductId = UUID.randomUUID())
        eventHandlerToTest.onQuoteCreated(MEMBER_ID, eventWithQuoteWithOriginatingProductId, CALL_TIME)
        verify (inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify (inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }

    @Test
    fun `does not send event when member has not signed and quote has unknown productType`() {
        every { memberService.hasSignedBefore(MemberHasSignedBeforeRequest(MEMBER_ID, SSN, EMAIL)) } returns false
        every { customerioService.sendEvent(MEMBER_ID, ordinaryEvent.toMap(MEMBER_ID)) } returns Unit
        val eventWithQuoteWithOriginatingProductId = ordinaryEvent.copy(productType = ProductType.UNKNOWN)
        eventHandlerToTest.onQuoteCreated(MEMBER_ID, eventWithQuoteWithOriginatingProductId, CALL_TIME)
        verify (inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify (inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }
}