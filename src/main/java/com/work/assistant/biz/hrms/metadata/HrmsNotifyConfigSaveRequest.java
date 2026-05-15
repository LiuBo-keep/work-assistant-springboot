package com.work.assistant.biz.hrms.metadata;

import com.work.assistant.common.hrms.NotifyChannel;
import lombok.Data;

/**
 * hr通知配置新增请求
 *
 * @author aidan.liu
 */
@Data
public class HrmsNotifyConfigSaveRequest {

  /**
   * 主键
   */
  private String id;

  /**
   * 是否启用
   */
  private Boolean enabled;

  /**
   * 通知渠道
   */
  private NotifyChannel notifyChannel;

  /**
   * push plus url
   */
  private String pushPlusUrl;

  /**
   * push plus token
   */
  private String pushPlusToken;

  /**
   * 钉钉通知url
   */
  private String dingTalkUrl;

  /**
   * hrms系统url
   */
  private String hrmsUrl;

  /**
   * hrms token
   */
  private String hrmsToken;
}
