package com.work.assistant.biz.hrms.model.hrms;

import com.work.assistant.common.hrms.CheckInType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 打卡记录
 *
 * @author aidan.liu
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "hrms_card_record")
public class HrmsCardRecord {

  /**
   * 主键
   */
  @Id
  @Column(name = "id")
  private String id;

  /**
   * 员工编号
   */
  @Column(name = "employee_id")
  private String employeeId;

  /**
   * 中文名称
   */
  @Column(name = "cn_name")
  private String cnName;

  /**
   * 英文名称
   */
  @Column(name = "en_name")
  private String enName;

  /**
   * 打卡类型
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "clock_in_type")
  private CheckInType clockInType;

  /**
   * 打卡日期(yyyy-MM-dd)
   */
  @Column(name = "clock_in_date")
  private LocalDate clockInDate;

  /**
   * 打卡时间(yyyy-MM-dd hh:mm:ss)
   */
  @Column(name = "clock_in_time")
  private LocalDateTime clockInTime;

  @Version
  @Column(name = "version_num")
  private Integer versionNum = 0;
}
