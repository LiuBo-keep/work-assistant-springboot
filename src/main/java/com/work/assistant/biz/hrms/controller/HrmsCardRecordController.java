package com.work.assistant.biz.hrms.controller;

import com.work.assistant.biz.hrms.metadata.HrmsCardRecordPageRequest;
import com.work.assistant.biz.hrms.metadata.HrmsCardRecordPageResponse;
import com.work.assistant.biz.hrms.service.HrmsCardRecordService;
import com.work.assistant.common.response.PageResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * hr打卡记录控制器
 *
 * @author aidan.liu
 */
@RestController
@RequestMapping("/api/v1/hrms/card-record")
public class HrmsCardRecordController {

  public final HrmsCardRecordService hrmsCardRecordService;

  public HrmsCardRecordController(HrmsCardRecordService hrmsCardRecordService) {
    this.hrmsCardRecordService = hrmsCardRecordService;
  }

  @PostMapping("/page")
  public PageResponse<HrmsCardRecordPageResponse> page(
      @RequestParam("pageSize") Integer pageSize,
      @RequestParam("pageNumber") Integer pageNumber,
      @RequestBody HrmsCardRecordPageRequest pageRequest) {
    return hrmsCardRecordService.page(pageNumber, pageSize, pageRequest);
  }
}
