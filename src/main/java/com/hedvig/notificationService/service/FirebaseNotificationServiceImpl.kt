package com.hedvig.notificationService.service

import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.*
import com.hedvig.notificationService.entities.FirebaseRepository
import com.hedvig.notificationService.entities.FirebaseToken
import com.hedvig.notificationService.service.TextKeys.CLAIM_PAID_BODY
import com.hedvig.notificationService.service.TextKeys.CLAIM_PAID_TITLE
import com.hedvig.notificationService.service.TextKeys.CONNECT_DD_BODY
import com.hedvig.notificationService.service.TextKeys.CONNECT_DD_TITLE
import com.hedvig.notificationService.service.TextKeys.DEFAULT_TITLE
import com.hedvig.notificationService.service.TextKeys.INSURANCE_POLICY_UPDATED_BODY
import com.hedvig.notificationService.service.TextKeys.INSURANCE_POLICY_UPDATED_TITLE
import com.hedvig.notificationService.service.TextKeys.INSURANCE_RENEWED_BODY
import com.hedvig.notificationService.service.TextKeys.INSURANCE_RENEWED_TITLE
import com.hedvig.notificationService.service.TextKeys.NEW_MESSAGE_BODY
import com.hedvig.notificationService.service.TextKeys.PAYMENT_FAILED_BODY
import com.hedvig.notificationService.service.TextKeys.PAYMENT_FAILED_TITLE
import com.hedvig.notificationService.service.TextKeys.REFERRAL_SUCCESS_BODY
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient
import com.hedvig.service.LocalizationService
import com.hedvig.service.TextKeysLocaleResolver
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*
import javax.money.MonetaryAmount
import javax.transaction.Transactional

@Service
open class FirebaseNotificationServiceImpl(
    private val firebaseRepository: FirebaseRepository,
    private val firebaseMessaging: FirebaseMessaging,
    private val localizationService: LocalizationService,
    private val memberService: MemberServiceClient,
    private val textKeysLocaleResolver: TextKeysLocaleResolver
) : FirebaseNotificationService {

    override fun sendNewMessageNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message.builder()
            .putData(TYPE, NEW_MESSAGE)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = DEFAULT_TITLE,
                    bodyTextKey = NEW_MESSAGE_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = DEFAULT_TITLE,
                    bodyTextKey = NEW_MESSAGE_BODY,
                    type = NEW_MESSAGE
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(NEW_MESSAGE, memberId, message)
    }

    override fun sendReferredSuccessNotification(
        memberId: String,
        referredName: String,
        incentiveAmount: String,
        incentiveCurrency: String
    ) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, REFERRAL_SUCCESS)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = DEFAULT_TITLE,
                    bodyTextKey = REFERRAL_SUCCESS_BODY
                ).putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                    .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                    .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                    .build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = DEFAULT_TITLE,
                    bodyTextKey = REFERRAL_SUCCESS_BODY,
                    type = REFERRAL_SUCCESS
                ).putData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                    .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                    .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                    .build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(REFERRAL_SUCCESS, memberId, message)
    }

    override fun sendConnectDirectDebitNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, CONNECT_DIRECT_DEBIT)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = CONNECT_DD_TITLE,
                    bodyTextKey = CONNECT_DD_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = CONNECT_DD_TITLE,
                    bodyTextKey = CONNECT_DD_BODY,
                    type = CONNECT_DIRECT_DEBIT
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(CONNECT_DIRECT_DEBIT, memberId, message)
    }

    override fun sendPaymentFailedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, PAYMENT_FAILED)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = PAYMENT_FAILED_TITLE,
                    bodyTextKey = PAYMENT_FAILED_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = PAYMENT_FAILED_TITLE,
                    bodyTextKey = PAYMENT_FAILED_BODY,
                    type = PAYMENT_FAILED
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(PAYMENT_FAILED, memberId, message)
    }

    override fun sendClaimPaidNotification(memberId: String, amount: MonetaryAmount) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, CLAIM_PAID)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = CLAIM_PAID_TITLE,
                    bodyTextKey = CLAIM_PAID_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = CLAIM_PAID_TITLE,
                    bodyTextKey = CLAIM_PAID_BODY,
                    type = CLAIM_PAID
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(CLAIM_PAID, memberId, message)
    }

    override fun sendInsurancePolicyUpdatedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, INSURANCE_POLICY_UPDATED)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = INSURANCE_POLICY_UPDATED_TITLE,
                    bodyTextKey = INSURANCE_POLICY_UPDATED_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = INSURANCE_POLICY_UPDATED_TITLE,
                    bodyTextKey = INSURANCE_POLICY_UPDATED_BODY,
                    type = INSURANCE_POLICY_UPDATED
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(INSURANCE_POLICY_UPDATED, memberId, message)
    }

    override fun sendInsuranceRenewedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, INSURANCE_RENEWED)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = INSURANCE_RENEWED_TITLE,
                    bodyTextKey = INSURANCE_RENEWED_BODY
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = INSURANCE_RENEWED_TITLE,
                    bodyTextKey = INSURANCE_RENEWED_BODY,
                    type = INSURANCE_RENEWED
                ).build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        sendNotification(INSURANCE_RENEWED, memberId, message)
    }

    override fun sendGenericCommunicationNotification(memberId: String, titleTextKey: String, bodyTextKey: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, GENERIC_COMMUNICATION)
            .setApnsConfig(
                createApnsConfig(
                    memberId = memberId,
                    titleTextKey = titleTextKey,
                    bodyTextKey = bodyTextKey
                ).build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(
                    memberId = memberId,
                    titleTextKey = titleTextKey,
                    bodyTextKey = bodyTextKey,
                    type = GENERIC_COMMUNICATION
                ).build()
            ).setToken(firebaseToken.get().token)
            .build()

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
                val docData = HashMap<String, Any>()
                docData["memberId"] = memberId
                docData["token"] = token
                collection.add(docData)
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
        bodyTextKey: String
    ): ApnsConfig.Builder {
        val (title, body) = resolveTitleAndBody(memberId, titleTextKey, bodyTextKey)

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
    }

    private fun createAndroidConfigBuilder(
        memberId: String,
        titleTextKey: String,
        bodyTextKey: String,
        type: String
    ): AndroidConfig.Builder {
        val (title, body) = resolveTitleAndBody(memberId, titleTextKey, bodyTextKey)

        return AndroidConfig
            .builder()
            .putData(DATA_MESSAGE_TITLE, title)
            .putData(DATA_MESSAGE_BODY, body)
            .putData(TYPE, type)
    }

    private fun resolveTitleAndBody(memberId: String, titleTextKey: String, bodyTextKey: String): Pair<String, String> {
        val acceptLanguage = memberService.profile(memberId).body?.acceptLanguage
        val locale = textKeysLocaleResolver.resolveLocale(acceptLanguage)

        val title =
            localizationService.getText(locale, titleTextKey) ?: throw Error("Could not find text key $titleTextKey")
        val body =
            localizationService.getText(locale, bodyTextKey) ?: throw Error("Could not find text key $bodyTextKey")

        return Pair(title, body)
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
        const val GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"

        const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
        const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"

        const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY"

        private val logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl::class.java)
    }
}
