package com.hedvig.notificationService.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedvig.notificationService.queue.requests.JobRequest;
import io.sentry.Sentry;
import io.sentry.event.UserBuilder;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate;
import org.springframework.cloud.aws.messaging.core.SqsMessageHeaders;
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static org.springframework.cloud.aws.messaging.core.SqsMessageHeaders.SQS_DELAY_HEADER;

@Component
public class JobPosterImpl implements JobPoster {

  private final Logger log = LoggerFactory.getLogger(JobPosterImpl.class);

  private final QueueMessagingTemplate queueMessagingTemplate;
  private final ObjectMapper objectMapper;
  private final String queueName;

  public JobPosterImpl(
      QueueMessagingTemplate queueMessagingTemplate,
      ObjectMapper objectMapper,
      @Value("${hedvig.notification-service.queueTasklist}") String queueName) {
    this.queueMessagingTemplate = queueMessagingTemplate;
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

        log.error("Could not start job for message: {}", requestAsJson);

    } catch (Exception e) {
      log.error("Caught exception, {}", e.getMessage(), e);
    } finally {
      Sentry.clearContext();
      MDC.remove("memberId");
    }
  }
}
