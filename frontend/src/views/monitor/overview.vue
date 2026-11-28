<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><Odometer /></el-icon>
        </div>
        <div>
          <h2 class="header-title">系统概览</h2>
          <p class="header-sub">服务运行状态、资源占用与 JVM 关键指标</p>
        </div>
      </div>
      <div class="header-right">
        <el-tag v-if="overview" :type="overview.uptimeMs > 0 ? 'success' : 'danger'" effect="light">
          {{ overview.uptimeMs > 0 ? '服务运行中' : '异常' }}
        </el-tag>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- ===== 概览指标卡 ===== -->
    <div v-if="overview" class="metric-cards">
      <div class="metric-card">
        <span class="metric-label">运行时长</span>
        <span class="metric-value">{{ formatUptime(overview.uptimeMs) }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">系统 CPU</span>
        <span class="metric-value">{{ overview.systemCpuLoadPercent ?? '--' }}%</span>
        <span class="metric-sub">进程 {{ overview.processCpuLoad ?? '--' }}%</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">系统负载均值</span>
        <span class="metric-value">{{ formatLoadAverage(overview.os?.systemLoadAverage) }}</span>
        <span class="metric-sub">{{ overview.availableProcessors }} 核</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">堆内存使用</span>
        <span class="metric-value">{{ formatBytes(overview.memory?.heapUsed) }}</span>
        <span class="metric-sub">/ {{ formatBytes(overview.memory?.heapMax) }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">堆内存占用率</span>
        <span class="metric-value" :style="{ color: heapUsageColor }">{{ overview.memory?.heapUsagePercent ?? '--' }}%</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">线程数</span>
        <span class="metric-value">{{ overview.threadCount }}</span>
        <span class="metric-sub">峰值 {{ overview.peakThreadCount }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">已加载类</span>
        <span class="metric-value">{{ overview.loadedClassCount }}</span>
        <span class="metric-sub">累计 {{ overview.totalLoadedClassCount }}</span>
      </div>
    </div>

    <!-- ===== 主体内容 ===== -->
    <div class="monitor-body">
      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">JVM 信息</span>
          </div>
          <div class="info-list">
            <div class="info-row"><span class="info-label">JVM 名称</span><span class="info-value">{{ overview?.jvmName ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">Java 版本</span><span class="info-value">{{ overview?.jvmVersion ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">可用处理器</span><span class="info-value">{{ overview?.availableProcessors ?? '--' }} 核</span></div>
            <div class="info-row"><span class="info-label">非堆内存</span><span class="info-value">{{ formatBytes(overview?.memory?.nonHeapUsed) }}</span></div>
            <div class="info-row"><span class="info-label">系统物理内存</span><span class="info-value">{{ formatBytes(overview?.totalPhysicalMemory) }}</span></div>
            <div class="info-row"><span class="info-label">空闲物理内存</span><span class="info-value">{{ formatBytes(overview?.freePhysicalMemory) }}</span></div>
            <div class="info-row"><span class="info-label">守护线程</span><span class="info-value">{{ overview?.daemonThreadCount ?? '--' }}</span></div>
          </div>
        </div>

        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">操作系统信息</span>
          </div>
          <div class="info-list">
            <div class="info-row"><span class="info-label">系统名称</span><span class="info-value">{{ overview?.os?.name ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">系统版本</span><span class="info-value">{{ overview?.os?.version ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">系统架构</span><span class="info-value">{{ overview?.os?.arch ?? '--' }}</span></div>
            <div class="info-row"><span class="info-label">系统负载均值</span><span class="info-value">{{ formatLoadAverage(overview?.os?.systemLoadAverage) }}</span></div>
            <div class="info-row"><span class="info-label">总物理内存</span><span class="info-value">{{ formatBytes(overview?.os?.totalPhysicalMemory) }}</span></div>
            <div class="info-row"><span class="info-label">空闲物理内存</span><span class="info-value">{{ formatBytes(overview?.os?.freePhysicalMemory) }}</span></div>
            <div class="info-row"><span class="info-label">已提交虚拟内存</span><span class="info-value">{{ formatBytes(overview?.os?.committedVirtualMemory) }}</span></div>
          </div>
        </div>
      </div>

      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">内存构成</span>
          </div>
          <div ref="memoryPieRef" class="chart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Odometer, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import monitorService from '@/services/monitor/monitor.service'
import { formatBytes, formatUptime, chartTextColor } from './common'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

/* ---- 状态 ---- */
const loading = ref(false)
const overview = ref(null)
const memoryPieRef = ref(null)
let memoryPie = null
let refreshTimer = null

/* ---- 计算属性 ---- */
const heapUsageColor = computed(() => {
  const p = overview.value?.memory?.heapUsagePercent || 0
  if (p > 85) return '#e24b4a'
  if (p > 70) return '#ba7517'
  return '#1d9e75'
})

/* ---- 格式化系统负载均值 ---- */
function formatLoadAverage(v) {
  if (v == null || v < 0) return '--'
  return v.toFixed(2)
}

/* ---- 渲染内存构成饼图 ---- */
function renderMemoryPie() {
  if (!memoryPieRef.value || !overview.value) return
  if (!memoryPie) {
    memoryPie = echarts.init(memoryPieRef.value)
  }
  const m = overview.value.memory
  const used = m.heapUsed
  const max = m.heapMax
  const free = Math.max(0, max - used)

  memoryPie.setOption({
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      valueFormatter: v => formatBytes(v)
    },
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
        { name: '已用', value: used, itemStyle: { color: '#378add' } },
        { name: '可用', value: free, itemStyle: { color: '#e4e7ed' } }
      ]
    }]
  })
}

/* ---- 数据加载 ---- */
async function load() {
  loading.value = true
  try {
    const res = await monitorService.overview()
    if (res.code === 200 && res.data) {
      overview.value = res.data.data ?? res.data
      await nextTick()
      renderMemoryPie()
    }
  } finally {
    loading.value = false
  }
}

function onResize() {
  memoryPie?.resize()
}

onMounted(() => {
  load()
  refreshTimer = setInterval(load, 10000)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', onResize)
  memoryPie?.dispose()
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
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
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
  height: 300px;
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
