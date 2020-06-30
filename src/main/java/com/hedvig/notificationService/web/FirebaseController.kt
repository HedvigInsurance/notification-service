package com.hedvig.notificationService.web

import com.hedvig.notificationService.dto.ReferralsSuccessSendNotificationRequest
import com.hedvig.notificationService.service.FirebaseNotificationService
import com.hedvig.notificationService.web.dto.ClaimPaidNotificationRequest
import com.hedvig.notificationService.web.dto.GenericCommunicationNotificationRequest
import com.hedvig.notificationService.web.dto.SendPushNotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/_/notifications")
class FirebaseController(private val firebaseNotificationService: FirebaseNotificationService) {

    private val log = LoggerFactory.getLogger(FirebaseController::class.java)

    @PostMapping("/{memberId}/token")
    fun saveFirebaseToken(
        @PathVariable memberId: String,
        @RequestBody token: String
    ): ResponseEntity<Void> {
        try {
            firebaseNotificationService.setFirebaseToken(memberId, token)
        } catch (e: Exception) {
            log.error(
                "Something went wrong while the Token {} for member {} was about to be stored in the database with error {}",
                token,
                memberId,
                e
            )
            throw e
        }

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{memberId}/token")
    fun getFirebaseToken(@PathVariable memberId: String): ResponseEntity<*> {
        return try {
            val firebaseTokenOptional = firebaseNotificationService.getFirebaseToken(memberId)
            firebaseTokenOptional
                .map { firebaseToken -> ResponseEntity.ok(firebaseToken.token) }
                .orElseGet { ResponseEntity.notFound().build() }
        } catch (e: Exception) {
            log.error(
                "Something went wrong while trying to fetch the firebase token for member {} with error {}",
                memberId,
                e
            )
            throw e
        }
    }

    @PostMapping("/{memberId}/push/send")
    fun sendPushNotification(
            @PathVariable memberId: String,
            @RequestBody request: Optional<SendPushNotificationRequest>
    ): ResponseEntity<Void> {
        val message = request.orElse(null)?.message
        firebaseNotificationService.sendNewMessageNotification(memberId, message)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/referred/success/send")
    fun sendReferredSuccessPushNotification(
        @PathVariable memberId: String,
        @RequestBody @Valid body: ReferralsSuccessSendNotificationRequest
    ): ResponseEntity<Void> {
        firebaseNotificationService.sendReferredSuccessNotification(
            memberId,
            body.referredName,
            body.incentiveAmount,
            body.incentiveCurrency
        )
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/connect-direct-debit/send")
    fun sendConnectDirectDebitNotificationFailedNotification(
        @PathVariable memberId: String
    ): ResponseEntity<Void> {
        firebaseNotificationService.sendConnectDirectDebitNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/payment-failed/send")
    fun sendPaymentFailedNotification(@PathVariable memberId: String): ResponseEntity<Void> {
        firebaseNotificationService.sendPaymentFailedNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/terminated-failed-charges/send")
    fun sendTerminatedFailedChargesNotification(@PathVariable memberId: String): ResponseEntity<Void> {
        firebaseNotificationService.sendTerminatedFailedChargesNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/claim-paid/send")
    fun sendClaimPaidNotification(
        @PathVariable memberId: String,
        @RequestBody body: ClaimPaidNotificationRequest
    ): ResponseEntity<Void> {
        firebaseNotificationService.sendClaimPaidNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/insurance-policy-updated/send")
    fun sendInsurancePolicyUpdatedNotification(@PathVariable memberId: String): ResponseEntity<Void> {
        firebaseNotificationService.sendInsurancePolicyUpdatedNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/insurance-renewed/send")
    fun sendInsuranceRenewedNotification(@PathVariable memberId: String): ResponseEntity<Void> {
        firebaseNotificationService.sendInsuranceRenewedNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/referralsEnabled/send")
    fun sendHedvigReferralsEnabledNotification(@PathVariable memberId: String): ResponseEntity<Void> {
        firebaseNotificationService.sendHedvigReferralsEnabledNotification(memberId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{memberId}/push/generic-communication/send")
    fun sendGenericCommunicationNotification(
        @PathVariable memberId: String,
        @RequestBody body: GenericCommunicationNotificationRequest
    ): ResponseEntity<Void> {
        firebaseNotificationService
            .sendGenericCommunicationNotification(
                memberId,
                body.titleTextKey,
                body.bodyTextKey
            )
        return ResponseEntity.noContent().build()
    }
}
