package com.hedvig.notificationService.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;
import java.util.Optional;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

  public static final String TITLE = "Hedvig";
  public static final String BODY = "Hej, du har ett nytt meddelande från Hedvig!";

  public static final String TYPE = "TYPE";
  public static final String NEW_MESSAGE = "NEW_MESSAGE";
  public static final String EMAIL = "EMAIL";

  private static Logger logger = LoggerFactory.getLogger(FirebaseNotificationServiceImpl.class);
  private final FirebaseRepository firebaseRepository;

  public FirebaseNotificationServiceImpl(FirebaseRepository firebaseRepository) {
    this.firebaseRepository = firebaseRepository;
  }

  @Override
  public void sendNewMessageNotification(String memberId) {

    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    Notification notification = new Notification(TITLE, BODY);

    Message message =
        Message.builder()
            .setNotification(notification)
            .putData(TYPE, NEW_MESSAGE)
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

    Notification notification = new Notification(TITLE, body);

    Message message =
        Message.builder()
            .setNotification(notification)
            .putData(TYPE, EMAIL)
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
  @Transactional
  public Optional<FirebaseToken> getFirebaseToken(String memberId) {
    return firebaseRepository.findById(memberId);
  }

  @Override
  @Transactional
  public void setFirebaseToken(String memberId, String token) {
    FirebaseToken firebaseToken = new FirebaseToken();
    firebaseToken.setMemberId(memberId);
    firebaseToken.setToken(token);

    firebaseRepository.save(firebaseToken);
  }
}
