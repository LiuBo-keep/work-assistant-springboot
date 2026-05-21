package com.work.assistant.biz.hrms.metadata;

import com.work.assistant.common.hrms.CheckInType;
import lombok.Data;

@Data
public class HrmsCardRecordRefreshRequest {

  /**
   * 打卡类型
   */
  private CheckInType clockInType;
}
