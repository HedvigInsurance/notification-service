package com.hedvig.notificationService.web

import com.hedvig.notificationService.dto.ReferralsSuccessSendNotificationRequest
import com.hedvig.notificationService.service.FirebaseNotificationService
import com.hedvig.notificationService.web.dto.ClaimPaidNotificationRequest
import com.hedvig.notificationService.web.dto.GenericCommunicationNotificationRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping("/_/notifications")
class FirebaseController(private val firebaseNotificationService: FirebaseNotificationService) {

    private val log = LoggerFactory.getLogger(FirebaseController::class.java)

    @PostMapping("/{memberId}/token")
    fun saveFirebaseToken(
        @PathVariable(name = "memberId") memberId: String, @RequestBody token: String
    ): ResponseEntity<*> {
        try {
            firebaseNotificationService.setFirebaseToken(memberId, token)
        } catch (e: Exception) {
            log.error(
                "Something went wrong while the Token {} for member {} was about to be stored in the database with error {}",
                token,
                memberId,
                e
            )
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Any>()
        }

        return ResponseEntity.noContent().build<Any>()
    }

    @GetMapping("/{memberId}/token")
    fun getFirebaseToken(@PathVariable(name = "memberId") memberId: String): ResponseEntity<*> {
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
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build<Any>()
        }
    }

    @PostMapping("/{memberId}/push/send")
    fun sendPushNotification(@PathVariable(name = "memberId") memberId: String): ResponseEntity<*> {
        firebaseNotificationService.sendNewMessageNotification(memberId)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/referred/success/send")
    fun sendReferredSuccessPushNotification(@PathVariable(name = "memberId") memberId: String, @RequestBody @Valid body: ReferralsSuccessSendNotificationRequest): ResponseEntity<*> {
        firebaseNotificationService.sendReferredSuccessNotification(
            memberId,
            body.referredName,
            body.incentiveAmount,
            body.incentiveCurrency
        )
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/connect-direct-debit/send")
    fun sendConnectDirectDebitNotificationFailedNotification(
        @PathVariable(name = "memberId") memberId: String
    ): ResponseEntity<*> {
        firebaseNotificationService.sendConnectDirectDebitNotification(memberId)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/payment-failed/send")
    fun sendPaymentFailedNotification(@PathVariable(name = "memberId") memberId: String): ResponseEntity<*> {
        firebaseNotificationService.sendPaymentFailedNotification(memberId)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/claim-paid/send")
    fun sendClaimPaidNotification(
        @PathVariable(name = "memberId") memberId: String,
        @RequestBody body: ClaimPaidNotificationRequest
    ): ResponseEntity<*> {
        firebaseNotificationService.sendClaimPaidNotification(memberId, body.amount)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/insurance-policy-updated/send")
    fun sendInsurancePolicyUpdatedNotification(@PathVariable(name = "memberId") memberId: String): ResponseEntity<*> {
        firebaseNotificationService.sendInsurancePolicyUpdatedNotification(memberId)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/insurance-renewed/send")
    fun sendInsuranceRenewedNotification(@PathVariable(name = "memberId") memberId: String): ResponseEntity<*> {
        firebaseNotificationService.sendInsuranceRenewedNotification(memberId)
        return ResponseEntity.noContent().build<Any>()
    }

    @PostMapping("/{memberId}/push/generic-communication/send")
    fun sendGenericCommunicationNotification(
        @PathVariable(name = "memberId") memberId: String,
        @RequestBody body: GenericCommunicationNotificationRequest
    ): ResponseEntity<*> {
        firebaseNotificationService
            .sendGenericCommunicationNotification(
                memberId,
                body.titleTextKey,
                body.bodyTextKey
            )
        return ResponseEntity.noContent().build<Any>()
    }
}
