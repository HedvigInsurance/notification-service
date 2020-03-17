package com.hedvig.notificationService.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import java.net.URI

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CustomerIOPostDataTest {

    @LocalServerPort
    var port: Int = 0

    @Autowired
    lateinit var testRestTemplate: TestRestTemplate

    @Test
    fun postSimpleData() {

        val memberId = "1337"
        val url = URI("http://localhost:$port/_/customerio/$memberId")
        val body = mapOf("id" to memberId, "key" to "someKey")

        val response = testRestTemplate.postForEntity(url, HttpEntity(body), String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.ACCEPTED)
    }
}