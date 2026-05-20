package com.work.assistant.scheduled.hrms.job;

import com.work.assistant.biz.hrms.model.hrms.HrmsCardRecord;
import com.work.assistant.biz.hrms.model.hrms.HrmsNotifyConfig;
import com.work.assistant.biz.hrms.repository.HrmsCardRecordRepository;
import com.work.assistant.biz.hrms.repository.HrmsNotifyConfigRepository;
import com.work.assistant.common.hrms.CheckInType;
import com.work.assistant.external.notify.enums.NotifyType;
import com.work.assistant.external.notify.manager.NotifyManager;
import com.work.assistant.external.notify.model.NotifyRequest;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class HrmsCheckInNotificationJob {

  /**
   * 通知管理器
   */
  private final NotifyManager notifyManager;

  /**
   * 打卡记录Repository
   */
  private final HrmsCardRecordRepository hrmsCardRecordRepository;
  private final HrmsNotifyConfigRepository hrmsNotifyConfigRepository;

  public HrmsCheckInNotificationJob(
      NotifyManager notifyManager,
      HrmsCardRecordRepository hrmsCardRecordRepository, HrmsNotifyConfigRepository hrmsNotifyConfigRepository) {

    this.notifyManager = notifyManager;
    this.hrmsCardRecordRepository = hrmsCardRecordRepository;
    this.hrmsNotifyConfigRepository = hrmsNotifyConfigRepository;
  }

  /**
   * 上班打卡提醒
   */
  public void morningCheckInNotification() {

    notifyIfNotCheckIn(
        CheckInType.CHECK_IN_AT_WORK,
        "上班打卡提醒"
    );
  }

  /**
   * 下班打卡提醒
   */
  public void afternoonCheckInNotification() {

    notifyIfNotCheckIn(
        CheckInType.CHECK_OUT_AT_WORK,
        "下班打卡提醒"
    );
  }

  /**
   * 未打卡通知
   * <p>
   * 逻辑：
   * 1. 查询今日是否已打卡
   * 2. 未打卡则发送通知
   */
  private void notifyIfNotCheckIn(CheckInType checkInType,
                                  String title) {

    try {

      HrmsNotifyConfig config = getEnableConfig();
      if (config == null || Boolean.FALSE.equals(config.getEnabled())) {
        log.warn("hrms notify config not enabled");
        return;
      }

      LocalDate today = LocalDate.now();

      // 查询当天打卡记录
      HrmsCardRecord hrmsCardRecord = hrmsCardRecordRepository.findByClockInDateAndClockInType(today, checkInType);

      // 已打卡
      if (hrmsCardRecord != null) {
        log.info("already checked in, date={}, type={}", today, checkInType);
        return;
      }

      // 构建通知请求
      NotifyRequest notifyRequest = buildNotifyRequest(checkInType, title);

      // 执行通知
      notifyManager.notify(notifyRequest);

      log.info("check in notification success, type={}", checkInType);
    } catch (Exception e) {
      log.error("check in notification error, type={}", checkInType, e);
    }
  }

  /**
   * 构建通知请求
   */
  private NotifyRequest buildNotifyRequest(
      CheckInType checkInType,
      String title) {

    NotifyRequest notifyRequest = new NotifyRequest();

    /**
     * 后续可扩展：
     *      企业微信
     *      PushPlus
     *      Telegram
     */
    notifyRequest.setNotifyType(
        NotifyType.DING_TALK
    );

    notifyRequest.setTitle(title);

    notifyRequest.setContent(
        buildMarkdownContent(checkInType)
    );

    return notifyRequest;
  }

  /**
   * 构建 Markdown 通知内容
   * <p>
   * 钉钉推荐使用 markdown
   */
  private String buildMarkdownContent(
      CheckInType checkInType) {

    StringBuilder builder = new StringBuilder();

    builder.append("## HRMS 打卡提醒")
        .append("\n\n");

    builder.append("> 日期：")
        .append(LocalDate.now())
        .append("\n\n");

    if (CheckInType.CHECK_IN_AT_WORK.equals(checkInType)) {

      builder.append("### 上班打卡提醒")
          .append("\n\n")
          .append("- 还未检测到上班打卡")
          .append("\n")
          .append("- 请及时完成打卡")
          .append("\n")
          .append("- 避免影响考勤")
          .append("\n");

    } else {
      builder.append("### 下班打卡提醒")
          .append("\n\n")
          .append("- 还未检测到下班打卡")
          .append("\n")
          .append("- 下班前请记得打卡")
          .append("\n")
          .append("- 避免遗漏考勤")
          .append("\n");
    }

    return builder.toString();
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
}