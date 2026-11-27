<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><Connection /></el-icon>
        </div>
        <div>
          <h2 class="header-title">线程监控</h2>
          <p class="header-sub">线程状态分布、存活线程明细与死锁检测</p>
        </div>
      </div>
      <div class="header-right">
        <el-tag v-if="threadData" :type="threadData.deadlockedCount > 0 ? 'danger' : 'success'" effect="light">
          {{ threadData.deadlockedCount > 0 ? `死锁 ${threadData.deadlockedCount}` : '无死锁' }}
        </el-tag>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- ===== 状态分布 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">线程状态分布</span>
        <span class="panel-sub" v-if="threadData">共 {{ threadData.total }} 个存活线程</span>
      </div>
      <div ref="threadPieRef" class="chart"></div>
    </div>

    <!-- ===== 线程列表 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">线程列表（按 CPU 耗时降序）</span>
      </div>
      <el-table :data="threadData?.threads || []" size="small" border v-loading="loading" height="480">
        <el-table-column prop="name" label="线程名" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" min-width="120" align="center">
          <template #default="{ row }">
            <span class="thread-state" :class="'state-' + row.state?.toLowerCase()">{{ row.state }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cpuTimeMs" label="CPU 时间(ms)" min-width="130" align="right" />
        <el-table-column prop="blockedCount" label="阻塞次数" min-width="100" align="right" />
        <el-table-column prop="waitedCount" label="等待次数" min-width="100" align="right" />
        <el-table-column prop="priority" label="优先级" min-width="80" align="center" />
        <el-table-column label="守护" min-width="70" align="center">
          <template #default="{ row }">{{ row.daemon ? '是' : '否' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { Connection, Refresh } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import monitorService from '@/services/monitor/monitor.service'
import { chartTextColor } from './common'

echarts.use([PieChart, TooltipComponent, LegendComponent, CanvasRenderer])

/* ---- 状态 ---- */
const loading = ref(false)
const threadData = ref(null)
const threadPieRef = ref(null)
let threadPie = null
let refreshTimer = null

const stateColorMap = {
  RUNNABLE: '#1d9e75',
  WAITING: '#378add',
  TIMED_WAITING: '#9b6cf0',
  BLOCKED: '#e24b4a',
  NEW: '#ba7517',
  TERMINATED: '#909399'
}

/* ---- 渲染状态分布饼图 ---- */
function renderThreadPie() {
  if (!threadPieRef.value || !threadData.value) return
  if (!threadPie) {
    threadPie = echarts.init(threadPieRef.value)
  }
  const dist = threadData.value.stateDistribution || {}
  const data = Object.entries(dist)
    .filter(([, v]) => v > 0)
    .map(([name, value]) => ({ name, value, itemStyle: { color: stateColorMap[name] || '#909399' } }))

  threadPie.setOption({
    backgroundColor: 'transparent',
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      bottom: 0,
      textStyle: { color: chartTextColor(), fontSize: 11 }
    },
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderWidth: 2, borderColor: 'transparent' },
      label: { show: false },
      data
    }]
  })
}

/* ---- 数据加载 ---- */
async function load() {
  loading.value = true
  try {
    const res = await monitorService.threads()
    if (res.code === 200) {
      threadData.value = res.data?.data ?? res.data
      await nextTick()
      renderThreadPie()
    }
  } finally {
    loading.value = false
  }
}

function onResize() {
  threadPie?.resize()
}

onMounted(() => {
  load()
  refreshTimer = setInterval(load, 15000)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', onResize)
  threadPie?.dispose()
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

    .panel-title {
      font-size: 14px;
      font-weight: 600;
      color: var(--color-text-primary);
    }

    .panel-sub {
      font-size: 12px;
      color: var(--color-text-secondary);
    }
  }
}

.chart {
  width: 100%;
  height: 280px;
}

.thread-state {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 10px;
  font-size: 12px;
  line-height: 1.6;

  &.state-runnable { background: rgba(29, 158, 117, 0.12); color: #1d9e75; }
  &.state-waiting { background: rgba(55, 138, 221, 0.12); color: #378add; }
  &.state-timed_waiting { background: rgba(155, 108, 240, 0.12); color: #9b6cf0; }
  &.state-blocked { background: rgba(226, 75, 74, 0.12); color: #e24b4a; }
  &.state-new { background: rgba(186, 117, 23, 0.12); color: #ba7517; }
  &.state-terminated { background: rgba(144, 147, 153, 0.12); color: #909399; }
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
