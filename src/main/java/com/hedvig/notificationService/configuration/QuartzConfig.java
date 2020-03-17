package com.hedvig.notificationService.configuration;

import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class QuartzConfig {

    @Bean
    SchedulerFactoryBeanCustomizer configure() {
        return (x) -> {
            //Don't overwrite jobs
            x.setOverwriteExistingJobs(false);
        };
    }
}
