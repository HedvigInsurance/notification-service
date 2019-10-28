package com.hedvig.notificationService.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hedvig.notificationService.entities.FirebaseRepository;
import com.hedvig.notificationService.entities.FirebaseToken;
import java.util.Optional;

import com.hedvig.notificationService.serviceIntegration.memberService.MemberService;
import com.hedvig.service.LocalizationService;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseNotificationServiceTest {

  @Mock
  FirebaseRepository firebaseRepository;

  @Mock
  FirebaseMessaging firebaseMessaging;

  @Mock
  LocalizationService localizationService;

  @Mock
  MemberService memberService;

  @Test
  public void sendNotification_whenNoFireBaseTokenExists_returnsFalse(){

    val sut = new FirebaseNotificationServiceImpl(firebaseRepository, firebaseMessaging, localizationService, memberService);

    boolean result = sut.sendNotification("1337", "Hej där!");

    assertThat(result).isFalse();

  }

  @Test
  public void sendNotification_whenFireBaseTokenExistsForMember_returnsTrue(){

    val token = new FirebaseToken();
    token.memberId = "1337";
    token.token = "jaldskjalf";
    given(firebaseRepository.findById("1337")).willReturn(Optional.of(token));

    val sut = new FirebaseNotificationServiceImpl(firebaseRepository, firebaseMessaging, localizationService, memberService);

    boolean result = sut.sendNotification("1337", "Hej där!");

    assertThat(result).isTrue();

  }

}