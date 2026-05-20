package com.work.assistant.biz.hrms.metadata;

import com.work.assistant.common.hrms.CheckInType;
import lombok.Data;

/**
 * hr打卡记录分页响应
 *
 * @author aidan.liu
 */
@Data
public class HrmsCardRecordPageResponse {

  /**
   * 主键
   */
  private String id;

  /**
   * 员工编号
   */
  private String employeeId;

  /**
   * 中文名称
   */
  private String cnName;

  /**
   * 英文名称
   */
  private String enName;

  /**
   * 打卡类型
   */
  private CheckInType clockInType;

  /**
   * 打卡时间(yyyy-MM-dd hh:mm:ss)
   */
  private String clockInTime;
}
