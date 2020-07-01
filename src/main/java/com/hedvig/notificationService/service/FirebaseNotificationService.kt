package com.hedvig.notificationService.service

import com.hedvig.notificationService.entities.FirebaseToken
import java.util.Optional

interface FirebaseNotificationService {

    fun sendNewMessageNotification(memberId: String, messageText: String?)

    fun sendReferredSuccessNotification(
        memberId: String,
        referredName: String,
        incentiveAmount: String,
        incentiveCurrency: String
    )

    fun sendConnectDirectDebitNotification(memberId: String)

    fun sendPaymentFailedNotification(memberId: String)

    fun sendClaimPaidNotification(memberId: String)

    fun sendInsurancePolicyUpdatedNotification(memberId: String)

    fun sendInsuranceRenewedNotification(memberId: String)

    fun getFirebaseToken(memberId: String): Optional<FirebaseToken>

    fun setFirebaseToken(memberId: String, token: String)

    fun sendGenericCommunicationNotification(memberId: String, titleTextKey: String, bodyTextKey: String)

    fun sendTerminatedFailedChargesNotification(memberId: String)

    fun sendHedvigReferralsEnabledNotification(memberId: String)
}
