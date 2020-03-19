package com.hedvig.notificationService.customerio

import com.hedvig.customerio.CustomerioMock
import com.hedvig.notificationService.WebIntegrationTestConfig
import net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(WebIntegrationTestConfig::class)
class CustomerIOIntegrationTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Autowired
    lateinit var customerioMock: CustomerioMock

    @Test
    fun postSimpleData() {

        val memberId = "1337"
        val url = URI("http://localhost:$port/_/customerio/$memberId")
        val body = mapOf("key" to "someKey")

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    @Test
    fun postedDataForwardedToCorrectCustomer() {
        val memberId = "1337"

        val url = URI("http://localhost:$port/_/customerio/$memberId")
        val body = mapOf("key" to "someKey")
        testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(customerioMock.updates[0].first).isEqualTo(memberId)
        assertThatJson(customerioMock.updates[0].second).isObject.containsEntry("key", "someKey")
    }
}
