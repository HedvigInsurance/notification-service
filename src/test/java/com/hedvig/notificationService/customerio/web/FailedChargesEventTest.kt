package com.hedvig.notificationService.customerio.web

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.ChargeFailedEvent
import com.hedvig.notificationService.customerio.dto.objects.ChargeFailedReason
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
import java.time.LocalDate

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
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")
        val body = makeJsonWithAttributes()

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    @Test
    fun failedChargesSentCallesEventHandler() {
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")
        val body = makeJsonWithAttributes()

        testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        verify {
            eventHandler.onFailedChargeEvent(
                "1227",
                ChargeFailedEvent(
                    terminationDate = null,
                    numberOfFailedCharges = 1,
                    chargesLeftBeforeTermination = 2,
                    chargeFailedReason = ChargeFailedReason.INSUFFICIENT_FUNDS
                )
            )
        }
    }

    @Test
    fun `return 400 if not all attribues of json are included`() {
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")
        val jsonWithoutAttributes = mapOf<Any, Any>()

        val response = testRestTemplate.postForEntity(url, HttpEntity(jsonWithoutAttributes), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `return 400 if numberOfFailedCharges is negative`() {
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")
        val body = makeJsonWithAttributes(
            numberOfFailedCharges = -1,
            numberOfChargesLeft = 0
        )

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `return 400 if numberOfChargesLeft is negative`() {
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")

        val body = makeJsonWithAttributes(
            numberOfFailedCharges = 0,
            numberOfChargesLeft = -1
        )

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `return 200 even if numberOfChargesLeft and numberOfFailedCharges is null`() {
        val url = URI("http://localhost:$port/_/events/1227/chargeFailed")
        val body = makeJsonWithAttributes(
            terminationDate = LocalDate.now(),
            numberOfChargesLeft = null,
            numberOfFailedCharges = null
        )

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        Assertions.assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    private fun makeJsonWithAttributes(
        memberId: String = "1227",
        numberOfFailedCharges: Int? = 1,
        numberOfChargesLeft: Int? = 2,
        terminationDate: LocalDate? = null,
        terminationReason: String = "INSUFFICIENT_FUNDS"
    ) = mapOf(
        "memberId" to memberId,
        "numberOfFailedCharges" to numberOfFailedCharges,
        "chargesLeftBeforeTermination" to numberOfChargesLeft,
        "terminationDate" to terminationDate,
        "chargeFailedReason" to terminationReason
    )
}
