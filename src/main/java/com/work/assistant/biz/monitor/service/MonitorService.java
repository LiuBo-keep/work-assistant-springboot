package com.work.assistant.biz.monitor.service;

import com.work.assistant.biz.monitor.model.ApiCallLog;
import com.work.assistant.biz.monitor.model.MetricSnapshot;
import com.work.assistant.biz.monitor.model.TaskRunLog;
import com.work.assistant.biz.monitor.repository.ApiCallLogRepository;
import com.work.assistant.biz.monitor.repository.MetricSnapshotRepository;
import com.work.assistant.biz.monitor.repository.TaskRunLogRepository;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.GarbageCollectorMXBean;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 系统监控服务。
 *
 * <p>通过 JDK 自带的 {@code ManagementFactory} MXBean 采集 JVM 与系统运行指标，
 * 无需额外依赖即可提供 CPU、内存、GC、线程、类加载等关键信息。
 *
 * @author work-assistant
 */
@Slf4j
@Service
public class MonitorService {

  private final MetricSnapshotRepository metricSnapshotRepository;
  private final TaskRunLogRepository taskRunLogRepository;
  private final ApiCallLogRepository apiCallLogRepository;
  private final DataSource dataSource;

  public MonitorService(MetricSnapshotRepository metricSnapshotRepository,
                        TaskRunLogRepository taskRunLogRepository,
                        ApiCallLogRepository apiCallLogRepository,
                        DataSource dataSource) {
    this.metricSnapshotRepository = metricSnapshotRepository;
    this.taskRunLogRepository = taskRunLogRepository;
    this.apiCallLogRepository = apiCallLogRepository;
    this.dataSource = dataSource;
  }

  /**
   * 聚合系统概览指标。
   */
  public Map<String, Object> overview() {
    MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    Runtime runtime = Runtime.getRuntime();

    MemoryUsage heap = memoryBean.getHeapMemoryUsage();
    MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();

    long heapUsed = heap.getUsed();
    long heapMax = heap.getMax();
    long nonHeapUsed = nonHeap.getUsed();
    long totalMem = runtime.totalMemory();
    long freeMem = runtime.freeMemory();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("jvmName", ManagementFactory.getRuntimeMXBean().getVmName());
    result.put("jvmVersion", System.getProperty("java.version"));
    result.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
    result.put("uptimeMs", ManagementFactory.getRuntimeMXBean().getUptime());

    // CPU 与操作系统信息
    result.put("availableProcessors", osBean.getAvailableProcessors());
    result.put("systemLoadAverage", osBean.getSystemLoadAverage());

    Map<String, Object> os = new LinkedHashMap<>();
    os.put("name", osBean.getName());
    os.put("version", osBean.getVersion());
    os.put("arch", osBean.getArch());
    os.put("systemLoadAverage", osBean.getSystemLoadAverage());

    // 扩展型 MXBean 提供物理内存、虚拟内存与 CPU 负载等更丰富的系统指标
    if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
      os.put("totalPhysicalMemory", sunOsBean.getTotalMemorySize());
      os.put("freePhysicalMemory", sunOsBean.getFreeMemorySize());
      os.put("committedVirtualMemory", sunOsBean.getCommittedVirtualMemorySize());
      os.put("systemCpuLoadPercent", round(sunOsBean.getCpuLoad() * 100));
      os.put("processCpuLoadPercent", round(sunOsBean.getProcessCpuLoad() * 100));
    }
    result.put("os", os);

    // 内存
    Map<String, Object> memory = new LinkedHashMap<>();
    memory.put("heapUsed", heapUsed);
    memory.put("heapMax", heapMax);
    memory.put("heapCommitted", heap.getCommitted());
    memory.put("heapUsagePercent", heapMax > 0 ? round(heapUsed * 100.0 / heapMax) : 0.0);
    memory.put("nonHeapUsed", nonHeapUsed);
    memory.put("nonHeapCommitted", nonHeap.getCommitted());
    memory.put("totalUsed", totalMem - freeMem);
    memory.put("totalAllocated", totalMem);
    result.put("memory", memory);

