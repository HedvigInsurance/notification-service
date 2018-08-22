package com.hedvig.notificationService.enteties;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class FirebaseToken {

  @Id public String memberId;

  @NotNull public String token;
}
