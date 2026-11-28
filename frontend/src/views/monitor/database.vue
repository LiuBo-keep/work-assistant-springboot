<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><Coin /></el-icon>
        </div>
        <div>
          <h2 class="header-title">数据库监控</h2>
          <p class="header-sub">数据库连接池状态与连通性</p>
        </div>
      </div>
      <div class="header-right">
        <el-tag v-if="dbData" :type="dbData.healthy ? 'success' : 'danger'" effect="light">
          {{ dbData.healthy ? '连接正常' : '连接异常' }}
        </el-tag>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- ===== 连接池指标卡 ===== -->
    <div v-if="dbData" class="metric-cards">
      <div class="metric-card">
        <span class="metric-label">连接池类型</span>
        <span class="metric-value metric-value-sm">{{ dbData.poolType }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">活跃连接</span>
        <span class="metric-value" :style="{ color: activeColor }">{{ dbData.activeConnections ?? '--' }}</span>
        <span class="metric-sub">最大 {{ dbData.maximumPoolSize }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">空闲连接</span>
        <span class="metric-value">{{ dbData.idleConnections ?? '--' }}</span>
        <span class="metric-sub">最小 {{ dbData.minimumIdle }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">连接占用率</span>
        <span class="metric-value" :style="{ color: usageColor }">{{ dbData.usagePercent ?? '--' }}%</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">等待获取连接</span>
        <span class="metric-value" :style="{ color: dbData.threadsAwaitingConnection > 0 ? '#e24b4a' : undefined }">
          {{ dbData.threadsAwaitingConnection ?? '--' }}
        </span>
      </div>
      <div class="metric-card">
        <span class="metric-label">连接探测耗时</span>
        <span class="metric-value">{{ dbData.pingMs ?? '--' }}<span class="metric-unit">ms</span></span>
      </div>
    </div>

    <!-- ===== 泄漏检测 + 自动续连 ===== -->
    <div v-if="dbData" class="monitor-body">
      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">连接泄漏检测</span>
            <el-tag :type="dbData.leakDetectionEnabled ? 'success' : 'info'" size="small">
              {{ dbData.leakDetectionEnabled ? '已启用' : '未启用' }}
            </el-tag>
          </div>
          <div class="info-list">
            <div class="info-row">
              <span class="info-label">检测阈值</span>
              <span class="info-value">{{ dbData.leakDetectionEnabled ? formatMs(dbData.leakDetectionThresholdMs) : '—' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">说明</span>
              <span class="info-value info-value-muted">连接持有超过阈值即视为泄漏，服务端日志会打印告警</span>
            </div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">自动续连</span>
            <el-tag :type="dbData.autoRenewalEnabled ? 'success' : 'info'" size="small">
              {{ dbData.autoRenewalEnabled ? '已启用' : '未启用' }}
            </el-tag>
          </div>
          <div class="info-list">
            <div class="info-row"><span class="info-label">连接最大存活</span><span class="info-value">{{ formatMs(dbData.maxLifetimeMs) }}</span></div>
            <div class="info-row"><span class="info-label">空闲保活间隔</span><span class="info-value">{{ formatMs(dbData.keepaliveTimeMs) }}</span></div>
            <div class="info-row">
              <span class="info-label">机制</span>
              <span class="info-value info-value-muted">连接到期自动销毁重建；空闲连接定期发 keepalive 防止被数据库/防火墙断开</span>
            </div>
          </div>
        </div>
      </div>

      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">连接池配置</span>
          </div>
          <div class="info-list">
            <div class="info-row"><span class="info-label">最大连接数</span><span class="info-value">{{ dbData?.maximumPoolSize ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">最小空闲连接</span><span class="info-value">{{ dbData?.minimumIdle ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">连接超时</span><span class="info-value">{{ formatMs(dbData?.connectionTimeoutMs) }}</span></div>
            <div class="info-row"><span class="info-label">空闲超时</span><span class="info-value">{{ formatMs(dbData?.idleTimeoutMs) }}</span></div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">连接占用情况</span>
          </div>
          <div ref="poolChartRef" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Coin, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import monitorService from '@/services/monitor/monitor.service'
import { formatMs, chartTextColor } from './common'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

/* ---- 状态 ---- */
const loading = ref(false)
const dbData = ref(null)
const poolChartRef = ref(null)
let poolChart = null
let refreshTimer = null

/* ---- 计算属性 ---- */
const activeColor = computed(() => {
  const active = dbData.value?.activeConnections ?? 0
  const max = dbData.value?.maximumPoolSize ?? 0
  if (max > 0 && active / max > 0.8) return '#e24b4a'
  if (max > 0 && active / max > 0.6) return '#ba7517'
  return undefined
})

const usageColor = computed(() => {
  const p = dbData.value?.usagePercent ?? 0
  if (p > 80) return '#e24b4a'
  if (p > 60) return '#ba7517'
  return undefined
})

/* ---- 渲染连接占用饼图 ---- */
function renderPoolChart() {
  if (!poolChartRef.value || !dbData.value) return
  if (!poolChart) {
    poolChart = echarts.init(poolChartRef.value)
  }
  const active = dbData.value.activeConnections ?? 0
  const idle = dbData.value.idleConnections ?? 0
  const awaiting = dbData.value.threadsAwaitingConnection ?? 0
  const max = dbData.value.maximumPoolSize ?? 0
  const free = Math.max(0, max - active - idle)

  poolChart.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      bottom: 0,
      textStyle: { color: chartTextColor(), fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderWidth: 2, borderColor: 'transparent' },
      label: { show: false },
      data: [
        { name: '活跃', value: active, itemStyle: { color: '#1d9e75' } },
        { name: '空闲', value: idle, itemStyle: { color: '#378add' } },
        { name: '等待', value: awaiting, itemStyle: { color: '#e24b4a' } },
        { name: '可分配', value: free, itemStyle: { color: '#e4e7ed' } }
      ]
    }]
  })
}

/* ---- 数据加载 ---- */
async function load() {
  loading.value = true
  try {
    const res = await monitorService.database()
    if (res.code === 200 && res.data) {
      dbData.value = res.data.data ?? res.data
      await nextTick()
      renderPoolChart()
    }
  } finally {
    loading.value = false
  }
}

function onResize() {
  poolChart?.resize()
}

onMounted(() => {
  load()
  refreshTimer = setInterval(load, 10000)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', onResize)
  poolChart?.dispose()
})
</script>

<style lang="scss" scoped>
.monitor-page {
  padding: 20px 24px 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-icon {
    width: 42px;
    height: 42px;
    border-radius: 10px;
    background: var(--brand-primary-soft);
    color: var(--brand-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--color-text-primary);
    margin: 0;
  }

  .header-sub {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin: 2px 0 0;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 10px;
  }
}

.metric-cards {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 12px;
}

.metric-card {
  background: var(--color-bg-card);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 4px;

  .metric-label {
    font-size: 12px;
    color: var(--color-text-secondary);
  }

  .metric-value {
    font-size: 22px;
    font-weight: 600;
    color: var(--color-text-primary);
    line-height: 1.2;

    &.metric-value-sm {
      font-size: 18px;
    }

    .metric-unit {
      font-size: 13px;
      font-weight: 400;
      margin-left: 2px;
      color: var(--color-text-secondary);
    }
  }

  .metric-sub {
    font-size: 11px;
    color: var(--color-text-placeholder);
  }
}

.monitor-body {
  display: flex;
  gap: 16px;

  .col {
    display: flex;
    flex-direction: column;
    gap: 16px;
    flex: 1;
    min-width: 0;
  }
}

.panel {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 16px 18px;

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--color-text-primary);
    }
  }
}

.chart {
  width: 100%;
  height: 260px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid var(--color-bg-hover);

  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }

  .info-label {
    font-size: 13px;
    color: var(--color-text-secondary);
  }

  .info-value {
    font-size: 13px;
    color: var(--color-text-primary);
    font-weight: 500;
    word-break: break-all;
    text-align: right;

    &.info-value-muted {
      font-size: 12px;
      font-weight: 400;
      color: var(--color-text-secondary);
      text-align: left;
      line-height: 1.5;
    }
  }
}

@media screen and (max-width: 768px) {
  .monitor-page {
    padding: 12px 12px 24px;
    gap: 12px;
  }

  .page-header {
    .header-sub { display: none; }
    .header-title { font-size: 15px; }
  }

  .metric-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 8px;
  }

  .monitor-body {
    flex-direction: column;
  }

  .chart {
    height: 220px;
  }
}
</style>
