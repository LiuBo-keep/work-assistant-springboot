<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><Monitor /></el-icon>
        </div>
        <div>
          <h2 class="header-title">接口监控</h2>
          <p class="header-sub">接口调用量、成功率、耗时与状态码分布</p>
        </div>
      </div>
      <div class="header-right">
        <el-select v-model="hours" size="default" style="width: 120px" @change="loadStats">
          <el-option :value="1" label="近 1 小时" />
          <el-option :value="6" label="近 6 小时" />
          <el-option :value="24" label="近 24 小时" />
          <el-option :value="72" label="近 3 天" />
        </el-select>
        <el-button :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
    </div>

    <!-- ===== 统计指标卡 ===== -->
    <div class="metric-cards">
      <div class="metric-card">
        <span class="metric-label">总调用次数</span>
        <span class="metric-value">{{ stats?.totalCount ?? '--' }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">成功次数</span>
        <span class="metric-value" style="color: #1d9e75">{{ stats?.successCount ?? '--' }}</span>
      </div>
      <div class="metric-card">
        <span class="metric-label">失败次数</span>
        <span class="metric-value" :style="{ color: stats?.failCount > 0 ? '#e24b4a' : undefined }">
          {{ stats?.failCount ?? '--' }}
        </span>
      </div>
      <div class="metric-card">
        <span class="metric-label">成功率</span>
        <span class="metric-value" :style="{ color: successRateColor }">{{ stats?.successRate ?? '--' }}%</span>
      </div>
    </div>

    <!-- ===== 接口聚合 + 状态码分布 ===== -->
    <div class="monitor-body">
      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">接口调用 TOP（按次数）</span>
          </div>
          <el-table :data="stats?.topApis || []" size="small" border v-loading="loading" max-height="360">
            <el-table-column prop="path" label="接口路径" min-width="260" show-overflow-tooltip />
            <el-table-column prop="count" label="调用次数" min-width="100" align="right" />
            <el-table-column label="平均耗时" min-width="110" align="right">
              <template #default="{ row }">{{ formatMs(row.avgDurationMs) }}</template>
            </el-table-column>
            <el-table-column label="最大耗时" min-width="110" align="right">
              <template #default="{ row }">
                <span :style="{ color: row.maxDurationMs > 1000 ? '#e24b4a' : undefined }">
                  {{ formatMs(row.maxDurationMs) }}
                </span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <div class="col">
        <div class="panel">
          <div class="panel-head">
            <span class="panel-title">状态码分布</span>
          </div>
          <div v-if="statusDist.length" class="status-list">
            <div v-for="s in statusDist" :key="s.statusCode" class="status-item">
              <el-tag :type="statusTagType(s.statusCode)" size="small">{{ s.statusCode }}</el-tag>
              <div class="status-bar-wrap">
                <div
                  class="status-bar"
                  :style="{ width: statusBarWidth(s.count), background: statusColor(s.statusCode) }"
                ></div>
              </div>
              <span class="status-count">{{ s.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无数据" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- ===== 调用明细 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">调用明细（最近 100 条）</span>
      </div>
      <el-table :data="logs" size="small" border v-loading="loading" max-height="480">
        <el-table-column prop="httpMethod" label="方法" min-width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="methodTagType(row.httpMethod)" size="small" effect="plain">{{ row.httpMethod }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestPath" label="路径" min-width="260" show-overflow-tooltip />
        <el-table-column label="状态码" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.statusCode)" size="small">{{ row.statusCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="耗时" min-width="100" align="right">
          <template #default="{ row }">
            <span :style="{ color: row.durationMs > 1000 ? '#e24b4a' : undefined }">
              {{ formatMs(row.durationMs) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="clientIp" label="客户端 IP" min-width="130" />
        <el-table-column prop="userName" label="用户" min-width="100">
          <template #default="{ row }">{{ row.userName || '—' }}</template>
        </el-table-column>
        <el-table-column label="时间" min-width="165">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Monitor, Refresh } from '@element-plus/icons-vue'
import monitorService from '@/services/monitor/monitor.service'
import { formatMs, formatTime } from './common'

/* ---- 状态 ---- */
const loading = ref(false)
const hours = ref(24)
const stats = ref(null)
const logs = ref([])
let refreshTimer = null

/* ---- 计算属性 ---- */
const successRateColor = computed(() => {
  const r = stats.value?.successRate ?? 100
  if (r < 90) return '#e24b4a'
  if (r < 99) return '#ba7517'
  return '#1d9e75'
})

const statusDist = computed(() => stats.value?.statusDistribution || [])

function statusTagType(code) {
  if (code >= 200 && code < 300) return 'success'
  if (code >= 300 && code < 400) return 'warning'
  return 'danger'
}

function statusColor(code) {
  if (code >= 200 && code < 300) return '#1d9e75'
  if (code >= 300 && code < 400) return '#ba7517'
  return '#e24b4a'
}

function methodTagType(method) {
  const map = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
  return map[method] || 'info'
}

function statusBarWidth(count) {
  const max = Math.max(...statusDist.value.map(s => s.count), 1)
  return Math.round((count / max) * 100) + '%'
}

/* ---- 数据加载 ---- */
async function loadStats() {
  const res = await monitorService.apiStats(hours.value)
  if (res.code === 200) {
    stats.value = res.data?.data ?? res.data
  }
}

async function loadLogs() {
  const res = await monitorService.apiLogs()
  if (res.code === 200) {
    logs.value = res.data?.data ?? res.data ?? []
  }
}

async function load() {
  loading.value = true
  try {
    await Promise.all([loadStats(), loadLogs()])
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  refreshTimer = setInterval(load, 15000)
})

onBeforeUnmount(() => {
  if (refreshTimer) clearInterval(refreshTimer)
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

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 10px;

  .status-bar-wrap {
    flex: 1;
    height: 8px;
    background: var(--color-bg-hover);
    border-radius: 4px;
    overflow: hidden;
  }

  .status-bar {
    height: 100%;
    border-radius: 4px;
    transition: width 0.3s;
  }

  .status-count {
    font-size: 13px;
    color: var(--color-text-primary);
    font-weight: 600;
    min-width: 40px;
    text-align: right;
  }
}

@media screen and (max-width: 768px) {
  .monitor-page {
    padding: 12px 12px 24px;
    gap: 12px;
  }

  .page-header {
    flex-wrap: wrap;

    .header-sub { display: none; }
    .header-title { font-size: 15px; }
  }

  .monitor-body {
    flex-direction: column;
  }
}
</style>
