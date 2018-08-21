package com.hedvig.notificationService.serviceIntegration.memberService.dto;

import lombok.Value;

@Value
public class BankIdAuthRequest {
    private String ssn;
    private String memberId;
}
