package com.hedvig.notificationService.serviceIntegration.productsPricing;

import com.hedvig.notificationService.serviceIntegration.productsPricing.dto.InsuranceNotificationDTO;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productPricing", url = "${hedvig.productsPricing.url}")
public interface ProductClient {

  @GetMapping("/_/insurance/searchByActivationDate?activationDate={date}")
  ResponseEntity<List<InsuranceNotificationDTO>> getInsurancesByActivationDate(
      @PathVariable("date") String activationDate);
}
