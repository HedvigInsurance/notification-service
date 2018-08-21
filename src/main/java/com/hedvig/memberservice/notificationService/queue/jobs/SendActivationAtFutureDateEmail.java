package com.hedvig.memberservice.notificationService.queue.jobs;

import com.hedvig.memberservice.notificationService.queue.EmailSender;
import com.hedvig.memberservice.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.memberservice.notificationService.serviceIntegration.expo.ExpoNotificationService;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.memberservice.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import java.util.Objects;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SendActivationAtFutureDateEmail {

  private Logger log = LoggerFactory.getLogger(SendActivationEmail.class);

  private final MemberServiceClient memberServiceClient;
  private final ExpoNotificationService expoNotificationService;

  private final String mandateSentNotification;
  private final ClassPathResource signatureImage;
  private static final String PUSH_MESSAGE =
      "Hej %s! Dagen är äntligen här - din Hedvigförsäkring är aktiverad! 🎉🎉!"; // TODO: CHANGE

  private final EmailSender emailSender;

  public SendActivationAtFutureDateEmail(
      MemberServiceClient memberServiceClient,
      ExpoNotificationService expoNotificationService,
      EmailSender emailSender)
      throws IOException {
    this.memberServiceClient = memberServiceClient;
    this.expoNotificationService = expoNotificationService;
    this.emailSender = emailSender;
    this.mandateSentNotification = LoadEmail("activated.html"); //TODO: CHANGE
    this.signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
  }

  public void run(SendActivationAtFutureDateRequest request) {
    ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());

    Member body = profile.getBody();

    if (body != null) {
      if (body.getEmail() != null) {
        sendEmail(request.getMemberId(), body.getEmail(), body.getFirstName());
      } else {
        log.error(String.format("Could not find email on user with id: %s", request.getMemberId()));
      }

      sendPush(body.getMemberId(), body.getFirstName());
    }
    else{
      log.error("Response body from member-service is null: {}", profile);
    }
  }

  private void sendPush(Long memberId, String firstName) {

    String message = String.format(PUSH_MESSAGE, firstName);
    expoNotificationService.sendNotification(Objects.toString(memberId), message);
  }

  // TODO: CHANGE
  private void sendEmail(final String memberId, final String emailAddress, final String firstName) {
    val finalEmail = mandateSentNotification.replace("{NAME}", firstName);
    emailSender.sendEmail(memberId, "Goda nyheter 🚝", emailAddress, finalEmail, signatureImage);
  }

  private String LoadEmail(final String s) throws IOException {
    return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
  }
}
