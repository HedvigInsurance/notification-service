package com.hedvig.notificationService.serviceIntegration.productsPricing.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Value;

@Value
public class InsuranceNotificationDTO {
  private UUID insuranceId;
  private String memberId;
  private LocalDateTime activationDate;
}
