package com.hedvig.notificationService.service

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Message
import com.hedvig.common.localization.LocalizationService
import com.hedvig.notificationService.entities.FirebaseRepository
import com.hedvig.notificationService.entities.FirebaseToken
import com.hedvig.notificationService.service.firebase.RealFirebaseMessenger
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional


@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
internal class FirebaseNotificationServiceImplTest {

    val messages = mutableListOf<Message>()

    @MockK
    lateinit var firebaseRepository: FirebaseRepository
    @MockK
    lateinit var firebaseMessaging: RealFirebaseMessenger
    @MockK
    lateinit var localizationService: LocalizationService
    @MockK
    lateinit var memberService: MemberServiceClient

    lateinit var classUnderTest: FirebaseNotificationServiceImpl

    @BeforeEach
    fun setup() {
        classUnderTest =
            FirebaseNotificationServiceImpl(firebaseRepository, firebaseMessaging, localizationService, memberService)

        val token = FirebaseToken().apply {
            memberId = MEMBER_ID
            token = "token"
        }

        every { firebaseRepository.findById(any()) } returns Optional.of(token)

        val member = Member(
            memberId = MEMBER_ID.toLong(),
            status = null,
            ssn = null,
            gender = null,
            firstName = null,
            lastName = null,
            street = null,
            floor = null,
            apartment = null,
            city = null,
            zipCode = null,
            country = null,
            email = null,
            phoneNumber = null,
            birthDate = null,
            signedOn = null,
            createdOn = null,
            fraudulentStatus = null,
            fraudulentDescription = null,
            acceptLanguage = null,
            traceMemberInfo = listOf()
        )

        every { memberService.profile(any()) } returns ResponseEntity.ok(member)


        messages.clear()
        every { firebaseMessaging.send(capture(messages)) } answers { "" }
    }

