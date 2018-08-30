package com.hedvig.notificationService.service;

import com.hedvig.notificationService.entities.FirebaseToken;
import java.util.Optional;

public interface FirebaseNotificationService {

  void sendNewMessageNotification(String memberId);

  void sendNotification(String memberId, String body);

  Optional<FirebaseToken> getFirebaseToken(String memberId);

  void setFirebaseToken(String memberId, String token);
}
