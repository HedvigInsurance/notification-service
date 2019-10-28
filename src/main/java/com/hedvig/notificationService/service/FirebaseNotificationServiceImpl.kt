package com.hedvig.notificationService.service

import com.google.firebase.cloud.FirestoreClient
import com.google.firebase.messaging.*
import com.hedvig.notificationService.entities.FirebaseRepository
import com.hedvig.notificationService.entities.FirebaseToken
import com.hedvig.notificationService.serviceIntegration.memberService.MemberService
import com.hedvig.service.LocalizationService
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
    private val memberService: MemberService
) : FirebaseNotificationService {
    override fun sendNewMessageNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message.builder()
            .putData(TYPE, NEW_MESSAGE)
            .setApnsConfig(createApnsConfig(NEW_MESSAGE_BODY).build())
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, NEW_MESSAGE).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)

            logger.info("Response from pushing notification {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendReferredSuccessNotification(
        memberId: String,
        referredName: String,
        incentiveAmount: String,
        incentiveCurrency: String
    ) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val body = String.format(REFERRAL_SUCCESS_BODY, referredName)

        val message = Message
            .builder()
            .putData(TYPE, REFERRAL_SUCCESS)
            .setApnsConfig(
                createApnsConfig(body)
                    .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                    .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                    .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                    .build()
            )
            .setAndroidConfig(
                createAndroidConfigBuilder(body, REFERRAL_SUCCESS)
                    .putData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                    .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                    .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                    .build()
            )
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)

            logger.info("Response from pushing notification {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
                memberId,
                e
            )
        }

    }

    override fun sendConnectDirectDebitNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)
        val locale = memberService.getPreferredLanguage(memberId)
        val title = localizationService.getText(locale, CONNECT_DD_TITLE) ?: throw Error("Could not find text key $CONNECT_DD_TITLE")
        val body = localizationService.getText(locale, CONNECT_DD_BODY) ?: throw Error("Could not find text key $CONNECT_DD_BODY")

        val message = Message
            .builder()
            .putData(TYPE, CONNECT_DIRECT_DEBIT)
            .setApnsConfig(createApnsConfig(
                    title = title,
                    body = body
            ).build())
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, CONNECT_DIRECT_DEBIT).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendConnectDirectDebitNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendPaymentFailedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)
        val locale = memberService.getPreferredLanguage(memberId)

        val title = localizationService.getText(locale, PAYMENT_FAILED_TITLE) ?: throw Error("Could not find text key $PAYMENT_FAILED_TITLE")
        val body = localizationService.getText(locale, PAYMENT_FAILED_BODY) ?: throw Error("Could not find text key $PAYMENT_FAILED_BODY")

        val message = Message
            .builder()
            .putData(TYPE, PAYMENT_FAILED)
            .setApnsConfig(createApnsConfig(
                    title = title,
                    body = body
            ).build()) // TODO: Copy from IEX
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, PAYMENT_FAILED).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendPaymentFailedNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendClaimPaidNotification(memberId: String, amount: MonetaryAmount) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, CLAIM_PAID)
            .setApnsConfig(createApnsConfig("Skadeanmälan utbetalad!").build()) // TODO: Copy from IEX
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, CLAIM_PAID).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendClaimPaidNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendInsurancePolicyUpdatedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, INSURANCE_POLICY_UPDATED)
            .setApnsConfig(createApnsConfig("Försäkringsvillkor uppdaterade").build()) // TODO: Copy from IEX
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, INSURANCE_POLICY_UPDATED).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendInsurancePolicyUpdatedNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendInsuranceRenewedNotification(memberId: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, INSURANCE_RENEWED)
            .setApnsConfig(createApnsConfig("Försäkring förnyad").build()) // TODO: Copy from IEX
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, NEW_MESSAGE).build())
            .setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendInsuranceRenewedNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendGenericCommunicationNotification(memberId: String, title: String, body: String) {
        val firebaseToken = firebaseRepository.findById(memberId)

        val message = Message
            .builder()
            .putData(TYPE, GENERIC_COMMUNICATION)
            .setApnsConfig(createApnsConfig(body).build())
            .setAndroidConfig(
                createAndroidConfigBuilder(body, GENERIC_COMMUNICATION)
                    .putData(TITLE, title)
                    .build()
            ).setToken(firebaseToken.get().token)
            .build()

        try {
            val response = firebaseMessaging.send(message)
            logger.info("Response from pushing notification: {}", response)
        } catch (e: FirebaseMessagingException) {
            logger.error(
                "SendGenericCommunicationNotification: Cannot send notification with memberId {} through Firebase. Error: {}",
                memberId,
                e
            )
        }
    }

    override fun sendNotification(memberId: String, body: String): Boolean {
        val firebaseToken = firebaseRepository.findById(memberId)

        if (firebaseToken.isPresent) {
            val message = Message.builder()
                .putData(TYPE, EMAIL)
                .setApnsConfig(createApnsConfig(body).build())
                .setAndroidConfig(createAndroidConfigBuilder(body, EMAIL).build())
                .setToken(firebaseToken.get().token)
                .build()
            try {
                val response = firebaseMessaging.send(message)

                logger.info("Response from pushing notification {}", response)
            } catch (e: FirebaseMessagingException) {
                logger.error(
                    "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
                    memberId,
                    e
                )
                return false
            }

            return true
        } else {
            return false
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


    private fun createApnsConfig(body: String, title: String = TITLE): ApnsConfig.Builder {
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

    private fun createAndroidConfigBuilder(body: String, type: String): AndroidConfig.Builder {
        return AndroidConfig
            .builder()
            .putData(DATA_MESSAGE_TITLE, TITLE)
            .putData(DATA_MESSAGE_BODY, body)
            .putData(TYPE, type)
    }

    companion object {

        const val TITLE = "Hedvig"
        const val NEW_MESSAGE_BODY = "Hej, du har ett nytt meddelande från Hedvig!"
        const val REFERRAL_SUCCESS_BODY = "%s skaffade Hedvig tack vare dig!"

        private const val CONNECT_DD_TITLE = "NOTIFICATION_CONNECT_DD_BODY"
        private const val CONNECT_DD_BODY = "NOTIFICATION_CONNECT_DD_BODY"

        private const val PAYMENT_FAILED_TITLE = "NOTIFICATION_PAYMENT_FAILED_TITLE"
        private const val PAYMENT_FAILED_BODY = "NOTIFICATION_PAYMENT_FAILED_BODY"

        const val TYPE = "TYPE"
        const val NEW_MESSAGE = "NEW_MESSAGE"
        const val REFERRAL_SUCCESS = "REFERRAL_SUCCESS"
        const val CONNECT_DIRECT_DEBIT = "CONNECT_DIRECT_DEBIT"
        const val PAYMENT_FAILED = "PAYMENT_FAILED"
        const val CLAIM_PAID = "CLAIM_PAID"
        const val INSURANCE_POLICY_UPDATED = "INSURANCE_POLICY_UPDATED"
        const val INSURANCE_RENEWED = "INSURANCE_RENEWED"
        const val GENERIC_COMMUNICATION = "GENERIC_COMMUNICATION"
        const val EMAIL = "EMAIL"

        const val DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE"
        const val DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY"

        const val DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT"
        const val DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY"

        private val logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl::class.java)
    }
}
