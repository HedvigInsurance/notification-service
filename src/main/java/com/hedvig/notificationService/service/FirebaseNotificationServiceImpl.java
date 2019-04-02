package com.hedvig.notificationService.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;

import java.util.HashMap;
import java.util.Map;
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
  private final FirebaseMessaging firebaseMessaging;

  public FirebaseNotificationServiceImpl(FirebaseRepository firebaseRepository, FirebaseMessaging firebaseMessaging) {
    this.firebaseRepository = firebaseRepository;
    this.firebaseMessaging = firebaseMessaging;
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
      String response = firebaseMessaging.send(message);

      logger.info("Response from pushing notification {}", response);
    } catch (FirebaseMessagingException e) {
      logger.error(
          "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
          memberId,
          e);
    }
  }

  @Override
  public boolean sendNotification(String memberId, String body) {
    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    Notification notification = new Notification(TITLE, body);

    if (firebaseToken.isPresent()) {
      Message message =
          Message.builder()
              .setNotification(notification)
              .putData(TYPE, EMAIL)
              .setToken(firebaseToken.get().token)
              .build();
      try {
        String response = firebaseMessaging.send(message);

        logger.info("Response from pushing notification {}", response);
      } catch (FirebaseMessagingException e) {
        logger.error(
            "SendNewMessageNotification: Cannot send notification with memberId {} through firebase. Error: {}",
            memberId,
            e);
        return false;
      }
      return true;
    }else {
      return false;
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
    Firestore db = FirestoreClient.getFirestore();
    CollectionReference collection = db.collection("push-notification-tokens");

    ApiFuture<QuerySnapshot> future = collection
            .whereEqualTo("token", token)
            .whereEqualTo("memberId", memberId).get();

    try {
      QuerySnapshot snapshot = future.get();

      if (snapshot.getDocuments().isEmpty()) {
        Map<String, Object> docData = new HashMap<>();
        docData.put("memberId", memberId);
        docData.put("token", token);
        collection.add(docData);
      }
    } catch (Exception e) {
      logger.error(
              "Something went wrong while fetching documents from firebase",
              e
      );
    }

    FirebaseToken firebaseToken = new FirebaseToken();
    firebaseToken.setMemberId(memberId);
    firebaseToken.setToken(token);

    firebaseRepository.save(firebaseToken);
  }
}
