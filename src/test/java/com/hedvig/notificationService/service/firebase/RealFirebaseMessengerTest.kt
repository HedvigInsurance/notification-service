package com.hedvig.notificationService.service.firebase

import com.google.firebase.FirebaseException
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

internal class RealFirebaseMessengerTest {

    @get:Rule
    public var thrown = ExpectedException.none()

    @MockK(relaxed = true)
    lateinit var firebaseMessaging: FirebaseMessaging

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `send forwards message to implementation`() {

        val classUnderTest = RealFirebaseMessenger(firebaseMessaging)

        val message = Message.builder().setToken("TEST").build()
        classUnderTest.send(message)

        verify {
            firebaseMessaging.send(message)
        }
    }

    @Test
    fun `send returns response from implementation`() {

        every { firebaseMessaging.send(any()) } returns "{}"

        val classUnderTest = RealFirebaseMessenger(firebaseMessaging)

        val message = Message.builder().setToken("TEST").build()
        val result = classUnderTest.send(message)

        assertThat(result).isEqualTo("{}")
    }

    @Test
    fun `send does not catch throws exceptions`() {

        every { firebaseMessaging.send(any()) } throws FirebaseException("Test")

        val classUnderTest = RealFirebaseMessenger(firebaseMessaging)

        val message = Message.builder().setToken("TEST").build()

        thrown.expect(FirebaseException::class.java)
        classUnderTest.send(message)
    }
}
