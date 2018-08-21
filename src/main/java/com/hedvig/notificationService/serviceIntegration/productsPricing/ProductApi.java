package com.hedvig.notificationService.serviceIntegration.productsPricing;

import com.hedvig.notificationService.serviceIntegration.productsPricing.dto.InsuranceNotificationDTO;
import feign.FeignException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class ProductApi {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProductApi.class);
  private final ProductClient client;

  @Autowired
  public ProductApi(ProductClient client) {
    this.client = client;
  }

  public List<InsuranceNotificationDTO> getInsurancesByActivationDate(LocalDate activationDate) {
    try {
      ResponseEntity<List<InsuranceNotificationDTO>> response =
          this.client.getInsurancesByActivationDate(
              activationDate.format(DateTimeFormatter.ISO_LOCAL_DATE));
      return response.getBody();
    } catch (FeignException ex) {
      if (ex.status() != 404) {
        log.error("Error getting insurances by activationDate from products-pricing", ex);
      }
    }
    return new ArrayList<>();
  }
}
