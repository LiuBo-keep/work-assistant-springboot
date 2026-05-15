package com.work.assistant.biz.hrms.metadata.convert;

import com.work.assistant.biz.hrms.metadata.HrmsCardRecordPageResponse;
import com.work.assistant.biz.hrms.model.hrms.HrmsCardRecord;
import com.work.assistant.common.utils.DateUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Component
public class ToHrmsCardRecordPageResponseConvert {

  public HrmsCardRecordPageResponse fromHrmsCardRecord(HrmsCardRecord hrmsCardRecord) {
    HrmsCardRecordPageResponse pageResponse = new HrmsCardRecordPageResponse();
    pageResponse.setId(hrmsCardRecord.getId());
    pageResponse.setEmployeeId(hrmsCardRecord.getEmployeeId());
    pageResponse.setCnName(hrmsCardRecord.getCnName());
    pageResponse.setEnName(hrmsCardRecord.getEnName());
    pageResponse.setClockInType(hrmsCardRecord.getClockInType());
    if (ObjectUtils.isNotEmpty(hrmsCardRecord.getClockInTime())) {
      String clockInTime = DateUtils.localDateTimeToString(hrmsCardRecord.getClockInTime(), DateUtils.DATE_FORMAT_TIME_01);
      pageResponse.setClockInTime(clockInTime);
    }
    return pageResponse;
  }
}
