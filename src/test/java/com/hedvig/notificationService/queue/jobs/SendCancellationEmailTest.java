package com.hedvig.notificationService.queue.jobs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.hedvig.notificationService.queue.EmailSender;
import com.hedvig.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import com.hedvig.notificationService.service.FirebaseNotificationService;
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import java.time.LocalDate;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class SendCancellationEmailTest {

  @Mock EmailSender emailSender;
  @Mock MemberServiceClient memberServiceClient;
  @Mock FirebaseNotificationService firebaseNotificationService;


  @Test
  public void run_whenCurrentInsurer_isNull_setsInsurerToDefaultValue() throws IOException {

    final Member member = new Member(
        1337L,
        "19121212-1212",
        "Tolvan",
        "Tolvansson",
        "Långgatan",
        "Storstan",
        "12345",
        1,
        "tolvansson@hedvig.com",
        "12345",
        "SE",
        LocalDate.parse("1912-12-12"),
        "1"
        );
    given(memberServiceClient.profile("1337")).willReturn(ResponseEntity.ok(member));

    val sut = new SendCancellationEmail(emailSender, memberServiceClient, firebaseNotificationService);

    final SendOldInsuranceCancellationEmailRequest request = new SendOldInsuranceCancellationEmailRequest();
    request.setMemberId(member.getMemberId().toString());

    sut.run(request);

    then(emailSender).should().sendEmail(eq("1337"), anyString(), anyString(), contains("försäkringsbolag  och"), any());


  }


}