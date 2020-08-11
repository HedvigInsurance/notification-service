package com.hedvig.notificationService.service.event

import com.hedvig.notificationService.service.request.EventRequestHandler
import com.ninjasquad.springmockk.MockkBean
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
class WebEventRequestTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockkBean(relaxed = true)
    private lateinit var eventRequestHandler: EventRequestHandler

    val testEvent = mapOf(
        "eventName" to "StartDateUpdatedEvent",
        "contractId" to UUID.randomUUID(),
        "owningMemberId" to "12345",
        "startDate" to LocalDate.now()
    )

    @Test
    fun `verify event request is handled`() {
        val url = URI("http://localhost:$port/_/event")

        val body = testEvent
        val requestId = "requestId"
        val headers = CollectionUtils.toMultiValueMap(mapOf("Request-Id" to listOf(requestId)))
        val response = testRestTemplate.postForEntity(url, HttpEntity(body, headers), String::class.java)

        verify {
            eventRequestHandler.onEventRequest(
                requestId,
                StartDateUpdatedEvent(
                    testEvent["contractId"].toString(),
                    testEvent["owningMemberId"].toString(),
                    LocalDate.parse(testEvent["startDate"].toString())
                )
            )
        }

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }
}
