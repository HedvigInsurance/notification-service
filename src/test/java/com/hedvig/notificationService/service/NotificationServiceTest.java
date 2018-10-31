package com.hedvig.notificationService.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

import com.google.common.collect.Lists;
import com.hedvig.notificationService.queue.JobPoster;
import com.hedvig.notificationService.queue.requests.JobRequest;
import com.hedvig.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.notificationService.serviceIntegration.productsPricing.ProductClient;
import com.hedvig.notificationService.serviceIntegration.productsPricing.dto.InsuranceNotificationDTO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

  private static final String MEMBER_ID = "1337";
  public static final String UUID_STRING = "abb24426-ab60-11e8-a253-0bd2776efb39";
  public static final String TODAY_DATE_STRING = LocalDate.now().format(DateTimeFormatter.ISO_DATE);
  public static final LocalDateTime LOCAL_DATE_TIME_STRING = LocalDate.now().atStartOfDay();
  @Mock JobPoster jobPoster;

  @Mock ProductClient client;

  @Mock NotificationService notificationService;

  @Captor ArgumentCaptor<JobRequest> jobRequestCaptor;

  @Before
  public void setUp(){
    notificationService = new NotificationServiceImpl(jobPoster, client);
  }


  @Test
  public void sendActivationEmails() {

    // given
    willDoNothing().given(jobPoster).startJob(jobRequestCaptor.capture(), eq(false));

    val activeInsurances =
        Lists.newArrayList(
            new InsuranceNotificationDTO(
                UUID.fromString(UUID_STRING), MEMBER_ID, LOCAL_DATE_TIME_STRING));

    given(client.getInsurancesByActivationDate(TODAY_DATE_STRING))
        .willReturn(ResponseEntity.ok(activeInsurances));

    notificationService.sendActivationEmails(0);

    val expectedJobRequest = new SendActivationEmailRequest();
    expectedJobRequest.setMemberId(MEMBER_ID);
    expectedJobRequest.setRequestId(UUID_STRING);
    assertThat(jobRequestCaptor.getValue())
        .isExactlyInstanceOf(SendActivationEmailRequest.class)
        .isEqualToIgnoringGivenFields(expectedJobRequest, "requestId");
  }
}
