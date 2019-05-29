package com.hedvig.notificationService.dto;

import lombok.Value;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Value
public class ReferralsSuccessSendNotificationRequest {
  @NotNull @NotEmpty String referredName;
}
