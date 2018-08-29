package com.hedvig.notificationService.queue;

import com.hedvig.notificationService.queue.requests.JobRequest;

public interface JobPoster {

  void startJob(JobRequest request, boolean delay);
}
