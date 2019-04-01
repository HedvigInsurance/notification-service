package com.hedvig.notificationService.web;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.hedvig.notificationService.entities.FirebaseToken;
import com.hedvig.notificationService.service.FirebaseNotificationService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/_/notifications")
public class FirebaseController {

  private final Logger log = LoggerFactory.getLogger(FirebaseController.class);
  private final FirebaseNotificationService firebaseNotificationService;

  public FirebaseController(FirebaseNotificationService firebaseNotificationService) {
    this.firebaseNotificationService = firebaseNotificationService;
  }

  @PostMapping("/{memberId}/token")
  public ResponseEntity<?> saveFirebaseToken(
      @PathVariable(name = "memberId") String memberId, @RequestBody String token) {

    Map<String, Object> docData = new HashMap<>();
    docData.put("memberId", memberId);
    docData.put("token", token);

    Firestore db = FirestoreClient.getFirestore();

    db.collection("push-notification-tokens").add(docData);

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
