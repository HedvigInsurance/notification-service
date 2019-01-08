package com.hedvig.notificationService.queue;

import java.util.List;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailSender {

  private final Logger log = LoggerFactory.getLogger(EmailSender.class);
  private final JavaMailSender mailSender;
  private final List<MemberBCCAddress> bcc;

  public EmailSender(
      JavaMailSender mailSender, List<MemberBCCAddress> bcc) {
    this.mailSender = mailSender;
    this.bcc = bcc;
  }

  @Deprecated // Emails should be sent from CRM
  public void sendEmail(
      final String memberId,
      final String subject,
      final String email,
      final String html,
      final ClassPathResource signatureImage) {
    try {
      val message = mailSender.createMimeMessage();
      val helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setSubject(subject);
      helper.setFrom("\"Hedvig\" <hedvig@hedvig.com>");
      helper.setTo(email);
      helper.setBcc(
          bcc.stream()
              .map(x -> x.format(memberId))
              .toArray(String[]::new));

      helper.setText(html, true);
      if (signatureImage != null) {
        helper.addInline("image002.jpg", signatureImage);
      }

      // TODO kill 👇 when we're sure we don't break stuff
      log.warn("DEPRECATED: Tried to send email from notification service when you shouldn't [subject=\"{}\", memberId={}]", subject, memberId);
      // mailSender.send(message);
    } catch (Exception e) {
      log.error("Could not send email to member", e);
      throw new RuntimeException("Could not send email to member", e);
    }
  }
}
