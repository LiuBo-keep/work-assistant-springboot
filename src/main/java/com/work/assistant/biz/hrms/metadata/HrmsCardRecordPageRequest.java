package com.work.assistant.biz.hrms.metadata;

import com.work.assistant.common.hrms.CheckInType;
import java.time.LocalDate;
import lombok.Data;

/**
 * hr打卡记录分页查询
 *
 * @author aidan.liu
 */
@Data
public class HrmsCardRecordPageRequest {

  /**
   * 打卡类型
   */
  private CheckInType clockInType;

  /**
   * 打卡开始日期(yyyy-MM-dd)
   */
  private LocalDate startClockInTime;

  /**
   * 打卡结束日期(yyyy-MM-dd)
   */
  private LocalDate endClockInTime;
}
