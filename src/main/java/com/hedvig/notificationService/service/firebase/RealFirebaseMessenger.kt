package com.hedvig.notificationService.service.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message

class RealFirebaseMessenger(
    private val firebaseMessaging: FirebaseMessaging) : FirebaseMessager {
    override fun send(message: Message) : String {
        return firebaseMessaging.send(message)
    }
}