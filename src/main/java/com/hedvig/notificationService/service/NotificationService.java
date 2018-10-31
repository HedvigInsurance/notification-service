package com.hedvig.notificationService.service;

import com.hedvig.notificationService.dto.CancellationEmailSentToInsurerRequest;
import com.hedvig.notificationService.dto.InsuranceActivationDateUpdatedRequest;
import java.util.List;

public interface NotificationService {

  void cancellationEmailSentToInsurer(long memberId, CancellationEmailSentToInsurerRequest insurer);

  void cancellationEmailSentToInsurer(long memberId, String insurer);

  void insuranceActivationDateUpdated(long memberId, InsuranceActivationDateUpdatedRequest request);

  void insuranceActivated(long memberId);

  List<String> sendActivationEmails(int NumberOfDaysFromToday);

  void insuranceSignedAndActivated(long memberId);
}
