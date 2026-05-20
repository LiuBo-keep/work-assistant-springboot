package com.work.assistant.scheduled.hrms;

import com.work.assistant.scheduled.hrms.job.HrmsCardRecordPullJob;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author aidan.liu
 */
@Slf4j
@Component
public class HrmsCardRecordPullTask {

  /**
   * 上午拉取开始时间
   *
   * <p>08:55 开始尝试拉取
   */
  private static final LocalTime MORNING_START = LocalTime.of(8, 50);

  /**
   * 上午拉取结束时间
   *
   * <p>超过 09:30 后不再执行上午拉取逻辑
   */
  private static final LocalTime MORNING_END = LocalTime.of(9, 30);

  /**
   * 下午拉取开始时间
   *
   * <p>17:30 开始尝试拉取
   */
  private static final LocalTime AFTERNOON_START = LocalTime.of(17, 30);

  /**
   * 下午拉取结束时间
   *
   * <p>超过 22:10 后不再执行下午拉取逻辑
   */
  private static final LocalTime AFTERNOON_END = LocalTime.of(22, 3);

  /**
   * 打卡记录拉取业务
   */
  private final HrmsCardRecordPullJob hrmsCardRecordPullJob;

  public HrmsCardRecordPullTask(HrmsCardRecordPullJob hrmsCardRecordPullJob) {
    this.hrmsCardRecordPullJob = hrmsCardRecordPullJob;
  }

  /**
   * HRMS 打卡记录定时任务
   *
   * <p>fixedDelay:
   * 当前任务执行完成后，
   * 延迟指定时间再次执行
   *
   * <p>timeUnit:
   * 时间单位为分钟
   *
   * <p>当前配置：
   * 每 5 分钟执行一次
   *
   * <p>设计思想：
   * 使用“高频轮询 + 业务幂等”代替大量 cron
   *
   * <p>优势：
   * 1. 避免维护大量 cron 表达式
   * 2. HRMS 数据延迟时可自动补偿
   * 3. 网络异常时具备自动重试能力
   * 4. 更适合分布式部署
   * 5. 更容易扩展
   *
   * <p>注意：
   * pullMorning/pullAfternoon 内部必须具备幂等控制
   * 否则高频执行会产生重复数据
   */
  @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
  public void execute() {

    LocalTime now = LocalTime.now();

    try {

      // 上午打卡拉取
      // 执行区间：08:55 ~ 09:30
      // 在此时间窗口内：每 5 分钟执行一次
      // 目的： 防止 HRMS 数据延迟
      if (between(now, MORNING_START, MORNING_END)) {
        log.info("execute morning pull");
        hrmsCardRecordPullJob.pullMorning();
      }

      // 下午打卡拉取
      // 执行区间：17:30 ~ 22:10
      // 说明：下班时间不固定，所以窗口设置较长
      if (between(now, AFTERNOON_START, AFTERNOON_END)) {
        log.info("execute afternoon pull");
        hrmsCardRecordPullJob.pullAfternoon();
      }

    } catch (Exception e) {

      log.error("hrms card record scheduled task error", e);
    }
  }

  /**
   * 判断当前时间是否在指定区间内
   *
   * <p>规则：
   * start <= now <= end
   *
   * <p>示例：
   * now=09:00
   * start=08:55
   * end=09:30
   *
   * <p>返回：
   * true
   *
   * <p>注意：
   * 包含边界时间
   */
  private boolean between(LocalTime now,
                          LocalTime start,
                          LocalTime end) {

    return !now.isBefore(start)
        && !now.isAfter(end);
  }
}
