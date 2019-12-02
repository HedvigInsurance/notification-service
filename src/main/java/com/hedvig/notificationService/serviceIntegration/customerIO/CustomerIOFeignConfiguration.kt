package com.hedvig.notificationService.serviceIntegration.customerIO

import feign.auth.BasicAuthRequestInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean

class CustomerIOFeignConfiguration (
    @Value("\${customerIO.siteId}") val username: String,
    @Value("\${customerIO.apiKey}") val password: String
){
    @Bean
    fun basicAuthRequestInterceptor() = BasicAuthRequestInterceptor(username, password)
}
