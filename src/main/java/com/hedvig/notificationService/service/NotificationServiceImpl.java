package com.hedvig.notificationService.service;

import com.google.common.collect.Lists;
import com.hedvig.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import com.hedvig.notificationService.queue.JobPoster;
import com.hedvig.notificationService.queue.requests.SendActivationAtFutureDateRequest;
import com.hedvig.notificationService.queue.requests.SendActivationDateUpdatedRequest;
import com.hedvig.notificationService.queue.requests.SendActivationEmailRequest;
import com.hedvig.notificationService.queue.requests.SendOldInsuranceCancellationEmailRequest;
import com.hedvig.notificationService.queue.requests.SendSignedAndActivatedEmailRequest;
import com.hedvig.notificationService.serviceIntegration.productsPricing.ProductClient;
import com.hedvig.notificationService.serviceIntegration.productsPricing.dto.InsuranceNotificationDTO;
import feign.FeignException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {
  private final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final JobPoster jobPoster;
  private final ProductClient productClient;

  public NotificationServiceImpl(JobPoster jobPoster,
      ProductClient productClient) {
    this.jobPoster = jobPoster;
    this.productClient = productClient;
  }

  @Override
  @Deprecated
  public void cancellationEmailSentToInsurer(
      final long memberId, final CancellationEmailSentToInsurerRequest insurer) {
    SendOldInsuranceCancellationEmailRequest request =
        new SendOldInsuranceCancellationEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    request.setInsurer(insurer.getInsurer());

    jobPoster.startJob(request, true);
  }

  @Override
  public void cancellationEmailSentToInsurer(
      final long memberId, final @NotNull String insurer) {
    SendOldInsuranceCancellationEmailRequest request = new SendOldInsuranceCancellationEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    request.setInsurer(insurer);

    jobPoster.startJob(request, true);
  }

  @Override
  public void insuranceActivationDateUpdated(
      final long memberId, final InsuranceActivationDateUpdatedRequest request) {
    SendActivationDateUpdatedRequest request2 = new SendActivationDateUpdatedRequest();
    request2.setRequestId(UUID.randomUUID().toString());
    request2.setMemberId(Objects.toString(memberId));
    request2.setInsurer(request.getCurrentInsurer());
    request2.setActivationDate(request.getActivationDate());
    jobPoster.startJob(request2, false);
  }

  @Override
  public void insuranceActivated(final long memberId) {
    SendActivationEmailRequest request = new SendActivationEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    jobPoster.startJob(request, false);
  }

  @Override
  public void insuranceSignedAndActivated(final long memberId) {
    SendSignedAndActivatedEmailRequest request = new SendSignedAndActivatedEmailRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    jobPoster.startJob(request, true);
  }

  @Override
  public List<String> sendActivationEmails(int NumberOfDaysFromToday) {
    try {
      final String activationDate = LocalDate.now().plusDays(NumberOfDaysFromToday)
          .format(DateTimeFormatter.ISO_LOCAL_DATE);
      val insuranceResponse = productClient.getInsurancesByActivationDate(activationDate);

      final List<InsuranceNotificationDTO> insurancesToRemind = insuranceResponse.getBody();

      return sendActivationEmails(NumberOfDaysFromToday, insurancesToRemind != null ? insurancesToRemind: Lists.newArrayList());
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error from products-pricing", ex.getMessage());
        throw new RuntimeException("Error from products-pricing", ex);
      }
      return new ArrayList<>();
    }
  }

  private List<String> sendActivationEmails(int NumberOfDaysFromToday, @NonNull List<InsuranceNotificationDTO> insurancesToRemind) {
    List<String> receivers = Lists.newArrayList();

    if (NumberOfDaysFromToday == 0) {
      insurancesToRemind.forEach(
          i -> {
            insuranceActivated(Long.parseLong(i.getMemberId()));
            receivers.add(i.getMemberId());
          });
    } else { //TODO: fix this, the maill with no be triggered. Check jobListener method in the JobPosterImpl class
      insurancesToRemind.forEach(
          i -> {
            insuranceActivationAtFutureDate(
                Long.parseLong(i.getMemberId()),
                ZonedDateTime.ofLocal(
                    i.getActivationDate(),
                    ZoneId.of("Europe/Stockholm"),
                    ZoneId.of("Europe/Stockholm").getRules().getOffset(Instant.now()))
                    .format(DateTimeFormatter.ISO_DATE));
            receivers.add(i.getMemberId());
          });
    }

    return receivers;
  }

  private void insuranceActivationAtFutureDate(final long memberId, final String activationDate) {
    SendActivationAtFutureDateRequest request = new SendActivationAtFutureDateRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setMemberId(Objects.toString(memberId));
    request.setActivationDate(activationDate);
    jobPoster.startJob(request, false);
  }
}
