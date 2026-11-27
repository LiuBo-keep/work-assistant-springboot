import BaseService from '../baseservice'

/**
 * MonitorService — 系统运行监控服务
 * 对接后端 /api/v1/monitor/... 接口
 */
class MonitorService extends BaseService {

  /** 系统概览指标（CPU/内存/JVM/GC/线程） */
  overview() {
    return this.get('/api/v1/monitor/overview')
  }

  /** 指定指标的历史快照 */
  metricHistory(metricName, hours = 24) {
    return this.get('/api/v1/monitor/metric-history', {
      params: { metricName, hours }
    })
  }

  /** 定时任务执行记录 */
  taskRunLogs() {
    return this.get('/api/v1/monitor/tasks')
  }

  /** 线程监控明细（状态分布 + 线程列表 + 死锁检测） */
  threads() {
    return this.get('/api/v1/monitor/threads')
  }

  /** 数据库连接池监控（连接池指标 + 连通性探测） */
  database() {
    return this.get('/api/v1/monitor/database')
  }
}

export default new MonitorService()
