package com.work.assistant.biz.hrms.manager;

import com.work.assistant.biz.hrms.model.hrms.HrmsCardRecord;
import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import com.work.assistant.biz.hrms.repository.HrmsCardRecordRepository;
import com.work.assistant.biz.hrms.repository.HrmsNotifyConfigRepository;
import com.work.assistant.common.hrms.CheckInType;
import com.work.assistant.common.utils.DateUtils;
import com.work.assistant.external.hrms.HrmsRestManager;
import com.work.assistant.external.hrms.metadata.HrmsCardRecordResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HrmsCardRecordPullManager {

  /**
   * 下班阙值时间
   */
  private static final LocalTime OFF_DUTY_TIME = LocalTime.of(17, 30);

  private final HrmsRestManager hrmsRestManager;
  private final HrmsCardRecordRepository hrmsCardRecordRepository;
  private final HrmsNotifyConfigRepository hrmsNotifyConfigRepository;

  public HrmsCardRecordPullManager(HrmsRestManager hrmsRestManager,
                                   HrmsCardRecordRepository hrmsCardRecordRepository,
                                   HrmsNotifyConfigRepository hrmsNotifyConfigRepository) {
    this.hrmsRestManager = hrmsRestManager;
    this.hrmsCardRecordRepository = hrmsCardRecordRepository;
    this.hrmsNotifyConfigRepository = hrmsNotifyConfigRepository;
  }

  /**
   * 上午拉取
   */
  public void pullMorning() {
    pull(CheckInType.CHECK_IN_AT_WORK);
  }

  /**
   * 下午拉取
   */
  public void pullAfternoon() {
    LocalTime now = LocalTime.now();
    if (now.isAfter(OFF_DUTY_TIME)) {
      pull(CheckInType.CHECK_OUT_AT_WORK);
    }
  }

  /**
   * 通用拉取逻辑
   */
  private void pull(CheckInType checkInType) {

    try {
      HrmsNotifyConfig config = getEnableConfig();
      if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
        log.warn("hrms notify config not enabled");
        return;
      }

      LocalDate today = LocalDate.now();

      // 幂等检查
      HrmsCardRecord existRecord =
          hrmsCardRecordRepository.findByClockInDateAndClockInType(today, checkInType);

      if (existRecord != null) {
        log.info("card record already exists, date={}, type={}", today, checkInType);
        return;
      }

      HrmsCardRecordResponse response =
          hrmsRestManager.queryCardRecord(
              config.getHrmsUrl(),
              config.getHrmsToken()
          );

      if (response == null) {
        log.warn("query hrms card record response is null");
        return;
      }

      HrmsCardRecord record = buildRecord(response, checkInType);

      hrmsCardRecordRepository.save(record);

      log.info("save hrms card record success, type={}, employeeId={}",
          checkInType,
          record.getEmployeeId());

    } catch (Exception e) {
      log.error("pull hrms card record error", e);
    }
  }

  /**
   * 获取启用配置
   */
  private HrmsNotifyConfig getEnableConfig() {

    List<HrmsNotifyConfig> configs = hrmsNotifyConfigRepository.findAll();
    if (CollectionUtils.isEmpty(configs)) {
      return null;
    }
    HrmsNotifyConfig config = configs.getFirst();
    if (Boolean.FALSE.equals(config.getEnabled())) {
      return null;
    }
    return config;
  }

  /**
   * DTO -> Entity
   */
  private HrmsCardRecord buildRecord(HrmsCardRecordResponse response,
                                     CheckInType checkInType) {

    HrmsCardRecord record = new HrmsCardRecord();

    record.setId(UUID.randomUUID().toString());
    record.setEmployeeId(response.getEmployeeId());
    record.setCnName(response.getCnName());
    record.setEnName(response.getEnName());
    record.setClockInType(checkInType);

    if (StringUtils.isNotBlank(response.getClockInTime())) {

      LocalDateTime clockInTime =
          DateUtils.stringToLocalDateTime(
              response.getClockInTime(),
              DateUtils.DATE_FORMAT_TIME_01
          );

      record.setClockInTime(clockInTime);

      if (clockInTime != null) {
        record.setClockInDate(clockInTime.toLocalDate());
      }
    }

    return record;
  }
}
