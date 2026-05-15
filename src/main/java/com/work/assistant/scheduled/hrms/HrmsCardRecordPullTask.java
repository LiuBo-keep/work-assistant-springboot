package com.work.assistant.scheduled.hrms;

import com.work.assistant.scheduled.hrms.job.HrmsCardRecordPullJob;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Component
public class HrmsCardRecordPullTask {

  private final HrmsCardRecordPullJob hrmsCardRecordPullJob;

  public HrmsCardRecordPullTask(HrmsCardRecordPullJob hrmsCardRecordPullJob) {
    this.hrmsCardRecordPullJob = hrmsCardRecordPullJob;
  }

  @Scheduled(cron = "0 55 8 * * ?")
  @Scheduled(cron = "0 57 8 * * ?")
  @Scheduled(cron = "0 59 8 * * ?")
  @Scheduled(cron = "0 0 9 * * ?")
  @Scheduled(cron = "0 10 9 * * ?")
  @Scheduled(cron = "0 20 9 * * ?")
  @Scheduled(cron = "0 28 9 * * ?")
  public void morningExecute() {
    hrmsCardRecordPullJob.pullMorning();
  }

  @Scheduled(cron = "0 31 17 * * ?")
  @Scheduled(cron = "0 33 17 * * ?")
  @Scheduled(cron = "0 40 17 * * ?")
  @Scheduled(cron = "0 0 22 * * ?")
  @Scheduled(cron = "0 01 22 * * ?")
  @Scheduled(cron = "0 05 22 * * ?")
  @Scheduled(cron = "0 07 22 * * ?")
  public void afternoonExecute() {
    hrmsCardRecordPullJob.pullAfternoon();
  }
}
