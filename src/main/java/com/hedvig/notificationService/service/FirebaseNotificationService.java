package com.hedvig.notificationService.service;

public interface FirebaseNotificationService {

  void sendNewMessageNotification(String memberId);

  void sendNotification(String memberId, String body);
}
