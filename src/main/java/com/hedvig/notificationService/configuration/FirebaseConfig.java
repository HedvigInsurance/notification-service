package com.hedvig.notificationService.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hedvig.notificationService.service.firebase.FirebaseMessager;
import com.hedvig.notificationService.service.firebase.RealFirebaseMessenger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@ConditionalOnProperty(value = "hedvig.usefakes", havingValue = "false", matchIfMissing = true)
@Configuration
public class FirebaseConfig {

  private final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

  @Value("${hedvig.firebase.database.url}")
  private String databaseUrl;

  @Value("${hedvig.firebase.config.path}")
  private File config;

  @PostConstruct
  public void init() throws IOException {
    logger.info("Initializing FirebaseApp");
    InputStream inputStream = new FileInputStream(config);

    FirebaseOptions options =
        new FirebaseOptions.Builder()
            .setCredentials(GoogleCredentials.fromStream(inputStream))
            .setDatabaseUrl(databaseUrl)
            .build();

    FirebaseApp.initializeApp(options);
    logger.info("FirebaseApp Initialized");
  }

  @Bean()
  @DependsOn("firebaseConfig")
  public FirebaseMessager firebaseMessaging() {
    return new RealFirebaseMessenger(FirebaseMessaging.getInstance());
  }
}