    @Test
    fun sendNewMessageNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("DEFAULT_TITLE", any()) } returns title
        every { localizationService.getTranslation("NEW_MESSAGE_BODY", any()) } returns body
        classUnderTest.sendNewMessageNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "NEW_MESSAGE"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "NEW_MESSAGE", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendReferredSuccessNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("DEFAULT_TITLE", any()) } returns title
        every { localizationService.getTranslation("REFERRAL_SUCCESS_BODY", any()) } returns body
        val referredName = "referredName"
        val incentiveAmount = "10.00"
        val incentiveCurrency = "SEK"
        classUnderTest.sendReferredSuccessNotification(
            MEMBER_ID,
            referredName,
            incentiveAmount,
            incentiveCurrency
        )

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "REFERRAL_SUCCESS"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf(
                "TYPE" to "REFERRAL_SUCCESS",
                "DATA_MESSAGE_TITLE" to title,
                "DATA_MESSAGE_BODY" to body,
                "DATA_MESSAGE_REFERRED_SUCCESS_NAME" to referredName,
                "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT" to incentiveAmount,
                "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY" to incentiveCurrency
            )
        )
        deepMatchMessageIOSData(
            messages[0], title, body, mapOf(
                "DATA_MESSAGE_REFERRED_SUCCESS_NAME" to referredName,
                "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT" to incentiveAmount,
                "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY" to incentiveCurrency
            )
        )
    }

    @Test
    fun sendConnectDirectDebitNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("NOTIFICATION_CONNECT_DD_TITLE", any()) } returns title
        every { localizationService.getTranslation("NOTIFICATION_CONNECT_DD_BODY", any()) } returns body
        classUnderTest.sendConnectDirectDebitNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "CONNECT_DIRECT_DEBIT"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "CONNECT_DIRECT_DEBIT", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendPaymentFailedNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("NOTIFICATION_PAYMENT_FAILED_TITLE", any()) } returns title
        every { localizationService.getTranslation("NOTIFICATION_PAYMENT_FAILED_BODY", any()) } returns body
        classUnderTest.sendPaymentFailedNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "PAYMENT_FAILED"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "PAYMENT_FAILED", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendTerminatedFailedChargesNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("TERMINATION_FAILED_CHARGES_TITLE", any()) } returns title
        every { localizationService.getTranslation("TERMINATION_FAILED_CHARGES_BODY", any()) } returns body
        classUnderTest.sendTerminatedFailedChargesNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "GENERIC_COMMUNICATION"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "GENERIC_COMMUNICATION", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendClaimPaidNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("CLAIM_PAID_TITLE", any()) } returns title
        every { localizationService.getTranslation("CLAIM_PAID_BODY", any()) } returns body
        classUnderTest.sendClaimPaidNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "CLAIM_PAID"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "CLAIM_PAID", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendInsurancePolicyUpdatedNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("INSURANCE_POLICY_UPDATED_TITLE", any()) } returns title
        every { localizationService.getTranslation("INSURANCE_POLICY_UPDATED_BODY", any()) } returns body
        classUnderTest.sendInsurancePolicyUpdatedNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "INSURANCE_POLICY_UPDATED"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "INSURANCE_POLICY_UPDATED", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendInsuranceRenewedNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("INSURANCE_RENEWED_TITLE", any()) } returns title
        every { localizationService.getTranslation("INSURANCE_RENEWED_BODY", any()) } returns body
        classUnderTest.sendInsuranceRenewedNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "INSURANCE_RENEWED"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "INSURANCE_RENEWED", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendHedvigReferralsEnabledNotification() {
        val title = "title"
        val body = "body"
        every { localizationService.getTranslation("REFERRALS_ENABLED_TITLE", any()) } returns title
        every { localizationService.getTranslation("REFERRALS_ENABLED_BODY", any()) } returns body
        classUnderTest.sendHedvigReferralsEnabledNotification(MEMBER_ID)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "REFERRALS_ENABLED"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "REFERRALS_ENABLED", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    @Test
    fun sendGenericCommunicationNotification() {
        val titleKey = "titleKey"
        val title = "title"
        every { localizationService.getTranslation(titleKey, any()) } returns title
        val bodyKey = "bodyKey"
        val body = "body"
        every { localizationService.getTranslation(bodyKey, any()) } returns body
        classUnderTest.sendGenericCommunicationNotification(MEMBER_ID, titleKey, bodyKey)

        deepMatchMessageCommonData(messages[0], mapOf("TYPE" to "GENERIC_COMMUNICATION"))
        deepMatchMessageAndroidData(
            messages[0],
            mapOf("TYPE" to "GENERIC_COMMUNICATION", "DATA_MESSAGE_TITLE" to title, "DATA_MESSAGE_BODY" to body)
        )
        deepMatchMessageIOSData(messages[0], title, body)
    }

    private fun deepMatchMessageCommonData(message: Message, map: Map<String, String>) {
        val data = ReflectionTestUtils.getField(message, "data") as Map<String, String>

        assertThat(data).isEqualTo(map)
    }

    private fun deepMatchMessageAndroidData(message: Message, map: Map<String, String>) {
        val androidConfigData = ReflectionTestUtils.getField(
            ReflectionTestUtils.getField(message, "androidConfig") as AndroidConfig,
            "data"
        ) as Map<String, String>

        assertThat(androidConfigData).isEqualTo(map)
    }

    private fun deepMatchMessageIOSData(
        message: Message,
        title: String,
        body: String,
        customData: Map<String, String>? = null
    ) {
        val apnsConfigPayload = ReflectionTestUtils.getField(
            ReflectionTestUtils.getField(message, "apnsConfig") as ApnsConfig,
            "payload"
        ) as Map<String, Any>
        val iOSTitle =
            ReflectionTestUtils.getField((apnsConfigPayload["aps"] as Map<String, Any>)["alert"], "title") as String
        val iOSBody =
            ReflectionTestUtils.getField((apnsConfigPayload["aps"] as Map<String, Any>)["alert"], "body") as String

        assertThat(title).isEqualTo(iOSTitle)
        assertThat(body).isEqualTo(iOSBody)
        customData?.let {
            it.forEach { entry ->
                assertThat(apnsConfigPayload).contains(Pair(entry.key, entry.value))
            }
        }
    }

    companion object {
        private val MEMBER_ID = "1234"
    }
}