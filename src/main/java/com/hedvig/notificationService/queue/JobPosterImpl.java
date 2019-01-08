package com.hedvig.notificationService.queue;

import static org.springframework.cloud.aws.messaging.core.SqsMessageHeaders.SQS_DELAY_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.notificationService.queue.jobs.SendActivationAtFutureDateEmail;
import com.hedvig.notificationService.queue.jobs.SendActivationDateUpdatedEmail;
import com.hedvig.notificationService.queue.jobs.SendActivationEmail;
import com.hedvig.notificationService.queue.jobs.SendCancellationEmail;
import com.hedvig.notificationService.queue.jobs.SendSignedAndActivatedEmail;
import com.hedvig.notificationService.queue.requests.JobRequest;
import com.hedvig.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import com.hedvig.notificationService.queue.requests.SendSignedAndActivatedEmailRequest;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import java.util.HashMap;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.SqsMessageHeaders;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

@Component
public class JobPosterImpl implements JobPoster {

  private final Logger log = LoggerFactory.getLogger(JobPosterImpl.class);

  private final SendCancellationEmail sendCancellationEmail;
  private final SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail;
  private final SendActivationEmail sendActivationEmail;
  private final SendActivationAtFutureDateEmail sendActivationAtFutureDateEmail;
  private final SendSignedAndActivatedEmail sendSignedAndActivatedEmail;
  private final QueueMessagingTemplate queueMessagingTemplate;
  private final ObjectMapper objectMapper;
  private final String queueName;

  public JobPosterImpl(
      SendCancellationEmail sendCancellationEmail,
      SendActivationDateUpdatedEmail sendActivationDateUpdatedEmail,
      SendActivationEmail sendActivationEmail,
      SendActivationAtFutureDateEmail sendActivationAtFutureDateEmail,
      SendSignedAndActivatedEmail sendSignedAndActivatedEmail,
      QueueMessagingTemplate queueMessagingTemplate,
      ObjectMapper objectMapper,
      @Value("${hedvig.notification-service.queueTasklist}") String queueName) {
    this.sendCancellationEmail = sendCancellationEmail;
    this.sendActivationDateUpdatedEmail = sendActivationDateUpdatedEmail;
    this.sendActivationEmail = sendActivationEmail;
    this.sendSignedAndActivatedEmail = sendSignedAndActivatedEmail;
    this.queueMessagingTemplate = queueMessagingTemplate;
    this.sendActivationAtFutureDateEmail = sendActivationAtFutureDateEmail;
    this.objectMapper = objectMapper;
    this.queueName = queueName;
  }

  @Override
  public void startJob(JobRequest request, boolean delay) {

    val headers = new HashMap<String, Object>();
    if (delay) {
      headers.put(SQS_DELAY_HEADER, 600);
    }
    SqsMessageHeaders sqsMessageHeaders = new SqsMessageHeaders(headers);
    try {
      String requestAsJson = objectMapper.writeValueAsString(request);
      log.info("Sending jobrequest to sqs queue: {} ", requestAsJson);
      this.queueMessagingTemplate.convertAndSend(queueName, requestAsJson, sqsMessageHeaders);
    } catch (JsonProcessingException ex) {
      log.error("Could not convert request to json: {}", request, ex);
    }
  }

  @SqsListener("${hedvig.notification-service.queueTasklist}")
  public void jobListener(String requestAsJson) {

    try {
      JobRequest request = objectMapper.readValue(requestAsJson, JobRequest.class);
      log.info("Receiving jobrequest from sqs queue: {} ", requestAsJson);

      val sentryContext = Sentry.getContext();
      sentryContext.setUser(new UserBuilder().setId(request.getMemberId()).build());
      MDC.put("memberId", request.getMemberId());

      // TODO kill 👇 once we deleted all producers
//      if (SendOldInsuranceCancellationEmailRequest.class.isInstance(request)) {
//        sendCancellationEmail.run((SendOldInsuranceCancellationEmailRequest) request);
//      } else if (SendActivationDateUpdatedRequest.class.isInstance(request)) {
//        sendActivationDateUpdatedEmail.run((SendActivationDateUpdatedRequest) request);
//      } else if (SendActivationEmailRequest.class.isInstance(request)) {
//        sendActivationEmail.run((SendActivationEmailRequest) request);
//      } else if (SendActivationAtFutureDateEmail.class.isInstance(request)) {
//        sendActivationAtFutureDateEmail.run((SendActivationAtFutureDateRequest) request);
//      } else if (SendSignedAndActivatedEmailRequest.class.isInstance(request)){
//        sendSignedAndActivatedEmail.run((SendSignedAndActivatedEmailRequest) request);
//      }else {
//      }
        log.error("Could not start job for message: {}", requestAsJson);

    } catch (Exception e) {
      log.error("Caught exception, {}", e.getMessage(), e);
    } finally {
      Sentry.clearContext();
      MDC.remove("memberId");
    }
  }
}
