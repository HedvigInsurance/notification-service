package com.hedvig.notificationService.service

import com.hedvig.customerio.CustomerioMock
import java.net.URI
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

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(WebIntegrationTestConfig::class)
class CustomerIOPostDataTest {

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
        val body = mapOf("id" to memberId, "key" to "someKey")

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }

    @Test
    fun postedDataForwardedToCustomerIO() {
        val memberId = "1337"
        val url = URI("http://localhost:$port/_/customerio/$memberId")
        val body = mapOf("id" to memberId, "key" to "someKey")
        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(customerioMock.updates[0].first).isEqualTo(memberId)
    }
}
