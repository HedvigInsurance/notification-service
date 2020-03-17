package com.hedvig.notificationService.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.CustomerioMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class WebIntegrationTestConfig {

    @Bean
    fun customerIO(objectMapper: ObjectMapper): CustomerioMock {
        return CustomerioMock(objectMapper)
    }
}
