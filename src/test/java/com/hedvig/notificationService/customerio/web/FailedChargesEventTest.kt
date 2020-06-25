package com.hedvig.notificationService.customerio.web

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
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
import java.net.URI

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FailedChargesEventTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @MockkBean(relaxed = true)
    private lateinit var eventHandler: EventHandler

    @Test
    fun failedChargesSent() {
        val url = URI("http://localhost:$port/_/events/chargeFailed")
        val body = mapOf(
            "numberOfFailedCharges" to 1,
            "numberOfChargesLeft" to 2,
            "terminationDate" to null
        )

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    @Test
    fun failedChargesSentCallesEventHandler() {
        val url = URI("http://localhost:$port/_/events/chargeFailed")
        val body = mapOf(
            "numberOfFailedCharges" to 1,
            "numberOfChargesLeft" to 2,
            "terminationDate" to null
        )

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        verify { eventHandler.onFailedChargeEvent(ChargeFailedEvent(1, 2, null)) }
    }
}
