<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><TrendCharts /></el-icon>
        </div>
        <div>
          <h2 class="header-title">资源趋势</h2>
          <p class="header-sub">堆内存、非堆内存、线程数与 CPU 的历史走势</p>
        </div>
      </div>
      <div class="header-right">
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- ===== 资源趋势图 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">资源趋势（近 24 小时）</span>
        <el-radio-group v-model="activeMetric" size="small" @change="loadHistory">
          <el-radio-button v-for="opt in METRIC_OPTIONS" :key="opt.value" :value="opt.value">
            {{ opt.label }}
          </el-radio-button>
        </el-radio-group>
      </div>
      <div ref="trendChartRef" class="chart"></div>
    </div>

    <!-- ===== GC 统计 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">GC 统计</span>
      </div>
      <el-table :data="overview?.gc || []" size="small" border>
        <el-table-column prop="name" label="收集器" min-width="140" />
        <el-table-column prop="count" label="次数" min-width="90" align="right" />
        <el-table-column label="总耗时" min-width="110" align="right">
          <template #default="{ row }">{{ formatMs(row.timeMs) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { TrendCharts, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import monitorService from '@/services/monitor/monitor.service'
import {
  formatMs, formatTime, chartTextColor, chartAxisLineColor, METRIC_META, METRIC_OPTIONS
} from './common'

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

/* ---- 状态 ---- */
const loading = ref(false)
const overview = ref(null)
const activeMetric = ref('heap.used')
const trendChartRef = ref(null)
let trendChart = null
let refreshTimer = null

function metricMeta() {
  return METRIC_META[activeMetric.value] || METRIC_META['heap.used']
}

/* ---- 渲染趋势图 ---- */
function renderTrendChart(history) {
  if (!trendChartRef.value) return
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  const meta = metricMeta()
  const times = history.map(p => formatTime(p.time))
  const values = history.map(p => (p.value / meta.scale).toFixed(2))

  trendChart.setOption({
    backgroundColor: 'transparent',
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    tooltip: {
      trigger: 'axis',
      valueFormatter: v => v + ' ' + meta.unit
    },
    xAxis: {
      type: 'category',
      data: times,
      axisLine: { lineStyle: { color: chartAxisLineColor() } },
      axisLabel: { color: chartTextColor(), fontSize: 11 }
    },
    yAxis: {
      type: 'value',
      name: meta.unit,
      axisLine: { show: false },
      splitLine: { lineStyle: { color: chartAxisLineColor() } },
      axisLabel: { color: chartTextColor(), fontSize: 11 }
    },
    series: [{
      name: meta.label,
      type: 'line',
      smooth: true,
      showSymbol: false,
      data: values,
      lineStyle: { width: 2, color: meta.color },
      areaStyle: { color: meta.color + '26' }
    }]
  })
}

/* ---- 数据加载 ---- */
async function loadHistory() {
  const res = await monitorService.metricHistory(activeMetric.value, 24)
  if (res.code === 200) {
    const data = res.data?.data ?? res.data ?? []
    renderTrendChart(Array.isArray(data) ? data : [])
  }
}

async function loadOverview() {
  const res = await monitorService.overview()
  if (res.code === 200 && res.data) {
    overview.value = res.data.data ?? res.data
  }
}

async function load() {
  loading.value = true
  try {
    await Promise.all([loadOverview(), loadHistory()])
  } finally {
    loading.value = false
  }
}

function onResize() {
  trendChart?.resize()
}

onMounted(() => {
  load()
  refreshTimer = setInterval(loadOverview, 10000)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', onResize)
  trendChart?.dispose()
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

.panel {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 16px 18px;

  .panel-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    flex-wrap: wrap;
    gap: 8px;

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

@media screen and (max-width: 768px) {
  .monitor-page {
    padding: 12px 12px 24px;
    gap: 12px;
  }

  .page-header {
    .header-sub { display: none; }
    .header-title { font-size: 15px; }
  }

  .chart {
    height: 220px;
  }
}
</style>
