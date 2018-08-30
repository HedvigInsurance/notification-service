package com.hedvig.notificationService.configuration;

import com.hedvig.notificationService.jobs.ActivationEmailJob;
import java.util.Objects;
import java.util.TimeZone;
import lombok.val;
import org.quartz.CronTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;

@Configuration
class QuartzConfig {

  private static final Logger log = LoggerFactory.getLogger(QuartzConfig.class);

  @Bean(name="activationEmailJob")
  JobDetailFactoryBean activationEmailJob() {
    JobDetailFactoryBean jobDetailFactoryBean = new JobDetailFactoryBean();
    jobDetailFactoryBean.setJobClass(ActivationEmailJob.class);
    jobDetailFactoryBean.setDurability(true);
    return jobDetailFactoryBean;
  }


  @Bean
  CronTriggerFactoryBean echoTrigger() {
    log.info("Configuring trigger");

    val factoryBean = new CronTriggerFactoryBean();

    factoryBean.setName("activationEmailTrigger");
    factoryBean.setCronExpression("0 0 8 ? * * *");
    factoryBean.setJobDetail(Objects.requireNonNull(activationEmailJob().getObject()));
    factoryBean.setGroup("automation");
    factoryBean.setMisfireInstruction(CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING);
    factoryBean.setTimeZone(TimeZone.getTimeZone("Europe/Stockholm"));
    return factoryBean;

  }

  @Bean
  SchedulerFactoryBeanCustomizer configure() {
    return (x) -> {
      //Don't overwrite jobs
      x.setOverwriteExistingJobs(false);
    };
  }
}
