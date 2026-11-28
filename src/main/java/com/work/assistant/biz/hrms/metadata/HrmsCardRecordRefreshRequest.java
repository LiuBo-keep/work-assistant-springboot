package com.work.assistant.biz.hrms.metadata;

import com.work.assistant.common.hrms.CheckInType;
import lombok.Data;

/**
 * 打卡记录刷新请求。
 *
 * @author work-assistant
 */
@Data
public class HrmsCardRecordRefreshRequest {

  /**
   * 打卡类型
   */
  private CheckInType clockInType;
}
