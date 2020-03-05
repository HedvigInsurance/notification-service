package com.hedvig.notificationService.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.hedvig.notificationService.service.firebase.FakeFirebaseMessenger
import com.hedvig.notificationService.service.firebase.FirebaseMessager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("development")
@Configuration
open class FirebaseDevelopmentConfig() {

    @Bean
    open fun firebaseMessaging(objectMapper: ObjectMapper): FirebaseMessager? {
        return FakeFirebaseMessenger(objectMapper)
    }
}