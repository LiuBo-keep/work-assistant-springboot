package com.work.assistant.biz.hrms.model.hrms;

import com.work.assistant.common.hrms.NotifyChannel;
import com.work.assistant.config.database.AesEncryptorConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * hr通知配置
 *
 * @author aidan.liu
 */
@ToString
@Getter
@Setter
@Entity
@Table(name = "hrms_notify_config")
public class HrmsNotifyConfig {

  @Id
  @GeneratedValue(generator = "system-uuid")
  @Column(name = "id")
  private String id;

  /**
   * 是否开启通知
   */
  @Column(name = "enabled")
  private Boolean enabled;

  /**
   * 通知渠道
   */
  @Enumerated(EnumType.STRING)
  @Column(name = "notify_channel")
  private NotifyChannel notifyChannel;

  /**
   * 微信通知url
   */
  @Column(name = "push_plus_url")
  private String pushPlusUrl;

  /**
   * 微信通知token
   */
  @Convert(converter = AesEncryptorConverter.class)
  @Column(name = "push_plus_token")
  private String pushPlusToken;

  /**
   * 钉钉通知url
   */

  @Column(name = "ding_talk_url")
  private String dingTalkUrl;

  /**
   * hr系统url
   */
  @Column(name = "hrms_url")
  private String hrmsUrl;

  /**
   * hr系统token
   */
  @Convert(converter = AesEncryptorConverter.class)
  @Column(name = "hrms_token")
  private String hrmsToken;

  @Version
  @Column(name = "version_num")
  private Integer versionNum = 0;

}
