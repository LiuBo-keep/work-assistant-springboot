package com.work.assistant.scheduled.hrms;

import com.work.assistant.scheduled.hrms.job.HrmsCheckInNotificationJob;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * HRMS 打卡通知定时任务。
 *
 * <p>该任务主要用于：
 *
 * <ul>
 *   <li>在员工上班打卡时间段内，定时检测未完成上班打卡的员工并发送提醒通知。</li>
 *   <li>在员工下班打卡时间段内，定时检测未完成下班打卡的员工并发送提醒通知。</li>
 * </ul>
 *
 * <p>任务采用固定频率调度：
 *
 * <ul>
 *   <li>每 2 分钟执行一次。</li>
 *   <li>通过时间窗口控制不同时间段内执行不同通知逻辑。</li>
 * </ul>
 *
 * <p>时间窗口说明：
 *
 * <ul>
 *   <li>上午通知时间：08:50 ~ 09:28</li>
 *   <li>下午通知时间：17:30 ~ 22:03</li>
 * </ul>
 *
 * <p>设计目的：
 *
 * <ul>
 *   <li>避免频繁通知造成消息骚扰。</li>
 *   <li>避免在非业务时间段执行无意义任务。</li>
 *   <li>将具体业务逻辑下沉至 Job 层，Task 仅负责调度。</li>
 * </ul>
 *
 * @author aidan.liu
 */

@Slf4j
@Component
public class HrmsCheckInNotificationTask {

  /**
   * 上午打卡通知开始时间。
   *
   * <p>当系统时间达到该时间后，
   * 开始执行上午上班打卡提醒逻辑。
   *
   * <p>例如：
   *
   * <ul>
   *   <li>08:50 开始检测未打卡员工。</li>
   *   <li>后续每 2 分钟继续执行一次通知检测。</li>
   * </ul>
   */
  private static final LocalTime MORNING_START = LocalTime.of(8, 50);

  /**
   * 上午打卡通知结束时间。
   *
   * <p>超过该时间后：
   *
   * <ul>
   *   <li>不再继续执行上午打卡提醒。</li>
   *   <li>避免对迟到较久员工持续发送通知。</li>
   * </ul>
   */
  private static final LocalTime MORNING_END = LocalTime.of(9, 28);

  /**
   * 下午打卡通知开始时间。
   *
   * <p>从该时间开始：
   *
   * <ul>
   *   <li>开始检测未完成下班打卡员工。</li>
   *   <li>执行下班打卡提醒通知。</li>
   * </ul>
   */
  private static final LocalTime AFTERNOON_START = LocalTime.of(17, 30);

  /**
   * 下午打卡通知结束时间。
   *
   * <p>超过该时间后：
   *
   * <ul>
   *   <li>停止执行下班打卡提醒。</li>
   *   <li>避免夜间持续执行无效通知。</li>
   * </ul>
   */
  private static final LocalTime AFTERNOON_END = LocalTime.of(22, 3);

  /**
   * 打卡通知记录拉取业务
   */
  private final HrmsCheckInNotificationJob hrmsCheckInNotificationJob;

  public HrmsCheckInNotificationTask(HrmsCheckInNotificationJob hrmsCheckInNotificationJob) {
    this.hrmsCheckInNotificationJob = hrmsCheckInNotificationJob;
  }


  /**
   * 定时执行入口。
   *
   * <p>调度规则：
   *
   * <ul>
   *   <li>固定延迟 2 分钟执行一次。</li>
   *   <li>上一轮任务结束后开始计算下一轮延迟时间。</li>
   * </ul>
   *
   * <p>执行逻辑：
   *
   * <ol>
   *   <li>获取当前系统时间。</li>
   *   <li>判断是否处于上午通知时间窗口。</li>
   *   <li>判断是否处于下午通知时间窗口。</li>
   *   <li>执行对应通知业务。</li>
   * </ol>
   *
   * <p>异常处理：
   *
   * <ul>
   *   <li>捕获所有异常避免定时任务线程中断。</li>
   *   <li>记录完整错误日志便于后续排查。</li>
   * </ul>
   */
  @Scheduled(cron = "0 */5 * * * MON-FRI")
  public void execute() {

    LocalTime now = LocalTime.now();

    try {

      // 上午打卡通知逻辑。
      if (between(now, MORNING_START, MORNING_END)) {
        hrmsCheckInNotificationJob.morningCheckInNotification();
      }

      // 下午打卡通知逻辑。
      if (between(now, AFTERNOON_START, AFTERNOON_END)) {
        hrmsCheckInNotificationJob.afternoonCheckInNotification();
      }

    } catch (Exception e) {
      log.error("hrms check in notification scheduled task error", e);
    }
  }

  private boolean between(LocalTime now,
                          LocalTime start,
                          LocalTime end) {

    return !now.isBefore(start)
        && !now.isAfter(end);
  }
}
