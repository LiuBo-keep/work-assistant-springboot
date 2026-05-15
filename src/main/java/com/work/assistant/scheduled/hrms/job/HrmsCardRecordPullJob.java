package com.work.assistant.scheduled.hrms.job;

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
import java.util.List;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Component
public class HrmsCardRecordPullJob {


  private final HrmsRestManager hrmsRestManager;
  private final HrmsCardRecordRepository hrmsCardRecordRepository;
  private final HrmsNotifyConfigRepository hrmsNotifyConfigRepository;


  public HrmsCardRecordPullJob(HrmsRestManager hrmsRestManager, HrmsCardRecordRepository hrmsCardRecordRepository,
                               HrmsNotifyConfigRepository hrmsNotifyConfigRepository) {
    this.hrmsRestManager = hrmsRestManager;
    this.hrmsCardRecordRepository = hrmsCardRecordRepository;
    this.hrmsNotifyConfigRepository = hrmsNotifyConfigRepository;
  }

  public void pullMorning() {
    try {
      List<HrmsNotifyConfig> hrmsNotifyConfigs = hrmsNotifyConfigRepository.findAll();
      if (CollectionUtils.isNotEmpty(hrmsNotifyConfigs) && hrmsNotifyConfigs.getFirst().getEnabled()) {
        HrmsNotifyConfig hrmsNotifyConfig = hrmsNotifyConfigs.getFirst();
        HrmsCardRecordResponse hrmsCardRecordResponse = hrmsRestManager.queryCardRecord(hrmsNotifyConfig.getHrmsUrl(), hrmsNotifyConfig.getHrmsToken());
        if (hrmsCardRecordResponse != null) {
          HrmsCardRecord hrmsCardRecord = new HrmsCardRecord();
          hrmsCardRecord.setId(UUID.randomUUID().toString());
          hrmsCardRecord.setEmployeeId(hrmsCardRecordResponse.getEmployeeId());
          hrmsCardRecord.setCnName(hrmsCardRecordResponse.getCnName());
          hrmsCardRecord.setEnName(hrmsCardRecordResponse.getEnName());
          if (StringUtils.isNotBlank(hrmsCardRecordResponse.getClockInTime())) {
            LocalDateTime localDateTime = DateUtils.stringToLocalDateTime(hrmsCardRecordResponse.getClockInTime(), DateUtils.DATE_FORMAT_TIME_01);
            hrmsCardRecord.setClockInTime(localDateTime);
            if (ObjectUtils.isNotEmpty(localDateTime)) {
              hrmsCardRecord.setClockInDate(localDateTime.toLocalDate());
            }
          }

          LocalDate now = LocalDate.now();
          HrmsCardRecord byClockInDate = hrmsCardRecordRepository.findByClockInDate(now);
          if (byClockInDate != null) {
            hrmsCardRecord.setClockInType(CheckInType.CHECK_OUT_AT_WORK);
          } else {
            hrmsCardRecord.setClockInType(CheckInType.CHECK_IN_AT_WORK);
          }
          hrmsCardRecordRepository.save(hrmsCardRecord);
        }
      }
    } catch (Exception e) {

    }
  }

  public void pullAfternoon() {

  }
}
