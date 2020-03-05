package com.hedvig.notificationService.service.firebase

import com.google.firebase.messaging.Message

interface FirebaseMessager {
    fun send(message: Message) : String
}