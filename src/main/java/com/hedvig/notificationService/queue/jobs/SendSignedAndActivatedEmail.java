package com.hedvig.notificationService.queue.jobs;

import com.hedvig.notificationService.queue.EmailSender;
import com.hedvig.notificationService.queue.requests.SendSignedAndActivatedEmailRequest;
import com.hedvig.notificationService.service.FirebaseNotificationService;
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import javax.validation.constraints.NotNull;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component

public class SendSignedAndActivatedEmail {
  private final Logger log = LoggerFactory.getLogger(SendSignedAndActivatedEmail.class);

  private final EmailSender emailSender;
  private final MemberServiceClient memberServiceClient;
  private final FirebaseNotificationService firebaseNotificationService;

  private final String signedAndActivatedNotification;
  private final ClassPathResource signatureImage;
  private static final String PUSH_MESSAGE = "Välkommen till Hedvig!👋";

  public SendSignedAndActivatedEmail(
      EmailSender emailSender,
      MemberServiceClient memberServiceClient,
      FirebaseNotificationService firebaseNotificationService)
      throws IOException {
    this.emailSender = emailSender;
    this.memberServiceClient = memberServiceClient;
    this.firebaseNotificationService = firebaseNotificationService;

    signedAndActivatedNotification = LoadEmail("onboarded_today.html");
    signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
  }

  public void run(SendSignedAndActivatedEmailRequest request) {
    ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());
    Member body = profile.getBody();

    if (body != null) {
      if (body.getEmail() != null) {
        sendEmail(request.getMemberId(), body.getEmail(), body.getFirstName());
      } else {
        log.error(String.format("Could not find email on user with id: %s", request.getMemberId()));
      }

    } else {
      log.error("Response body from member-service is null: {}", profile);
    }
  }

  private void sendEmail(@NotNull final String memberId, final String email, final String firstName) {
    val finalEmail = signedAndActivatedNotification.replace("{NAME}", firstName);
    emailSender.sendEmail(memberId, "Välkommen till Hedvig 😍", email, finalEmail, signatureImage);
  }

  private String LoadEmail(final String s) throws IOException {
    return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
  }
}
