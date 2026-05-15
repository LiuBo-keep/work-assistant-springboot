package com.work.assistant.external.hrms.metadata;

import lombok.Data;

/**
 * @author aidan.liu
 */
@Data
public class HrmsCardRecordResponse {

  private String employeeId;

  private String cnName;

  private String enName;

  private String clockInTime;

  private String clockInType;

  private String attendanceLocation;
}