    // 线程
    result.put("threadCount", threadBean.getThreadCount());
    result.put("peakThreadCount", threadBean.getPeakThreadCount());
    result.put("daemonThreadCount", threadBean.getDaemonThreadCount());

    // 类加载
    result.put("loadedClassCount", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount());
    result.put("totalLoadedClassCount", ManagementFactory.getClassLoadingMXBean().getTotalLoadedClassCount());

    // GC
    List<Map<String, Object>> gcList = new ArrayList<>();
    for (GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
      Map<String, Object> gc = new LinkedHashMap<>();
      gc.put("name", gcBean.getName());
      gc.put("count", gcBean.getCollectionCount());
      gc.put("timeMs", gcBean.getCollectionTime());
      gcList.add(gc);
    }
    result.put("gc", gcList);

    return result;
  }

  /**
   * 查询指定指标的历史快照（最近 N 小时）。
   */
  public List<Map<String, Object>> metricHistory(String metricName, int hours) {
    LocalDateTime to = LocalDateTime.now();
    LocalDateTime from = to.minusHours(hours);
    List<MetricSnapshot> snapshots =
        metricSnapshotRepository.findByMetricNameAndCapturedAtBetweenOrderByCapturedAtAsc(
            metricName, from, to);

    List<Map<String, Object>> result = new ArrayList<>();
    for (MetricSnapshot s : snapshots) {
      Map<String, Object> point = new HashMap<>();
      point.put("value", s.getMetricValue());
      point.put("time", s.getCapturedAt().toString());
      result.add(point);
    }
    return result;
  }

  /**
   * 采集线程监控明细：状态分布统计 + 存活线程列表。
   *
   * <p>线程列表按 CPU 占用时间降序返回（最耗 CPU 的线程靠前），
   * 便于快速定位异常线程或死锁候选。
   */
  public Map<String, Object> threads() {
    ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    // 状态分布统计
    Map<String, Long> stateCount = new LinkedHashMap<>();
    for (Thread.State s : Thread.State.values()) {
      stateCount.put(s.name(), 0L);
    }

    List<Map<String, Object>> threadList = new ArrayList<>();
    ThreadInfo[] threadInfos = threadBean.getThreadInfo(threadBean.getAllThreadIds(), 0);
    for (ThreadInfo info : threadInfos) {
      if (info == null) {
        continue;
      }
      Thread.State state = info.getThreadState();
      stateCount.merge(state.name(), 1L, Long::sum);

      Map<String, Object> t = new LinkedHashMap<>();
      t.put("id", info.getThreadId());
      t.put("name", info.getThreadName());
      t.put("state", state.name());
      t.put("cpuTimeMs", threadBean.getThreadCpuTime(info.getThreadId()) / 1_000_000);
      t.put("blockedCount", info.getBlockedCount());
      t.put("waitedCount", info.getWaitedCount());
      t.put("daemon", info.isDaemon());
      t.put("priority", info.getPriority());
      threadList.add(t);
    }

    // 按 CPU 耗时降序
    threadList.sort((a, b) ->
        Long.compare((long) b.get("cpuTimeMs"), (long) a.get("cpuTimeMs")));

    // 死锁检测
    long[] deadlocked = threadBean.findDeadlockedThreads();

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("total", threadList.size());
    result.put("stateDistribution", stateCount);
    result.put("deadlockedCount", deadlocked == null ? 0 : deadlocked.length);
    result.put("threads", threadList);
    return result;
  }

  /**
   * 采集数据库连接池指标。
   *
   * <p>Spring Boot 默认连接池为 HikariCP，通过 {@link HikariPoolMXBean} 读取连接池
   * 实时状态（活跃/空闲/等待连接数、连接创建/超时统计等），并执行一次轻量 SQL 探测
   * 数据库连通性与响应耗时。
   */
  public Map<String, Object> database() {
    Map<String, Object> result = new LinkedHashMap<>();

    boolean isHikari = dataSource instanceof HikariDataSource;
    result.put("poolType", isHikari ? "HikariCP" : dataSource.getClass().getSimpleName());

    if (isHikari) {
      HikariDataSource ds = (HikariDataSource) dataSource;
      HikariPoolMXBean pool = ds.getHikariPoolMXBean();
      int active = pool.getActiveConnections();
      int idle = pool.getIdleConnections();

      result.put("activeConnections", active);
      result.put("idleConnections", idle);
      result.put("totalConnections", pool.getTotalConnections());
      result.put("threadsAwaitingConnection", pool.getThreadsAwaitingConnection());
      result.put("maximumPoolSize", ds.getMaximumPoolSize());
      result.put("minimumIdle", ds.getMinimumIdle());
      result.put("connectionTimeoutMs", ds.getConnectionTimeout());
      result.put("idleTimeoutMs", ds.getIdleTimeout());

      // ---- 连接泄漏检测相关 ----
      long leakThreshold = ds.getLeakDetectionThreshold();
      result.put("leakDetectionThresholdMs", leakThreshold);
      result.put("leakDetectionEnabled", leakThreshold > 0);

      // ---- 自动续连相关（连接换新 / 空闲保活） ----
      long maxLifetime = ds.getMaxLifetime();
      long keepaliveTime = ds.getKeepaliveTime();
      result.put("maxLifetimeMs", maxLifetime);
      result.put("keepaliveTimeMs", keepaliveTime);
      result.put("autoRenewalEnabled", maxLifetime > 0);

      // 连接占用率（活跃 / 最大），>80% 提示连接紧张（潜在泄漏/耗尽风险）
      int max = ds.getMaximumPoolSize();
      result.put("usagePercent", max > 0 ? Math.round(active * 100.0 / max) : 0);
    }

    // 连通性探测：执行一次轻量 SQL 并计时
    long start = System.currentTimeMillis();
    boolean healthy = false;
    String errorMsg = null;
    try (Connection conn = dataSource.getConnection();
         java.sql.Statement stmt = conn.createStatement();
         java.sql.ResultSet rs = stmt.executeQuery("SELECT 1")) {
      if (rs.next()) {
        healthy = true;
      }
    } catch (SQLException e) {
      healthy = false;
      errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
      log.warn("database connectivity check failed", e);
    }
    long pingMs = System.currentTimeMillis() - start;

    result.put("healthy", healthy);
    result.put("pingMs", pingMs);
    result.put("errorMsg", errorMsg);
    return result;
  }

  /**
   * 查询定时任务执行记录。
   */
  public List<TaskRunLog> taskRunLogs() {
    return taskRunLogRepository.findTop50ByOrderByStartTimeDesc();
  }

  /**
   * 采样当前 JVM 内存指标并落库。
   *
   * <p>由定时任务调用，间隔采样用于绘制历史趋势曲线。
   */
  public void sampleMetrics() {
    try {
      MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
      MemoryUsage heap = memoryBean.getHeapMemoryUsage();
      MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
      ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
      OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
      LocalDateTime now = LocalDateTime.now();

      saveSnapshot("heap.used", heap.getUsed(), now);
      saveSnapshot("heap.max", heap.getMax(), now);
      saveSnapshot("nonheap.used", nonHeap.getUsed(), now);
      saveSnapshot("thread.count", threadBean.getThreadCount(), now);

      // CPU 负载（进程级），部分环境可能返回 -1 表示不可用，跳过
      if (osBean instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {
        double cpu = sunOsBean.getProcessCpuLoad();
        if (cpu >= 0) {
          saveSnapshot("process.cpu", round(cpu * 100), now);
        }
      }

      // 数据库活跃连接数（用于连接池趋势/泄漏监控）
      if (dataSource instanceof HikariDataSource ds) {
        HikariPoolMXBean pool = ds.getHikariPoolMXBean();
        if (pool != null) {
          saveSnapshot("db.active", pool.getActiveConnections(), now);
        }
      }
    } catch (Exception e) {
      log.error("sample system metrics error", e);
    }
  }

  private void saveSnapshot(String name, double value, LocalDateTime time) {
    MetricSnapshot snapshot = new MetricSnapshot();
    snapshot.setMetricName(name);
    snapshot.setMetricValue(value);
    snapshot.setCapturedAt(time);
    metricSnapshotRepository.save(snapshot);
  }

  /**
   * 聚合接口调用统计（近 N 小时）。
   *
   * <p>返回总调用次数、成功率、平均耗时、慢接口 TOP（按调用次数）、状态码分布。
   */
  public Map<String, Object> apiStats(int hours) {
    LocalDateTime from = LocalDateTime.now().minusHours(hours);
    LocalDateTime to = LocalDateTime.now();

    long totalCount = apiCallLogRepository.countByCreatedAtBetween(from, to);
    // 成功 = 状态码 2xx
    long successCount = apiCallLogRepository
        .countByCreatedAtBetweenAndStatusCodeBetween(from, to, 200, 299);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("totalCount", totalCount);
    result.put("successCount", successCount);
    result.put("failCount", totalCount - successCount);
    result.put("successRate", totalCount > 0 ? round(successCount * 100.0 / totalCount) : 100.0);

    // 各接口聚合：路径、次数、平均耗时、最大耗时
    List<Map<String, Object>> topApis = new ArrayList<>();
    for (Object[] row : apiCallLogRepository.aggregateByPath(from)) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("path", row[0]);
      item.put("count", row[1]);
      item.put("avgDurationMs", row[2] == null ? 0 : toLong(row[2]));
      item.put("maxDurationMs", row[3] == null ? 0 : toLong(row[3]));
      topApis.add(item);
    }
    result.put("topApis", topApis);

    // 状态码分布
    List<Map<String, Object>> statusDist = new ArrayList<>();
    for (Object[] row : apiCallLogRepository.aggregateByStatus(from)) {
      Map<String, Object> item = new LinkedHashMap<>();
      item.put("statusCode", row[0]);
      item.put("count", row[1]);
      statusDist.add(item);
    }
    result.put("statusDistribution", statusDist);

    return result;
  }

  /**
   * 查询最近接口调用明细（最近 100 条）。
   */
  public List<ApiCallLog> apiLogs() {
    return apiCallLogRepository.findTop100ByOrderByCreatedAtDesc();
  }

  /**
   * 记录一次定时任务执行结果。
   */
  public void recordTaskRun(String taskName, LocalDateTime startTime,
                            LocalDateTime endTime, long durationMs,
                            boolean success, String errorMsg) {
    try {
      TaskRunLog logEntry = new TaskRunLog();
      logEntry.setTaskName(taskName);
      logEntry.setStartTime(startTime);
      logEntry.setEndTime(endTime);
      logEntry.setDurationMs(durationMs);
      logEntry.setStatus(success ? "SUCCESS" : "FAILED");
      logEntry.setErrorMsg(errorMsg);
      logEntry.setCreatedAt(LocalDateTime.now());
      taskRunLogRepository.save(logEntry);
    } catch (Exception e) {
      // 记录日志本身失败不应影响主流程
      log.error("record task run log error", e);
    }
  }

  private double round(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  /**
   * 将聚合查询返回的数值对象安全转为 long（兼容 Long/Double/BigDecimal 等类型）。
   */
  private long toLong(Object value) {
    if (value == null) {
      return 0L;
    }
    if (value instanceof Number number) {
      return number.longValue();
    }
    return Long.parseLong(value.toString());
  }
}
