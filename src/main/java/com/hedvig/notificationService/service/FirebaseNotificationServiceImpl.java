package com.hedvig.notificationService.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.hedvig.notificationService.enteties.FirebaseRepository;
import com.hedvig.notificationService.enteties.FirebaseToken;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

  public static final String TITLE = "Hedvig";
  public static final String BODY = "Hej, du har ett nytt meddelande från Hedvig!";

  private static Logger logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl.class);
  private final FirebaseRepository firebaseRepository;

  public FirebaseNotificationServiceImpl(FirebaseRepository firebaseRepository) {
    this.firebaseRepository = firebaseRepository;
  }

  @Override
  public void sendNewMessageNotification(String memberId) {

    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    Message message =
        Message.builder()
            .putData("title", TITLE)
            .putData("body", BODY)
            .setToken(firebaseToken.get().token)
            .build();

    try {
      String response = FirebaseMessaging.getInstance().send(message);

      logger.info("Response from pushing notification {}", response);
    } catch (FirebaseMessagingException e) {
      logger.error(
          "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
          memberId,
          e);
    }
  }

  @Override
  public void sendNotification(String memberId, String body) {
    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    Message message =
        Message.builder()
            .putData("title", TITLE)
            .putData("body", body)
            .setToken(firebaseToken.get().token)
            .build();
    try {
      String response = FirebaseMessaging.getInstance().send(message);

      logger.info("Response from pushing notification {}", response);
    } catch (FirebaseMessagingException e) {
      logger.error(
          "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
          memberId,
          e);
    }
  }
}
