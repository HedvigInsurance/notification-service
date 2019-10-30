package com.hedvig.notificationService.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import com.hedvig.notificationService.queue.MemberBCCAddress;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.hedvig.service")
@ComponentScan("com.hedvig.client")
@EnableFeignClients(basePackages = "com.hedvig.client")
public class NotificationServiceConfiguration {

  @Bean
  List<MemberBCCAddress> bccAddresses(@Value("${hedvig.notificationService.memberBcc}") String[] bcc){
    return Arrays.stream(bcc).
        map(MemberBCCAddress::new)
        .collect(Collectors.toList());
  }

  @Bean
  public Module kotlinModule() {
    return new KotlinModule();
  }
}
