package com.work.assistant.scheduled.hrms.job;

import com.work.assistant.biz.hrms.manager.HrmsCardRecordPullManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class HrmsCardRecordPullJob {


  private final HrmsCardRecordPullManager hrmsCardRecordPullManager;


  public HrmsCardRecordPullJob(HrmsCardRecordPullManager hrmsCardRecordPullManager) {
    this.hrmsCardRecordPullManager = hrmsCardRecordPullManager;
  }

  public void pullMorning() {
    hrmsCardRecordPullManager.pullMorning();
  }

  public void pullAfternoon() {
    hrmsCardRecordPullManager.pullAfternoon();
  }
}