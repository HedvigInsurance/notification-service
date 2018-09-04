package com.hedvig.notificationService.queue.jobs;

import com.hedvig.notificationService.queue.EmailSender;
import com.hedvig.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import com.hedvig.notificationService.service.FirebaseNotificationService;
import com.hedvig.notificationService.serviceIntegration.memberService.MemberServiceClient;
import com.hedvig.notificationService.serviceIntegration.memberService.dto.Member;
import java.io.IOException;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class SendCancellationEmail {

  private final Logger log = LoggerFactory.getLogger(SendCancellationEmail.class);

  private final EmailSender emailSender;
  private final MemberServiceClient memberServiceClient;
  private final FirebaseNotificationService firebaseNotificationService;

  private final String mandateSentNotification;
  private final ClassPathResource signatureImage;
  private static final String PUSH_MESSAGE =
      "Hej %s! Tänkte bara meddela att vi har skickat en uppsägning till ditt gamla försäkringsbolag %s nu! Jag återkommer till dig när de har bekräftat ditt uppsägningsdatum. Hej så länge! 👋";

  public SendCancellationEmail(
      EmailSender emailSender,
      MemberServiceClient memberServiceClient,
      FirebaseNotificationService firebaseNotificationService)
      throws IOException {
    this.emailSender = emailSender;
    this.memberServiceClient = memberServiceClient;
    this.firebaseNotificationService = firebaseNotificationService;

    mandateSentNotification = LoadEmail("notifications/insurance_mandate_sent_to_insurer.html");
    signatureImage = new ClassPathResource("mail/wordmark_mail.jpg");
  }

  public void run(SendOldInsuranceCancellationEmailRequest request) {
    ResponseEntity<Member> profile = memberServiceClient.profile(request.getMemberId());
    Member body = profile.getBody();

    if (body != null) {
      if (body.getEmail() != null) {
        sendEmail(request.getMemberId(), body.getEmail(), body.getFirstName(), request.getInsurer());
      } else {
        log.error(String.format("Could not find email on user with id: %s", request.getMemberId()));
      }

      sendPush(body.getMemberId(), body.getFirstName(), request.getInsurer());

    } else {
      log.error("Response body from member-service is null: {}", profile);
    }
  }

  private void sendPush(Long memberId, String firstName, String insurer) {

    String message = String.format(PUSH_MESSAGE, firstName, insurer);
    firebaseNotificationService.sendNotification(Objects.toString(memberId), message);
  }

  private void sendEmail(@NotNull final String memberId,
      final String email, final String firstName, final String insurer) {

    val finalEmail =
        mandateSentNotification.replace("{NAME}", firstName).replace("{INSURER}", insurer);

    emailSender.sendEmail(memberId, "Välkommen till Hedvig 😍", email, finalEmail, signatureImage);
  }

  private String LoadEmail(final String s) throws IOException {
    return IOUtils.toString(new ClassPathResource("mail/" + s).getInputStream(), "UTF-8");
  }
}
