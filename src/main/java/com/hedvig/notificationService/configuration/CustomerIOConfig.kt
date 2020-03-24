package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.customerio.Customerio
import com.hedvig.customerio.CustomerioClient
import com.hedvig.customerio.CustomerioMock
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CustomerIOConfig {

    @Bean("customerIO")
    @ConditionalOnProperty("hedvig.usefakes", havingValue = "true", matchIfMissing = false)
    fun customerIOMock(objectMapper: ObjectMapper): CustomerioClient {
        return CustomerioMock(objectMapper)
    }

    @Bean("customerIO")
    @ConditionalOnMissingBean
    fun customerIO(
        objectMapper: ObjectMapper,
        @Value("\${customerio.siteid}") siteId: String,
        @Value("\${customerio.secretApiKey}") secretApiKey: String
    ): CustomerioClient {
        return Customerio(siteId, secretApiKey, objectMapper, okhttp3.OkHttpClient())
    }
}
