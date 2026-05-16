package com.work.assistant.biz.hrms.repository;

import com.work.assistant.biz.hrms.model.hrms.HrmsCardRecord;

import java.time.LocalDate;

import com.work.assistant.common.hrms.CheckInType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * hr打卡记录仓库
 *
 * @author aidan.liu
 */
@Repository
public interface HrmsCardRecordRepository extends JpaRepository<HrmsCardRecord, String> {


    HrmsCardRecord findByClockInDateAndClockInType(LocalDate date, CheckInType checkInType);
}
