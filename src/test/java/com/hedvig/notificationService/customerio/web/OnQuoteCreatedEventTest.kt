package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.CustomerioService
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.builders.EMAIL
import com.hedvig.notificationService.customerio.builders.MEMBER_ID
import com.hedvig.notificationService.customerio.builders.SSN
import com.hedvig.notificationService.customerio.builders.a
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.hedvigfacades.MemberServiceImpl
import com.hedvig.notificationService.service.request.HandledRequestRepository
import com.hedvig.notificationService.serviceIntegration.memberService.dto.HasPersonSignedBeforeRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class OnQuoteCreatedEventTest {
    private val CALL_TIME = Instant.parse("2020-05-03T20:03:03.23123Z")

    private val customerioService = mockk<CustomerioService>(relaxed = true)
    private val memberService = mockk<MemberServiceImpl>()
    private val handledRequestRepository = mockk<HandledRequestRepository>(relaxed = true)

    lateinit var eventHandlerToTest: EventHandler

    @BeforeEach
    fun setup() {
        eventHandlerToTest = EventHandler(
            repo = mockk(),
            configuration = mockk(),
            firebaseNotificationService = mockk(),
            customerioService = customerioService,
            memberService = memberService,
            handledRequestRepository = handledRequestRepository
        )
    }

    @Test
    fun `send event when member is not signed and event is ordinary`() {
        val requestId = "unhandled request"
        val quoteCreatedEvent = a.quoteCreatedEvent.build()
        every { memberService.hasPersonSignedBefore(HasPersonSignedBeforeRequest(SSN, EMAIL)) } returns false
        eventHandlerToTest.onQuoteCreated(quoteCreatedEvent, CALL_TIME, requestId)
        verify { customerioService.sendEvent(MEMBER_ID, quoteCreatedEvent.toMap()) }
        verify {
            customerioService.updateCustomerAttributes(
                MEMBER_ID, mapOf(
                    "email" to EMAIL,
                    "first_name" to quoteCreatedEvent.firstName,
                    "last_name" to quoteCreatedEvent.lastName
                ),
                CALL_TIME
            )
        }

        verify { handledRequestRepository.storeHandledRequest(requestId) }
    }

    @Test
    fun `does not send event when member signed and event is ordinary`() {
        val quoteCreatedEvent = a.quoteCreatedEvent.build()
        every { memberService.hasPersonSignedBefore(HasPersonSignedBeforeRequest(SSN, EMAIL)) } returns true
        eventHandlerToTest.onQuoteCreated(quoteCreatedEvent, CALL_TIME)
        verify(inverse = true) {
            customerioService.updateCustomerAttributes(
                MEMBER_ID,
                any(),
                CALL_TIME
            )
        }
        verify(inverse = true) { customerioService.sendEvent(MEMBER_ID, quoteCreatedEvent.toMap()) }
    }

    @Test
    fun `does not send event when member has not signed and quote is created from hope`() {
        every { memberService.hasPersonSignedBefore(HasPersonSignedBeforeRequest(SSN, EMAIL)) } returns false
        val eventWithQuoteCreatedFromHope = a.quoteCreatedEvent.copy(initiatedFrom = "HOPE").build()
        eventHandlerToTest.onQuoteCreated(eventWithQuoteCreatedFromHope, CALL_TIME)
        verify(inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify(inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }

    @Test
    fun `does not send event when member has not signed and quote has originating productId`() {
        every { memberService.hasPersonSignedBefore(HasPersonSignedBeforeRequest(SSN, EMAIL)) } returns false
        val eventWithQuoteWithOriginatingProductId =
            a.quoteCreatedEvent.copy(originatingProductId = UUID.randomUUID()).build()
        eventHandlerToTest.onQuoteCreated(eventWithQuoteWithOriginatingProductId, CALL_TIME)
        verify(inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify(inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }

    @Test
    fun `does not send event when member has not signed and quote has unknown productType`() {
        every { memberService.hasPersonSignedBefore(HasPersonSignedBeforeRequest(SSN, EMAIL)) } returns false
        val eventWithQuoteWithOriginatingProductId = a.quoteCreatedEvent.copy(productType = "UNKNOWN").build()
        eventHandlerToTest.onQuoteCreated(eventWithQuoteWithOriginatingProductId, CALL_TIME)
        verify(inverse = true) { customerioService.updateCustomerAttributes(MEMBER_ID, any(), CALL_TIME) }
        verify(inverse = true) { customerioService.sendEvent(MEMBER_ID, any()) }
    }


    @Test
    fun `handled request dose nothing`() {
        val requestId = "handled request id"
        every { handledRequestRepository.isRequestHandled(requestId) } returns true
        val quoteCreatedEvent = a.quoteCreatedEvent.build()
        eventHandlerToTest.onQuoteCreated(quoteCreatedEvent, CALL_TIME, requestId)

        verify(exactly = 0) { customerioService.sendEvent(any(), any()) }
        verify(exactly = 0) { customerioService.updateCustomerAttributes(any(), any(), any()) }
        verify(exactly = 0)  { handledRequestRepository.storeHandledRequest(any()) }
    }
}
