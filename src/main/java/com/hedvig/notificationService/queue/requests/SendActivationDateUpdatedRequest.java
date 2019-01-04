package com.hedvig.notificationService.queue.requests;

import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Deprecated
public class SendActivationDateUpdatedRequest extends JobRequest {

  @NotNull String insurer;
  @NotNull Instant activationDate;
}
