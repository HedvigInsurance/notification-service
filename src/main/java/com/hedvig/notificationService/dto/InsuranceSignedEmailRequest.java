package com.hedvig.notificationService.dto;

import lombok.Value;

@Value
public class InsuranceSignedEmailRequest {

  long memberId;
  boolean switchingFromCurrentInsurer;
}
