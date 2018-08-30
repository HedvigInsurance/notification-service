package com.hedvig.notificationService.web;

import com.hedvig.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;
import com.hedvig.notificationService.entities.MailConfirmation;
import com.hedvig.notificationService.entities.MailRepository;
import com.hedvig.notificationService.service.FirebaseNotificationService;
import com.hedvig.notificationService.service.NotificationService;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import javax.validation.Valid;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/notifications")
public class NotificationsController {

  private final Logger log = LoggerFactory.getLogger(NotificationsController.class);
  private final NotificationService notificationService;
  private final MailRepository mailRepository;
  private final FirebaseNotificationService firebaseNotificationService;

  public NotificationsController(
      NotificationService notificationService,
      MailRepository mailRepository,
      FirebaseNotificationService firebaseNotificationService) {
    this.notificationService = notificationService;
    this.mailRepository = mailRepository;
    this.firebaseNotificationService = firebaseNotificationService;
  }

  @PostMapping("/{memberId}/cancellationEmailSentToInsurer")
  public ResponseEntity<?> cancellationEmailSentToInsurer(
      @PathVariable Long memberId, @RequestBody CancellationEmailSentToInsurerRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.cancellationEmailSentToInsurer(memberId, body);
    } catch (MailException e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{memberId}/insuranceActivated")
  public ResponseEntity<?> insuranceActivated(@PathVariable Long memberId) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivated(memberId);
    } catch (MailException e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{memberId}/insuranceActivationDateUpdated")
  public ResponseEntity<?> insuranceActivationDateUpdated(
      @PathVariable Long memberId, @RequestBody @Valid InsuranceActivationDateUpdatedRequest body) {
    MDC.put("memberId", Objects.toString(memberId));
    try {
      notificationService.insuranceActivationDateUpdated(memberId, body);
    } catch (MailException e) {
      log.error("Could not send email to member", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    return ResponseEntity.noContent().build();
  }

  /**
   * This endpoint is called x days before the activation, in order to notify members for their
   * insurance's activation. @RequestBody NumberOfDaysFromToday the numbers from today that the
   * insurance will be activated
   *
   * @return 204 on success
   *     <p>or
   * @return 404 not found if there is no isnurance that will be activated on that date
   */
  @PostMapping("/insuranceWillBeActivatedAt")
  public ResponseEntity<?> insuranceReminder(@RequestBody int NumberOfDaysFromToday) {

    if (NumberOfDaysFromToday < 0) {
      return ResponseEntity.badRequest().build();
    }

    return ResponseEntity.ok(notificationService.sendActivationEmails(NumberOfDaysFromToday));
  }

  @PostMapping("/mailConfirmed")
  public ResponseEntity<?> mailConfirmed(@RequestBody String memberId) {
    val conf = new MailConfirmation();
    conf.setMemberId(memberId);
    conf.setConfirmationId(UUID.randomUUID().toString());
    mailRepository.save(conf);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{memberId}/token")
  public ResponseEntity<?> saveFirebaseToken(
      @PathVariable(name = "memberId") String memberId, @RequestBody String token) {
    try {
      firebaseNotificationService.setFirebaseToken(memberId, token);
    } catch (Exception e) {
      log.error(
          "Something went wrong while the Token {} for member {} was about to be stored in the database with error {}",
          token,
          memberId,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{memberId}/token")
  public ResponseEntity<?> getFirebaseToken(@PathVariable(name = "memberId") String memberId) {
    try {
      Optional<FirebaseToken> firebaseTokenOptional =
          firebaseNotificationService.getFirebaseToken(memberId);
      return firebaseTokenOptional
          .map(firebaseToken -> ResponseEntity.ok(firebaseToken.token))
          .orElseGet(() -> ResponseEntity.notFound().build());
    } catch (Exception e) {
      log.error(
          "Something went wrong while trying to fetch the firebase token for member {} with error {}",
          memberId,
          e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  @PostMapping("/{memberId}/push/send")
  public ResponseEntity<?> sendPushNotification(@PathVariable(name = "memberId") String memberId) {
    firebaseNotificationService.sendNewMessageNotification(memberId);
    return ResponseEntity.noContent().build();
  }
}
