package com.hedvig.notificationService.queue.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Deprecated
public class SendOldInsuranceCancellationEmailRequest extends JobRequest {

  String insurer;
}
