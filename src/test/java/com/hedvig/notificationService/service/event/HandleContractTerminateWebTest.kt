package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.service.request.HandledRequestRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.CollectionUtils
import java.net.URI
import java.time.LocalDate
import java.util.UUID

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class HandleContractTerminateWebTest {
    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockkBean(relaxed = true)
    private lateinit var eventRequestHandler: EventHandler

    @MockkBean(relaxed = true)
    private lateinit var handledRequestRepository: HandledRequestRepository

    val testEvent = mapOf(
        "eventName" to "ContractTerminatedEvent",
        "contractId" to UUID.randomUUID(),
        "owningMemberId" to "12345",
        "terminationDate" to LocalDate.now(),
        "isFinalContract" to true
    )

    @Test
    fun `verify event request is handled`() {
        val url = URI("http://localhost:$port/_/event")

        val body = testEvent
        val requestId = "requestId"
        val headers = CollectionUtils.toMultiValueMap(mapOf("Request-Id" to listOf(requestId)))
        val response = testRestTemplate.postForEntity(url, HttpEntity(body, headers), String::class.java)

        every { handledRequestRepository.isRequestHandled(any()) } returns true

        verify {
            eventRequestHandler.onContractTerminatedEvent(
                ContractTerminatedEvent(
                    testEvent["contractId"].toString(),
                    testEvent["owningMemberId"].toString(),
                    LocalDate.parse(
                        testEvent["terminationDate"].toString()
                    ),
                    true
                ),
                any()
            )
        }

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }
}
