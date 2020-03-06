package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.service.firebase.FakeFirebaseMessenger
import com.hedvig.notificationService.service.firebase.FirebaseMessager
import com.hedvig.notificationService.serviceIntegration.memberService.FakeMemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("development")
@Configuration
open class DevelopmentConfig() {

    @Bean
    open fun firebaseMessaging(): FirebaseMessager? {
        val objectMapper = ObjectMapper()
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
        return FakeFirebaseMessenger(objectMapper)
    }

    @ConditionalOnProperty("hedvig.usefakes", havingValue = "true", matchIfMissing = false)
    @Primary
    @Bean
    open fun MemberServiceClient(): MemberServiceClient {
        return FakeMemberServiceClient()
    }
}
