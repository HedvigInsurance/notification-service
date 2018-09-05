package com.hedvig.notificationService.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class CancellationEmailSentToInsurerRequest {
  @NotNull @NotEmpty String insurer;
}
