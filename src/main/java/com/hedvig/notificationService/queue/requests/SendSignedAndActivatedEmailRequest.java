package com.hedvig.notificationService.queue.requests;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SendSignedAndActivatedEmailRequest extends JobRequest {

}
