<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="360"
    trigger="click"
    popper-class="notif-popper"
    :show-arrow="false"
  >
    <!-- 触发按钮 -->
    <template #reference>
      <div class="notif-trigger" title="消息通知" aria-label="消息通知">
        <el-icon :size="17">
          <Bell />
        </el-icon>
        <span v-if="unreadCount > 0" class="notif-badge">
          {{ unreadCount > 99 ? '99+' : unreadCount }}
        </span>
      </div>
    </template>

    <!-- 面板 -->
    <div class="notif-panel">

      <!-- 头部 -->
      <div class="np-head">
        <span class="np-title">消息通知</span>
        <div class="np-actions">
          <span v-if="unreadCount > 0" class="np-link" @click="readAll">全部已读</span>
          <el-icon class="np-icon-btn" :size="13" title="刷新" @click="fetchNotifications">
            <Refresh />
          </el-icon>
        </div>
      </div>

      <!-- Tab -->
      <div class="np-tabs">
        <div class="np-tab" :class="{ active: tab === 'all' }" @click="tab = 'all'">全部</div>
        <div class="np-tab" :class="{ active: tab === 'unread' }" @click="tab = 'unread'">
          未读
          <span v-if="unreadCount > 0" class="np-tab-badge">{{ unreadCount }}</span>
        </div>
      </div>

      <!-- 列表 -->
      <div class="np-list" v-loading="loading">
        <template v-if="filteredList.length > 0">
          <div
            v-for="item in filteredList"
            :key="item.id"
            class="np-item"
            :class="{ unread: !item.read }"
            @click="markRead(item)"
          >
            <div class="np-dot" :class="{ hidden: item.read }" />
            <div class="np-type-icon" :class="item.type">
              <el-icon :size="15">
                <component :is="iconMap[item.type]" />
              </el-icon>
            </div>
            <div class="np-body">
              <div class="np-msg" :title="item.message">{{ item.message }}</div>
              <div class="np-time">{{ item.time }}</div>
            </div>
          </div>
        </template>
        <div v-else class="np-empty">
          <el-icon :size="28">
            <MuteNotification />
          </el-icon>
          <span>暂无通知</span>
        </div>
      </div>

      <!-- 底部 -->
      <div class="np-foot">
        <span class="np-link" @click="goNotifyRecord">查看全部通知</span>
      </div>

    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import {
  Bell, Refresh, MuteNotification,
  CircleCheck, CircleClose, Warning, InfoFilled
} from '@element-plus/icons-vue'
import reportsService from '../services/hrms/reports/reports.service'

/* ---- 状态 ---- */
const router = useRouter()
const visible = ref(false)
const loading = ref(false)
const tab = ref('all')
const notifications = ref([])
let pollTimer = null

/* ---- 图标映射 ---- */
const iconMap = {
  success: CircleCheck,
  danger: CircleClose,
  warning: Warning,
  info: InfoFilled
}

/* ---- 计算属性 ---- */
const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)
const filteredList = computed(() =>
  tab.value === 'unread'
    ? notifications.value.filter(n => !n.read)
    : notifications.value
)

/* ---- 数据 ---- */
async function fetchNotifications() {
  loading.value = true
  try {
    const today = getToday()
    const res = await reportsService.getDingTalkRecords(
      { page: 0, size: 10 },
      { startNotifyTime: `${today} 00:00:00`, endNotifyTime: `${today} 23:59:59` }
    )
    if (res.code === 200 && res.data?.data) {
      const readIds = JSON.parse(localStorage.getItem('wa.notif.read') || '[]')
      notifications.value = res.data.data.map(item => ({
        id: item.id ?? item.msgSerialNumber ?? Math.random(),
        type: item.notifyStatus === 'SUCCESS' ? 'success' : 'danger',
        message: buildMsg(item),
        time: item.notifyTime || item.createDate || '',
        read: readIds.includes(String(item.id ?? item.msgSerialNumber))
      }))
    }
  } catch (e) {
    console.warn('[NotificationBell] 加载失败', e)
  } finally {
    loading.value = false
  }
}

/* ---- 操作 ---- */
function markRead(item) {
  item.read = true
  persist()
}

function readAll() {
  notifications.value.forEach(n => (n.read = true))
  persist()
}

function persist() {
  const ids = notifications.value.filter(n => n.read).map(n => String(n.id))
  localStorage.setItem('wa.notif.read', JSON.stringify(ids))
}

function goNotifyRecord() {
  visible.value = false
  router.push('/hrms/reports/notify-record')
}

