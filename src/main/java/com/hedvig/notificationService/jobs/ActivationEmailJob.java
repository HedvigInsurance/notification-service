package com.hedvig.notificationService.jobs;

import com.hedvig.notificationService.service.NotificationService;
import java.time.LocalDateTime;
import lombok.Setter;
import lombok.val;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class ActivationEmailJob extends QuartzJobBean {

  private Logger log = LoggerFactory.getLogger(ActivationEmailJob.class);


  @Autowired
  @Setter
  public NotificationService notificationService;


  @Override
  protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
    try {
      log.info("Executing ActivationEmailJob scheduledtime: {}, localDate: {}",
          context.getScheduledFireTime().toString(),
          LocalDateTime.now());

      val membersSentTo = notificationService.sendActivationEmails(0);

      log.info("Sent email to the following memberId: ", String.join(",", membersSentTo));

    } catch (Exception e) {
      throw new JobExecutionException("Exception in job execution", e, true);
    }
  }
}
