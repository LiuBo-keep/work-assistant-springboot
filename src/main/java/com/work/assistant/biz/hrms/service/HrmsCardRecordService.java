package com.work.assistant.biz.hrms.service;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.work.assistant.biz.hrms.metadata.HrmsCardRecordPageRequest;
import com.work.assistant.biz.hrms.metadata.HrmsCardRecordPageResponse;
import com.work.assistant.biz.hrms.metadata.convert.ToHrmsCardRecordPageResponseConvert;
import com.work.assistant.biz.hrms.model.hrms.HrmsCardRecord;
import com.work.assistant.biz.hrms.model.hrms.QHrmsCardRecord;
import com.work.assistant.common.response.PageResponse;
import com.work.assistant.common.response.Pageable;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * hr打卡记录服务
 *
 * @author aidan.liu
 */
@Service
public class HrmsCardRecordService {

    private final JPAQueryFactory jpaQueryFactory;
    private final ToHrmsCardRecordPageResponseConvert toHrmsCardRecordPageResponseConvert;

    public HrmsCardRecordService(JPAQueryFactory jpaQueryFactory, ToHrmsCardRecordPageResponseConvert toHrmsCardRecordPageResponseConvert) {
        this.jpaQueryFactory = jpaQueryFactory;
        this.toHrmsCardRecordPageResponseConvert = toHrmsCardRecordPageResponseConvert;
    }

    public PageResponse<HrmsCardRecordPageResponse> page(Integer pageNumber, Integer pageSize, HrmsCardRecordPageRequest pageRequest) {
        QHrmsCardRecord qHrmsCardRecord = QHrmsCardRecord.hrmsCardRecord;
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (ObjectUtils.isNotEmpty(pageRequest.getClockInType())) {
            booleanBuilder.and(qHrmsCardRecord.clockInType.eq(pageRequest.getClockInType()));
        }
        if (ObjectUtils.isNotEmpty(pageRequest.getStartClockInTime())) {
            booleanBuilder.and(qHrmsCardRecord.clockInDate.goe(pageRequest.getStartClockInTime()));
        }
        if (ObjectUtils.isNotEmpty(pageRequest.getEndClockInTime())) {
            booleanBuilder.and(qHrmsCardRecord.clockInDate.loe(pageRequest.getEndClockInTime()));
        }

        Pageable pageable = Pageable.of(pageNumber, pageSize);
        List<HrmsCardRecordPageResponse> hrmsCardRecordPageResponseList = new ArrayList<>();
        Long totalCount = jpaQueryFactory.select(qHrmsCardRecord.count()).from(qHrmsCardRecord).where(booleanBuilder).fetchFirst();
        if (ObjectUtils.isNotEmpty(totalCount)) {
            List<HrmsCardRecord> hrmsCardRecords = jpaQueryFactory.selectFrom(qHrmsCardRecord).where(booleanBuilder).orderBy(qHrmsCardRecord.clockInTime.desc()).offset(pageable.getOffset())
                    .limit(pageable.getPageSize()).fetch();
            for (HrmsCardRecord hrmsCardRecord : hrmsCardRecords) {
                HrmsCardRecordPageResponse hrmsCardRecordPageResponse = toHrmsCardRecordPageResponseConvert.fromHrmsCardRecord(hrmsCardRecord);
                hrmsCardRecordPageResponseList.add(hrmsCardRecordPageResponse);
            }
        }
        return new PageResponse<>(pageable, totalCount, hrmsCardRecordPageResponseList);
    }
}
