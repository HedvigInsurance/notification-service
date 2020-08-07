package com.hedvig.notificationService.service.event

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.ContractCreatedEvent
import com.hedvig.notificationService.customerio.dto.ContractRenewalQueuedEvent
import com.hedvig.notificationService.customerio.dto.QuoteCreatedEvent
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
import com.hedvig.notificationService.service.request.EventRequestHandler
import com.hedvig.notificationService.service.request.NoDataOnEventException
import com.hedvig.notificationService.service.request.NoNameOnEventException
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

class HandleEventRequestsTest {

    val eventHandler = mockk<EventHandler>(relaxed = true)

    val mapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())

    val serviceToTest = EventRequestHandler(eventHandler, mapper)

    @Test
    fun `start date update event`() {
        val contractId = "contractId"
        val memberId = "memberId"
        val startDate = "2020-11-02"
        val map = mapOf(
            "name" to "StartDateUpdatedEvent",
            "data" to mapOf(
                "contractId" to contractId,
                "owningMemberId" to memberId,
                "startDate" to startDate
            )
        )

        serviceToTest.onEventRequest(
            "unhandled request", mapper.valueToTree(map)
        )

        verify {
            eventHandler.onStartDateUpdatedEvent(
                StartDateUpdatedEvent(
                    contractId,
                    memberId,
                    LocalDate.parse(startDate)
                ), any()
            )
        }
    }

    @Test
    fun `charge failed event`() {
        val terminationDate = "2020-08-07"
        val numberOfFailedCharges = 1
        val chargesLeftBeforeTermination = null
        val chargeFailedReason = ChargeFailedReason.INSUFFICIENT_FUNDS
        val memberId = "1234"
        val map = mapOf(
            "name" to "ChargeFailedEvent",
            "data" to mapOf(
                "terminationDate" to terminationDate,
                "chargeFailedReason" to chargeFailedReason,
                "memberId" to memberId
            )
        )

        serviceToTest.onEventRequest(
            "unhandled request", mapper.valueToTree(map)
        )

        verify {
            eventHandler.onFailedChargeEvent(
                ChargeFailedEvent(
                    LocalDate.parse(terminationDate),
                    numberOfFailedCharges,
                    chargesLeftBeforeTermination,
                    chargeFailedReason,
                    memberId
                )
            )
        }
    }

    @Test
    fun `contract created event`() {
        val contractId = "contractId"
        val owningMemberId = "owningMemberId"
        val startDate = "2020-01-01"
        val signSource = null
        val map = mapOf(
            "name" to "ContractCreatedEvent",
            "data" to mapOf(
                "contractId" to contractId,
                "owningMemberId" to owningMemberId,
                "startDate" to startDate,
                "signSource" to signSource
            )
        )

        serviceToTest.onEventRequest(
            "unhandled request", mapper.valueToTree(map)
        )

        verify {
            eventHandler.onContractCreatedEvent(
                ContractCreatedEvent(
                    contractId,
                    owningMemberId,
                    LocalDate.parse(startDate),
                    signSource
                ),
                any()
            )
        }
    }

    @Test
    fun `contract renewal queued event`() {
        val contractId = "contractId"
        val contractType = "type of contract"
        val memberId = "memberId"
        val renewalQueuedAt = "2020-01-01"


        val map = mapOf(
            "name" to "ContractRenewalQueuedEvent",
            "data" to mapOf(
                "contractId" to contractId,
                "contractType" to contractType,
                "memberId" to memberId,
                "renewalQueuedAt" to renewalQueuedAt
            )
        )

        serviceToTest.onEventRequest(
            "unhandled request", mapper.valueToTree(map)
        )

        verify {
            eventHandler.onContractRenewalQueued(
                ContractRenewalQueuedEvent(
                    contractId,
                    contractType,
                    memberId,
                    LocalDate.parse(renewalQueuedAt)
                ),
                any()
            )
        }
    }

    @Test
    fun `quote created event`() {
        val memberId = "memberId"
        val quoteId = UUID.randomUUID()
        val firstName = "firstName"
        val lastName = "lastName"
        val postalCode = "1234"
        val email = "123"
        val ssn = "121212"
        val initiatedFrom = "now"
        val attributedTo = "hedvig"
        val productType = "type of product"
        val currentInsurer = "123"
        val price = BigDecimal.TEN
        val currency = "SEK"
        val originatingProductId = null

        val map = mapOf(
            "name" to "QuoteCreatedEvent",
            "data" to mapOf(
                "memberId" to memberId,
                "quoteId" to quoteId,
                "firstName" to firstName,
                "lastName" to lastName,
                "postalCode" to postalCode,
                "email" to email,
                "ssn" to ssn,
                "initiatedFrom" to initiatedFrom,
                "attributedTo" to attributedTo,
                "productType" to productType,
                "currentInsurer" to currentInsurer,
                "price" to price,
                "currency" to currency
            )
        )

        serviceToTest.onEventRequest(
            "unhandled request", mapper.valueToTree(map)
        )

        verify {
            eventHandler.onQuoteCreated(
                QuoteCreatedEvent(
                    memberId,
                    quoteId,
                    firstName,
                    lastName,
                    postalCode,
                    email,
                    ssn,
                    initiatedFrom,
                    attributedTo,
                    productType,
                    currentInsurer,
                    price,
                    currency,
                    originatingProductId
                ),
                any()
            )
        }
    }

    @Test
    fun `throw NoNameOnEventException when name is not set`() {
        val map = mapOf(
            "data" to mapOf(
                "somedata" to "somedata"
            )
        )

        assertThrows<NoNameOnEventException> {
            serviceToTest.onEventRequest("requestId", mapper.valueToTree(map))
        }
    }

    @Test
    fun `throw NoDataOnEventException when data is not set`() {
        val map = mapOf(
            "name" to "SomeEvent"
        )

        assertThrows<NoDataOnEventException> {
            serviceToTest.onEventRequest("requestId", mapper.valueToTree(map))
        }
    }
}