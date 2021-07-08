package com.hedvig.notificationService.service.firebase

import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.ApsAlert
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.hedvig.libs.translations.LocaleResolver
import com.hedvig.libs.translations.Translations
import com.hedvig.notificationService.entities.FirebaseRepository
import com.hedvig.notificationService.entities.FirebaseToken
import com.hedvig.notificationService.service.firebase.TextKeys.CLAIM_PAID_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.CLAIM_PAID_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.CONNECT_DD_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.CONNECT_DD_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.DEFAULT_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.INSURANCE_POLICY_UPDATED_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.INSURANCE_POLICY_UPDATED_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.INSURANCE_RENEWED_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.INSURANCE_RENEWED_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.NEW_MESSAGE_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.PAYMENT_FAILED_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.PAYMENT_FAILED_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.REFERRALS_ENABLED_BODY
import com.hedvig.notificationService.service.firebase.TextKeys.REFERRALS_ENABLED_TITLE
import com.hedvig.notificationService.service.firebase.TextKeys.REFERRAL_SUCCESS_BODY
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.Locale
import java.util.Optional
import javax.transaction.Transactional

@Service
class FirebaseNotificationServiceImpl(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseMessaging: FirebaseMessager,
    private val translations: Translations,
    private val memberService: MemberServiceClient
) : FirebaseNotificationService {

    override fun sendNewMessageNotification(memberId: String, messageText: String?) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = NEW_MESSAGE,
            titleTextKey = DEFAULT_TITLE,
            bodyTextKey = NEW_MESSAGE_BODY,
            customData = messageText?.let { mapOf(DATA_NEW_MESSAGE_BODY to it) }
        )

        sendNotification(NEW_MESSAGE, memberId, message)
    }

    override fun sendReferredSuccessNotification(
        memberId: String,
        referredName: String,
        incentiveAmount: String,
        incentiveCurrency: String
    ) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = REFERRAL_SUCCESS,
            titleTextKey = DEFAULT_TITLE,
            bodyTextKey = REFERRAL_SUCCESS_BODY,
            customData = mapOf(
                DATA_MESSAGE_REFERRED_SUCCESS_NAME to referredName,
                DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT to incentiveAmount,
                DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY to incentiveCurrency
            ),
            bodyReplacements = mapOf("REFERRAL_VALUE" to incentiveAmount.toFloat().toInt().toString())
        )

        sendNotification(REFERRAL_SUCCESS, memberId, message)
    }

    override fun sendConnectDirectDebitNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(memberId, firebaseToken,
            CONNECT_DIRECT_DEBIT, CONNECT_DD_TITLE, CONNECT_DD_BODY)

        sendNotification(CONNECT_DIRECT_DEBIT, memberId, message)
    }

    override fun sendPaymentFailedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(memberId, firebaseToken,
            PAYMENT_FAILED, PAYMENT_FAILED_TITLE, PAYMENT_FAILED_BODY)

        sendNotification(PAYMENT_FAILED, memberId, message)
    }

    override fun sendTerminatedFailedChargesNotification(memberId: String) {
        this.sendGenericCommunicationNotification(
            memberId = memberId,
            titleTextKey = TERMINATION_FAILED_CHARGES_TITLE,
            bodyTextKey = TERMINATION_FAILED_CHARGES_BODY
        )
    }

    override fun sendClaimPaidNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = CLAIM_PAID,
            titleTextKey = CLAIM_PAID_TITLE,
            bodyTextKey = CLAIM_PAID_BODY
        )

        sendNotification(CLAIM_PAID, memberId, message)
    }

    override fun sendInsurancePolicyUpdatedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = INSURANCE_POLICY_UPDATED,
            titleTextKey = INSURANCE_POLICY_UPDATED_TITLE,
            bodyTextKey = INSURANCE_POLICY_UPDATED_BODY
        )

        sendNotification(INSURANCE_POLICY_UPDATED, memberId, message)
    }

    override fun sendInsuranceRenewedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = INSURANCE_RENEWED,
            titleTextKey = INSURANCE_RENEWED_TITLE,
            bodyTextKey = INSURANCE_RENEWED_BODY
        )

        sendNotification(INSURANCE_RENEWED, memberId, message)
    }

    override fun sendHedvigReferralsEnabledNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = REFERRALS_ENABLED,
            titleTextKey = REFERRALS_ENABLED_TITLE,
            bodyTextKey = REFERRALS_ENABLED_BODY
        )

        sendNotification(REFERRALS_ENABLED, memberId, message)
    }

    override fun sendGenericCommunicationNotification(memberId: String, titleTextKey: String, bodyTextKey: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = createMessage(
            memberId = memberId,
            firebaseToken = firebaseToken,
            dataType = GENERIC_COMMUNICATION,
            titleTextKey = titleTextKey,
            bodyTextKey = bodyTextKey
        )

        sendNotification(GENERIC_COMMUNICATION, memberId, message)
    }

    private fun sendNotification(type: String, memberId: String, message: Message) {
        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendNotification: Cannot send notification of type {} with memberId {} through Firebase. Error: {}",
                type,
                memberId,
                e
            )
        }
    }

    private fun createMessage(
        memberId: String,
        firebaseToken: Optional<FirebaseToken>,
        dataType: String,
        titleTextKey: String,
        bodyTextKey: String,
        customData: Map<String, String>? = null,
        bodyReplacements: Map<String, String> = emptyMap()
    ): Message {
        val apsConfig = createApnsConfig(
            memberId = memberId,
            titleTextKey = titleTextKey,
            bodyTextKey = bodyTextKey,
            bodyReplacements = bodyReplacements,
            type = dataType
        )

        customData?.let {
            apsConfig.putAllCustomData(it)
        }

        val androidConfig = createAndroidConfigBuilder(
            memberId = memberId,
            titleTextKey = titleTextKey,
            bodyTextKey = bodyTextKey,
            bodyReplacements = bodyReplacements,
            type = dataType
        )

        customData?.let {
            androidConfig.putAllData(it)
        }

        return Message.builder()
            .putData(TYPE, dataType)
            .setApnsConfig(apsConfig.build())
            .setAndroidConfig(androidConfig.build())
            .setToken(firebaseToken.get().token)
            .build()
    }

    @Transactional
    override fun getFirebaseToken(memberId: String): Optional<FirebaseToken> {
        return firebaseRepository.findById(memberId)
    }

    @Transactional
    override fun setFirebaseToken(memberId: String, token: String) {
        val db = FirestoreClient.getFirestore()
        val collection = db.collection("push-notification-tokens")

        val future = collection
            .whereEqualTo("token", token)
            .whereEqualTo("memberId", memberId).get()

        try {
            val snapshot = future.get()

            if (snapshot.documents.isEmpty()) {
                collection.add(
                    mapOf(
                        "memberId" to memberId,
                        "token" to token
                    )
                )
            }
        } catch (e: Exception) {
            logger.error(
                "Something went wrong while fetching documents from firebase",
                e
            )
        }

        val firebaseToken = FirebaseToken()
        firebaseToken.setMemberId(memberId)
        firebaseToken.setToken(token)

        firebaseRepository.save(firebaseToken)
    }

    private fun createApnsConfig(
        memberId: String,
        titleTextKey: String,
        bodyTextKey: String,
        bodyReplacements: Map<String, String>,
        type: String
    ): ApnsConfig.Builder {
        val (title, body) = resolveTitleAndBody(memberId, titleTextKey, bodyTextKey, bodyReplacements)

        return ApnsConfig
            .builder()
            .setAps(
                Aps
                    .builder()
                    .setAlert(
                        ApsAlert
                            .builder()
                            .setTitle(title)
                            .setBody(body)
                            .build()
                    ).build()
            )
            .putCustomData(TYPE, type)
    }

    private fun createAndroidConfigBuilder(
        memberId: String,
        titleTextKey: String,
        bodyTextKey: String,
        bodyReplacements: Map<String, String>,
        type: String
    ): AndroidConfig.Builder {
        val (title, body) = resolveTitleAndBody(memberId, titleTextKey, bodyTextKey, bodyReplacements)

        return AndroidConfig
            .builder()
            .putData(DATA_MESSAGE_TITLE, title)
            .putData(DATA_MESSAGE_BODY, body)
            .putData(TYPE, type)
    }

    private fun resolveTitleAndBody(
        memberId: String,
        titleTextKey: String,
        bodyTextKey: String,
        bodyReplacements: Map<String, String>
    ): Pair<String, String> {
        val acceptLanguage = memberService.profile(memberId).body?.acceptLanguage
        val locale = LocaleResolver.resolve(acceptLanguage)

        val title =
            translateWithReplacements(titleTextKey, locale)
                ?: throw Error("Could not find text key $titleTextKey")
        val body =
            translateWithReplacements(bodyTextKey, locale, bodyReplacements)
                ?: throw Error("Could not find text key $bodyTextKey")

        return Pair(title, body)
    }

    fun translateWithReplacements(
        key: String,
        locale: Locale,
        replacements: Map<String, String> = emptyMap()
    ): String? {

        var translation = translations.get(key, locale) ?: return null
        replacements.forEach { (token, text) -> translation = translation.replace("{$token}", text) }
        return translation
    }

    companion object {
        const val TYPE = "TYPE"
        const val NEW_MESSAGE = "NEW_MESSAGE"
        const val REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
        const val CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
        const val PAYMENT_FAILED = "PAYMENT_FAILED"
        const val CLAIM_PAID = "CLAIM_PAID"
        const val INSURANCE_POLICY_UPDATED = "INSURANCE_POLICY_UPDATED"
        const val INSURANCE_RENEWED = "INSURANCE_RENEWED"
        const val REFERRALS_ENABLED = "REFERRALS_ENABLED"
        const val GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"

        const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
        const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"

        const val TERMINATION_FAILED_CHARGES_TITLE = "TERMINATION_FAILED_CHARGES_TITLE"
        const val TERMINATION_FAILED_CHARGES_BODY = "TERMINATION_FAILED_CHARGES_BODY"

        const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY"

        const val DATA_NEW_MESSAGE_BODY = "DATA_NEW_MESSAGE_BODY"

        private val logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl::class.java)
    }
}
