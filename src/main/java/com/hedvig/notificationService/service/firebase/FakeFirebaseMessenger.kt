package com.hedvig.notificationService.service.firebase

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.firebase.messaging.Message

class FakeFirebaseMessenger(private val objectMapper:ObjectMapper) : FirebaseMessager {
    override fun send(message: Message): String {
        return "Sent from firebaseMessaging: ${objectMapper.writeValueAsString(message)}"
    }
}
