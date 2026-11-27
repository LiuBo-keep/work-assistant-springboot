<template>
  <div class="page-wrap" v-loading="loading">

    <!-- ① 搜索区 -->
    <div class="search-card">
      <el-form ref="formRef" :model="search" :show-message="false" @keydown.enter="getCardRecords(1)">
        <div class="search-grid">
          <div class="search-field">
            <span class="field-label">打卡类型</span>
            <el-select v-model="search.clockInType" clearable placeholder="请选择打卡类型" size="small"
                       style="width:100%">
              <el-option label="上班" value="CHECK_IN_AT_WORK" />
              <el-option label="下班" value="CHECK_OUT_AT_WORK" />
            </el-select>
          </div>
          <div class="search-field">
            <span class="field-label">刷卡时间</span>
            <el-date-picker
              v-model="search.clockInTime"
              clearable
              type="daterange"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              start-placeholder="刷卡时间起"
              end-placeholder="刷卡时间止"
              :default-time="[new Date('2000-01-01 00:00:00'), new Date('2000-01-01 23:59:59')]"
              size="small"
            />
          </div>
        </div>
        <div class="search-footer">
          <el-button size="small" :icon="Refresh" @click="reset()">重置</el-button>
          <el-button size="small" type="primary" :icon="Search" @click="getCardRecords(1)">搜索</el-button>
          <div class="footer-divider" />
          <el-tooltip content="从 HRMS 拉取今日最新打卡记录并刷新列表" placement="top" :show-after="400">
            <el-button
              size="small"
              :icon="Promotion"
              :loading="syncing"
              type="primary"
              plain
              class="btn-sync"
              @click="syncToday"
            >
              同步今日打卡
            </el-button>
          </el-tooltip>
        </div>
      </el-form>
    </div>

    <!-- ② 数据表卡 -->
    <div class="table-card">
      <div class="table-bar">
        <span class="table-title">
          <el-icon><Grid /></el-icon>打卡记录明细
        </span>
        <div class="table-bar-right">
          <span v-if="lastSyncTime" class="sync-time">
            <el-icon :size="12"><Clock /></el-icon>
            上次同步：{{ lastSyncTime }}
          </span>
          <span class="table-meta">共 {{ pageInfo.total }} 条记录</span>
        </div>
      </div>

      <el-table :data="tableData" stripe size="small" style="width:100%" class="card-table">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="clockInType" label="打卡类型" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.clockInType === 'CHECK_IN_AT_WORK'" class="type-badge type-in">
              <el-icon :size="12"><Sunrise /></el-icon>上班
            </span>
            <span v-else class="type-badge type-out">
              <el-icon :size="12"><Sunset /></el-icon>下班
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="employeeId" label="员工号">
          <template #default="{ row }">
            <span class="mono-cell">{{ row.employeeId || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cnName" label="中文名">
          <template #default="{ row }">
            <span class="name-cell">{{ row.cnName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="enName" label="英文名">
          <template #default="{ row }">
            <span class="en-cell">{{ row.enName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="clockInTime" label="刷卡时间">
          <template #default="{ row }">
            <span class="time-cell">{{ row.clockInTime }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="table-empty">
            <el-icon :size="40"><DataBoard /></el-icon>
            <p class="empty-title">暂无打卡记录</p>
            <p class="empty-sub">调整筛选条件，或点击右上角「同步今日打卡」拉取最新数据</p>
          </div>
        </template>
      </el-table>

      <!-- ③ 分页条 -->
      <div class="pagination-bar">
        <el-pagination
          background
          v-model:current-page="pageInfo.page"
          :page-sizes="[50, 100, 200, 300, 400]"
          :page-size="pageInfo.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pageInfo.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Grid, Promotion, Clock, Sunrise, Sunset, DataBoard } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import reportsService from '@/services/hrms/reports/reports.service.js'
import notify from '@/utils/notify'

/* ---- refs ---- */
const loading = ref(false)
const syncing = ref(false)
const formRef = ref(null)
const tableData = ref([])
const lastSyncTime = ref(localStorage.getItem('wa.cardRecord.lastSync') || '')

/* ---- helpers ---- */
function getCurrentDate() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getNow() {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
}

/* ---- state ---- */
const search = reactive({
  clockInType: undefined,
  clockInTime: [getCurrentDate(), getCurrentDate()]
})

const pageInfo = reactive({ page: 1, size: 50, total: 0 })

/* ---- api ---- */
async function execute(action) {
  try {
    await action()
  } catch (e) {
    notify.error(e.message || e.stack)
  }
}

async function getCardRecords(currentPage) {
  loading.value = true
  await execute(async () => {
    if (search.clockInTime?.length === 2) {
      search.startClockInTime = search.clockInTime[0]
      search.endClockInTime = search.clockInTime[1]
    } else {
      search.startClockInTime = null
      search.endClockInTime = null
    }
    const res = await reportsService.getCardRecords(
      { page: currentPage - 1, size: pageInfo.size },
      search
    )
    if (res.code !== 200) return notify.error(res.error)
    tableData.value = res.data.data
    pageInfo.total = res.data.page.totalCount
    pageInfo.page = res.data.page.pageIndex + 1
  })
  loading.value = false
}

/* ---- 同步今日打卡 ---- */
async function syncToday() {
  syncing.value = true
  await execute(async () => {
    const res = await reportsService.refreshCardRecord({
      clockInType: search.clockInType  // null = 同步上班 + 下班全部类型
    })
    if (res.code !== 200 || res.data?.error) {
      ElMessage({
        showClose: true,
        dangerouslyUseHTMLString: true,
        message: `<div style="max-height:300px;overflow-y:auto">${res.error || res.data?.error?.message || '同步失败'}</div>`,
        type: 'error',
        duration: 0
      })
      return
    }
    // 记录同步时间
    const now = `今天 ${getNow()}`
    lastSyncTime.value = now
    localStorage.setItem('wa.cardRecord.lastSync', now)
    ElMessage.success('同步成功，已拉取最新打卡记录')
    // 自动切换到今日并刷新
    search.clockInTime = [getCurrentDate(), getCurrentDate()]
    await getCardRecords(1)
  })
  syncing.value = false
}

function handleSizeChange(size) {
  pageInfo.size = size
  getCardRecords(1)
}

function handleCurrentChange(page) {
  pageInfo.page = page
  getCardRecords(page)
}

async function reset() {
  formRef.value?.resetFields()
  search.clockInTime = [getCurrentDate(), getCurrentDate()]
  await getCardRecords(1)
}

onMounted(() => getCardRecords(1))
</script>

<style lang="scss" scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  background: var(--color-bg-page);
  min-height: 100%;
  box-sizing: border-box;
}

/* ===== 搜索卡 ===== */
.search-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 16px 18px 12px;
  box-shadow: var(--shadow-sm);
}

.search-grid {
  display: grid;
  grid-template-columns: 160px 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.search-field {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .el-input, .el-select, .el-date-editor {
    width: 100% !important;
  }
}

.field-label {
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-secondary);
  letter-spacing: 0.03em;
}

.search-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid var(--color-border-light);
}

/* 分隔竖线 */
.footer-divider {
  width: 1px;
  height: 18px;
  background: var(--color-border);
  margin: 0 2px;
}

/* 同步按钮 */
.btn-sync {
  /* 使用品牌蓝的 plain 样式，与「搜索」主按钮同族但弱化，保持协调 */
}

/* 打卡类型徽章 */
.type-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 500;
  line-height: 1.6;

  &.type-in {
    background: var(--brand-primary-fade);
    color: var(--brand-primary);
  }

  &.type-out {
    background: var(--color-warning-soft);
    color: var(--color-warning);
  }
}

.mono-cell {
  font-family: 'SF Mono', 'JetBrains Mono', Consolas, Menlo, monospace;
  font-size: 12px;
  color: var(--color-text-regular);
}

.time-cell {
  font-variant-numeric: tabular-nums;
  color: var(--color-text-regular);
}

/* 空状态 */
.table-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 48px 0;

  .el-icon {
    color: var(--color-border);
  }
}

.empty-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
  margin: 0;
}

.empty-sub {
  font-size: 12px;
  color: var(--color-text-placeholder);
  margin: 0;
}

/* ===== 数据表卡 ===== */
.table-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.table-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.table-bar-right {
  display: flex;
  align-items: center;
  gap: 14px;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);

  .el-icon {
    font-size: 15px;
    color: var(--color-text-secondary);
  }
}

/* 上次同步时间 */
.sync-time {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-placeholder);
}

.table-meta {
  font-size: 12px;
  color: var(--color-text-placeholder);
}

:deep(.el-table th.el-table__cell) {
  font-size: 12px;
  letter-spacing: 0.03em;
  background: var(--color-bg-hover) !important;
  color: var(--color-text-secondary);
  font-weight: 500;
  height: 42px;
}

:deep(.el-table td.el-table__cell) {
  font-size: 12px;
  padding: 9px 0;
}

/* 斑马纹 */
.card-table :deep(.el-table__body tr.el-table__row--striped td.el-table__cell) {
  background: var(--color-bg-hover);
}

/* hover 高亮 */
.card-table :deep(.el-table__body tr:hover > td.el-table__cell) {
  background: var(--brand-primary-fade) !important;
}

:deep(.el-table__body),
:deep(.el-table__header) {
  table-layout: fixed !important;
  width: 100% !important;
}

.name-cell {
  font-weight: 500;
  color: var(--color-text-primary);
}

.en-cell {
  color: var(--color-text-secondary);
  font-size: 12px;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 12px 16px;
  border-top: 1px solid var(--color-border-light);
  flex-shrink: 0;
}
</style>