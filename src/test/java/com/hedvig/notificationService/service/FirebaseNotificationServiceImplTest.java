package com.hedvig.notificationService.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;
import java.util.Optional;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseNotificationServiceImplTest {

  @Mock
  FirebaseRepository firebaseRepository;

  @Mock
  FirebaseMessaging firebaseMessaging;

  @Test
  public void sendNotification_whenNoFireBaseTokenExists_returnsFalse(){

    val sut = new FirebaseNotificationServiceImpl(firebaseRepository, firebaseMessaging);

    boolean result = sut.sendNotification("1337", "Hej där!");

    assertThat(result).isFalse();

  }

  @Test
  public void sendNotification_whenFireBaseTokenExistsForMember_returnsTrue(){

    val token = new FirebaseToken();
    token.memberId = "1337";
    token.token = "jaldskjalf";
    given(firebaseRepository.findById("1337")).willReturn(Optional.of(token));

    val sut = new FirebaseNotificationServiceImpl(firebaseRepository, firebaseMessaging);

    boolean result = sut.sendNotification("1337", "Hej där!");

    assertThat(result).isTrue();

  }

}