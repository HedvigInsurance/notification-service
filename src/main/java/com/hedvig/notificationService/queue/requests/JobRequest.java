package com.hedvig.notificationService.queue.requests;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.NonFinal;

@Data
@NonFinal
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SendOldInsuranceCancellationEmailRequest.class, name = "sendOldInsuranceCancellationEmailRequest"),
        @JsonSubTypes.Type(value = SendActivationEmailRequest.class, name = "sendActivationEmailRequest"),
        @JsonSubTypes.Type(value = SendActivationDateUpdatedRequest.class, name = "sendActivationDateUpdatedRequest"),
        @JsonSubTypes.Type(value = SendSignedAndActivatedEmailRequest.class, name = "sendSignedAndActivatedEmailRequest")
})
public class JobRequest {
  @NotNull
  String memberId;

  @NotNull
  String requestId;
}
