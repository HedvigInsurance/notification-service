package com.hedvig.notificationService.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public final class ReferralsSuccessSendNotificationRequest {
  @NotNull @NotEmpty
  private final String referredName;
  @NotNull @NotEmpty
  private final String incentiveAmount;
  @NotNull @NotEmpty
  private final String incentiveCurrency;

  @java.beans.ConstructorProperties({"referredName", "incentiveAmount", "incentiveCurrency"})
  public ReferralsSuccessSendNotificationRequest(@NotNull @NotEmpty String referredName, @NotNull @NotEmpty String incentiveAmount, @NotNull @NotEmpty String incentiveCurrency) {
    this.referredName = referredName;
    this.incentiveAmount = incentiveAmount;
    this.incentiveCurrency = incentiveCurrency;
  }

  public @NotNull @NotEmpty String getReferredName() {
    return this.referredName;
  }

  public @NotNull @NotEmpty String getIncentiveAmount() {
    return this.incentiveAmount;
  }

  public @NotNull @NotEmpty String getIncentiveCurrency() {
    return this.incentiveCurrency;
  }

  public boolean equals(final Object o) {
    if (o == this) return true;
    if (!(o instanceof ReferralsSuccessSendNotificationRequest)) return false;
    final ReferralsSuccessSendNotificationRequest other = (ReferralsSuccessSendNotificationRequest) o;
    final Object this$referredName = this.getReferredName();
    final Object other$referredName = other.getReferredName();
    if (this$referredName == null ? other$referredName != null : !this$referredName.equals(other$referredName))
      return false;
    final Object this$incentiveAmount = this.getIncentiveAmount();
    final Object other$incentiveAmount = other.getIncentiveAmount();
    if (this$incentiveAmount == null ? other$incentiveAmount != null : !this$incentiveAmount.equals(other$incentiveAmount))
      return false;
    final Object this$incentiveCurrency = this.getIncentiveCurrency();
    final Object other$incentiveCurrency = other.getIncentiveCurrency();
    if (this$incentiveCurrency == null ? other$incentiveCurrency != null : !this$incentiveCurrency.equals(other$incentiveCurrency))
      return false;
    return true;
  }

  public int hashCode() {
    final int PRIME = 59;
    int result = 1;
    final Object $referredName = this.getReferredName();
    result = result * PRIME + ($referredName == null ? 43 : $referredName.hashCode());
    final Object $incentiveAmount = this.getIncentiveAmount();
    result = result * PRIME + ($incentiveAmount == null ? 43 : $incentiveAmount.hashCode());
    final Object $incentiveCurrency = this.getIncentiveCurrency();
    result = result * PRIME + ($incentiveCurrency == null ? 43 : $incentiveCurrency.hashCode());
    return result;
  }

  public String toString() {
    return "ReferralsSuccessSendNotificationRequest(referredName=" + this.getReferredName() + ", incentiveAmount=" + this.getIncentiveAmount() + ", incentiveCurrency=" + this.getIncentiveCurrency() + ")";
  }
}