/* ---- 工具 ---- */
function getToday() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function buildMsg(item) {
  const raw = item.content ? item.content.replace(/<[^>]+>/g, '').slice(0, 40) : '—'
  return `${item.employeeName ?? ''} ${item.title ?? ''}：${raw}`
}

/* ---- 生命周期 ---- */
onMounted(() => {
  fetchNotifications()
  pollTimer = setInterval(fetchNotifications, 30_000)
})
onBeforeUnmount(() => {
  if (pollTimer) clearInterval(pollTimer)
})

/* ---- 暴露 ---- */
defineExpose({ fetchNotifications, unreadCount })
</script>

<style lang="scss" scoped>
.notif-trigger {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  position: relative;
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }
}

.notif-badge {
  position: absolute;
  top: 5px;
  right: 5px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: #e24b4a;
  color: #fff;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
}
</style>

<style>
/* Popover 容器 */
.notif-popper.el-popover {
  padding: 0 !important;
  border-radius: 12px !important;
  border: 0.5px solid var(--color-border-tertiary) !important;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12) !important;
  overflow: hidden;
}

/* 头部 */
.notif-panel .np-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px 10px;
  border-bottom: 0.5px solid var(--color-border-tertiary);
}

.notif-panel .np-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
}

.notif-panel .np-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.notif-panel .np-link {
  font-size: 11px;
  color: var(--color-text-info);
  cursor: pointer;
}

.notif-panel .np-link:hover {
  text-decoration: underline;
}

.notif-panel .np-icon-btn {
  color: var(--color-text-secondary);
  cursor: pointer;
}

.notif-panel .np-icon-btn:hover {
  color: var(--color-text-primary);
}

/* Tab */
.notif-panel .np-tabs {
  display: flex;
  border-bottom: 0.5px solid var(--color-border-tertiary);
}

.notif-panel .np-tab {
  flex: 1;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: color 0.15s, border-color 0.15s;
}

.notif-panel .np-tab.active {
  color: #378add;
  border-bottom-color: #378add;
  font-weight: 500;
}

.notif-panel .np-tab-badge {
  background: #e24b4a;
  color: #fff;
  border-radius: 8px;
  font-size: 10px;
  padding: 0 5px;
  line-height: 16px;
}

/* 列表 */
.notif-panel .np-list {
  max-height: 320px;
  overflow-y: auto;
  min-height: 80px;
}

.notif-panel .np-list::-webkit-scrollbar {
  width: 4px;
}

.notif-panel .np-list::-webkit-scrollbar-thumb {
  background: var(--color-border-secondary);
  border-radius: 2px;
}

/* 列表项 */
.notif-panel .np-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 11px 14px;
  border-bottom: 0.5px solid var(--color-border-tertiary);
  cursor: pointer;
  transition: background 0.12s;
}

.notif-panel .np-item:last-child {
  border-bottom: none;
}

.notif-panel .np-item:hover {
  background: var(--color-background-secondary);
}

.notif-panel .np-item.unread {
  background: var(--color-background-info);
}

.notif-panel .np-item.unread:hover {
  background: #d5e9f7;
}

/* 蓝点 */
.notif-panel .np-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #378add;
  flex-shrink: 0;
  margin-top: 5px;
}

.notif-panel .np-dot.hidden {
  background: transparent;
}

/* 类型图标 */
.notif-panel .np-type-icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notif-panel .np-type-icon.success {
  background: #e1f5ee;
  color: #0f6e56;
}

.notif-panel .np-type-icon.danger {
  background: #fcebeb;
  color: #a32d2d;
}

.notif-panel .np-type-icon.warning {
  background: #faeeda;
  color: #854f0b;
}

.notif-panel .np-type-icon.info {
  background: #e6f1fb;
  color: #185fa5;
}

/* 内容 */
.notif-panel .np-body {
  flex: 1;
  min-width: 0;
}

.notif-panel .np-msg {
  font-size: 12px;
  color: var(--color-text-primary);
  line-height: 1.5;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notif-panel .np-time {
  font-size: 11px;
  color: var(--color-text-tertiary);
}

/* 空状态 */
.notif-panel .np-empty {
  padding: 32px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  color: var(--color-text-tertiary);
  font-size: 12px;
}

/* 底部 */
.notif-panel .np-foot {
  padding: 10px 14px;
  border-top: 0.5px solid var(--color-border-tertiary);
  text-align: center;
}

.notif-panel .np-foot .np-link {
  font-size: 12px;
}
</style>