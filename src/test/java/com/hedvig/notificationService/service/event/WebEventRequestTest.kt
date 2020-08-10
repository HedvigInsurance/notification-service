package com.hedvig.notificationService.service.event

import com.fasterxml.jackson.databind.ObjectMapper
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

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class WebEventRequestTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate
    @Autowired
    lateinit var mapper: ObjectMapper

    @MockkBean(relaxed = true)
    private lateinit var eventRequestHandler: EventRequestHandler

    val testEvent: Map<String, Any> = mapOf(
        "name" to "TestEvent",
        "data" to mapOf<String, Any>(
            "memberId" to "1234"
        )
    )

    @Test
    fun `verify event request is handled`() {
        val url = URI("http://localhost:$port/_/events/request")

        val body = testEvent
        val requestId = "requestId"
        val headers = CollectionUtils.toMultiValueMap(mapOf("Request-Id" to listOf(requestId)))
        val response = testRestTemplate.postForEntity(url, HttpEntity(body, headers), String::class.java)

        verify {
            eventRequestHandler.onEventRequest(requestId, mapper.valueToTree(body))
        }

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }
}
