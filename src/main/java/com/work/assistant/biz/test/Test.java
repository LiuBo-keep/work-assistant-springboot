package com.work.assistant.biz.test;

import com.work.assistant.scheduled.hrms.job.HrmsCardRecordPullJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author aidan.liu
 */
@RestController
public class Test {

  @Autowired
  HrmsCardRecordPullJob hrmsCardRecordPullJob;


  @GetMapping("/test")
  public void Test() {
    hrmsCardRecordPullJob.pullMorning();
  }
}
