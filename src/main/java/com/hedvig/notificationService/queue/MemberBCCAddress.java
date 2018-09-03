package com.hedvig.notificationService.queue;

import lombok.Value;

@Value
public class MemberBCCAddress {

  final String[] parts;

  public MemberBCCAddress(final String emailAddress) {
    this.parts = emailAddress.split("@");
    if(parts.length != 2) {
      throw new IllegalArgumentException("emailAddress is not a valid email address: " + emailAddress);
    }
  }

  public String format(String memberId) {
    return String.format("%s+%s@%s", parts[0], memberId, parts[1]);
  }
}
