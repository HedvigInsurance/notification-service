package com.hedvig.notificationService.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FirebaseNotificationServiceImpl implements FirebaseNotificationService {

  public static final String TITLE = "Hedvig";
  public static final String NEW_MESSAGE_BODY = "Hej, du har ett nytt meddelande från Hedvig!";
  public static final String REFERRAL_SUCCESS_BODY = "%s skaffade Hedvig tack vare dig!";

  public static final String TYPE = "TYPE";
  public static final String NEW_MESSAGE = "NEW_MESSAGE";
  public static final String REFERRAL_SUCCESS = "REFERRAL_SUCCESS";
  public static final String EMAIL = "EMAIL";
  public static final String SIMPLE = "SIMPLE";

  public static final String DATA_MESSAGE_TITLE = "DATA_MESSAGE_TITLE";
  public static final String DATA_MESSAGE_BODY = "DATA_MESSAGE_BODY";

  public static final String DATA_MESSAGE_REFERRED_SUCCESS_NAME = "DATA_MESSAGE_REFERRED_SUCCESS_NAME";
  public static final String DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT";
  public static final String DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY = "DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY";

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

    Message message =
        Message.builder()
            .putData(TYPE, NEW_MESSAGE)
            .setApnsConfig(createApnsConfig(NEW_MESSAGE_BODY).build())
            .setAndroidConfig(createAndroidConfigBuilder(NEW_MESSAGE_BODY, NEW_MESSAGE).build())
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
  public void sendReferredSuccessNotification(String memberId, String referredName, String incentiveAmount, String incentiveCurrency) {
    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    String body = String.format(REFERRAL_SUCCESS_BODY, referredName);

    Message message = Message
        .builder()
        .putData(TYPE, REFERRAL_SUCCESS)
        .setApnsConfig(
            createApnsConfig(body)
                .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                .putCustomData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                .build())
        .setAndroidConfig(
            createAndroidConfigBuilder(body, REFERRAL_SUCCESS)
                .putData(DATA_MESSAGE_REFERRED_SUCCESS_NAME, referredName)
                .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_AMOUNT, incentiveAmount)
                .putData(DATA_MESSAGE_REFERRED_SUCCESS_INCENTIVE_CURRENCY, incentiveCurrency)
                .build())
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
  public void sendSimpleNotification(String memberId, String title, String body) {
    Optional<FirebaseToken> firebaseToken = firebaseRepository.findById(memberId);

    Message message = Message
            .builder()
            .putData(TYPE, SIMPLE)
            .setApnsConfig(createApnsConfig(title, body).build())
            .setAndroidConfig(createAndroidConfigBuilder(title, body, SIMPLE).build())
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

    if (firebaseToken.isPresent()) {
      Message message =
          Message.builder()
              .putData(TYPE, EMAIL)
              .setApnsConfig(createApnsConfig(body).build())
              .setAndroidConfig(createAndroidConfigBuilder(body, EMAIL).build())
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
    } else {
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


  private ApnsConfig.Builder createApnsConfig(String body) {
    return createApnsConfig(FirebaseNotificationServiceImpl.TITLE, body);
  }

  private ApnsConfig.Builder createApnsConfig(String title, String body) {
    return ApnsConfig
        .builder()
        .setAps(Aps
            .builder()
            .setAlert(ApsAlert
                .builder()
                .setTitle(title)
                .setBody(body)
                .build()
            ).build());
  }

  private AndroidConfig.Builder createAndroidConfigBuilder(String body, String type) {
    return createAndroidConfigBuilder(FirebaseNotificationServiceImpl.TITLE, body, type);
  }

  private AndroidConfig.Builder createAndroidConfigBuilder(String title, String body, String type) {
    return AndroidConfig
        .builder()
        .putData(DATA_MESSAGE_TITLE, title)
        .putData(DATA_MESSAGE_BODY, body)
        .putData(TYPE, type);
  }
}
