package com.work.assistant.biz.hrms.controller;

import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigQueryResponse;
import com.work.assistant.biz.hrms.metadata.HrmsNotifyConfigSaveRequest;
import com.work.assistant.biz.hrms.service.HrmsNotifyConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * hr通知配置控制器
 *
 * @author aidan.liu
 */
@RestController
@RequestMapping("/api/v1/hrms/notify-config")
public class HrmsNotifyConfigController {

  private final HrmsNotifyConfigService hrmsNotifyConfigService;

  public HrmsNotifyConfigController(HrmsNotifyConfigService hrmsNotifyConfigService) {
    this.hrmsNotifyConfigService = hrmsNotifyConfigService;
  }

  @PostMapping("/save")
  public void save(@RequestBody HrmsNotifyConfigSaveRequest request) {
    hrmsNotifyConfigService.save(request);
  }

  @PostMapping("/query")
  public HrmsNotifyConfigQueryResponse query() {
    return hrmsNotifyConfigService.query();
  }
}
