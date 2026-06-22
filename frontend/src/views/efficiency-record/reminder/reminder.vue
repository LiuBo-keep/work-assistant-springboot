<template>
  <div class="reminder-layout">

    <!-- ===== 左侧：分类 & 状态 ===== -->
    <div class="reminder-sidebar">

      <div class="sb-head">
        <span class="sb-title">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
            <path d="M13.73 21a2 2 0 0 1-3.46 0" />
          </svg>
          定时提醒
        </span>
        <button class="icon-btn-sm" @click="openAdd" title="新建提醒">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      </div>

      <!-- 通知权限状态 -->
      <div class="perm-card" :class="permClass">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <div class="perm-text">
          <span class="perm-title">{{ permTitle }}</span>
          <span class="perm-sub">{{ permSub }}</span>
        </div>
        <button v-if="notifyPermission === 'default'" class="perm-btn" @click="requestPermission">授权</button>
      </div>

      <!-- 分类筛选 -->
      <div class="filter-section">
        <div class="filter-label">分类</div>
        <div
          v-for="c in categoryFilters" :key="c.key"
          class="filter-item" :class="{ active: activeCategory === c.key }"
          @click="activeCategory = c.key"
        >
          <span class="filter-icon" v-html="c.icon" />
          <span class="filter-name">{{ c.label }}</span>
          <span class="filter-count">{{ countByCategory(c.key) }}</span>
        </div>
      </div>

      <!-- 状态筛选 -->
      <div class="filter-section">
        <div class="filter-label">状态</div>
        <div
          v-for="s in statusFilters" :key="s.key"
          class="filter-item" :class="{ active: activeStatus === s.key }"
          @click="activeStatus = s.key"
        >
          <span class="filter-dot" :style="{ background: s.color }" />
          <span class="filter-name">{{ s.label }}</span>
          <span class="filter-count">{{ countByStatus(s.key) }}</span>
        </div>
      </div>

      <!-- 统计 -->
      <div class="sb-stats">
        <div class="stat-item">
          <span class="stat-num enabled">{{ reminders.filter(r => r.enabled).length }}</span>
          <span class="stat-lbl">已启用</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num">{{ reminders.filter(r => !r.enabled).length }}</span>
          <span class="stat-lbl">已停用</span>
        </div>
      </div>

    </div>

    <!-- ===== 右侧：提醒列表 ===== -->
    <div class="reminder-main">

      <!-- 工具栏 -->
      <div class="main-toolbar">
        <span class="result-count">{{ filteredReminders.length }} 个提醒</span>
        <div class="toolbar-right">
          <button class="toolbar-btn primary" @click="openAdd">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新建提醒
          </button>
        </div>
      </div>

      <!-- 列表 -->
      <div class="reminder-list" v-if="filteredReminders.length">
        <div
          v-for="r in filteredReminders" :key="r.id"
          class="reminder-card" :class="{ disabled: !r.enabled }"
        >
          <!-- 开关 -->
          <div class="r-switch" :class="{ on: r.enabled }" @click="toggleEnabled(r)">
            <div class="r-switch-dot" />
          </div>

          <!-- 图标 -->
          <div class="r-icon"
               :style="{ background: getCategory(r.category).color + '18', color: getCategory(r.category).color }">
            <span v-html="getCategory(r.category).icon" />
          </div>

          <!-- 主体 -->
          <div class="r-body">
            <div class="r-title-row">
              <span class="r-title" :class="{ disabled: !r.enabled }">{{ r.title }}</span>
              <span class="r-category-badge"
                    :style="{ background: getCategory(r.category).color + '18', color: getCategory(r.category).color }">{{ getCategory(r.category).label
                }}</span>
            </div>

            <div class="r-time-row">
              <!-- 一次性 -->
              <template v-if="r.repeatType === 'once'">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <polyline points="12 6 12 12 16 14" />
                </svg>
                <span>{{ formatDateTime(r.datetime) }}</span>
                <span v-if="r.enabled" class="countdown"
                      :class="{ soon: getCountdownMs(r) < 3600000 }">{{ formatCountdown(r) }}</span>
              </template>

              <!-- 每天 -->
              <template v-else-if="r.repeatType === 'daily'">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
                <span>每天 {{ r.time }}</span>
                <span v-if="r.enabled" class="countdown">{{ formatCountdown(r) }}</span>
              </template>

              <!-- 每周 -->
              <template v-else-if="r.repeatType === 'weekly'">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
                <span>{{ formatWeekdays(r.weekdays) }} {{ r.time }}</span>
                <span v-if="r.enabled" class="countdown">{{ formatCountdown(r) }}</span>
              </template>
            </div>

            <div class="r-desc" v-if="r.desc">{{ r.desc }}</div>
          </div>

          <!-- 操作 -->
          <div class="r-actions">
            <button class="action-icon-btn" @click="testNotify(r)" title="测试通知">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                <path d="M13.73 21a2 2 0 0 1-3.46 0" />
              </svg>
            </button>
            <button class="action-icon-btn" @click="openEdit(r)" title="编辑">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
            <button class="action-icon-btn danger" @click="removeReminder(r.id)" title="删除">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="3 6 5 6 21 6" />
                <path d="M19 6l-1 14H6L5 6" />
                <path d="M10 11v6M14 11v6M9 6V4h6v2" />
              </svg>
            </button>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="reminder-empty" v-else>
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
             opacity="0.2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <p class="empty-title">{{ activeCategory !== 'all' || activeStatus !== 'all' ? '没有符合条件的提醒' : '还没有任何提醒'
          }}</p>
        <p class="empty-sub">{{ activeCategory !== 'all' || activeStatus !== 'all' ? '换个条件试试' : '点击右上角「新建提醒」开始'
          }}</p>
      </div>

    </div>

    <!-- ===== 新建/编辑 弹窗 ===== -->
    <Transition name="modal">
      <div class="modal-mask" v-if="showForm" @click.self="closeForm">
        <div class="modal-box">
          <div class="modal-header">
            <span class="modal-title">{{ editingId ? '编辑提醒' : '新建提醒' }}</span>
            <button class="modal-close" @click="closeForm">✕</button>
          </div>
          <div class="modal-body">

            <div class="form-group">
              <label class="form-label required">提醒标题</label>
              <input class="form-input" v-model="form.title" placeholder="例如：喝水时间" maxlength="50"
                     ref="titleInputRef" />
            </div>

            <div class="form-group">
              <label class="form-label">分类</label>
              <div class="category-picker">
                <button
                  v-for="c in categories" :key="c.key"
                  class="category-btn" :class="{ active: form.category === c.key }"
                  :style="form.category === c.key ? { background: c.color + '18', borderColor: c.color, color: c.color } : {}"
                  @click="form.category = c.key"
                >
                  <span v-html="c.icon" /> {{ c.label }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">重复方式</label>
              <div class="seg-ctrl">
                <button :class="{ active: form.repeatType === 'once' }" @click="form.repeatType = 'once'">仅一次
                </button>
                <button :class="{ active: form.repeatType === 'daily' }" @click="form.repeatType = 'daily'">每天
                </button>
                <button :class="{ active: form.repeatType === 'weekly' }" @click="form.repeatType = 'weekly'">每周
                </button>
              </div>
            </div>

            <!-- 仅一次：日期时间 -->
            <div class="form-group" v-if="form.repeatType === 'once'">
              <label class="form-label required">提醒时间</label>
              <input class="form-input" type="datetime-local" v-model="form.datetime" />
            </div>

            <!-- 每天/每周：时间 -->
            <div class="form-group" v-else>
              <label class="form-label required">提醒时刻</label>
              <input class="form-input" type="time" v-model="form.time" style="width:140px" />
            </div>

            <!-- 每周：星期选择 -->
            <div class="form-group" v-if="form.repeatType === 'weekly'">
              <label class="form-label required">重复星期</label>
              <div class="weekday-picker">
                <button
                  v-for="(w, i) in weekdayNames" :key="i"
                  class="weekday-btn" :class="{ active: form.weekdays.includes(i) }"
                  @click="toggleWeekday(i)"
                >{{ w }}
                </button>
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">备注</label>
              <textarea class="form-textarea" v-model="form.desc" placeholder="提醒内容（可选）..." rows="2" />
            </div>

          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="closeForm">取消</button>
            <button class="modal-btn confirm" @click="saveForm" :disabled="!canSave">
              {{ editingId ? '保存修改' : '创建提醒' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

    <!-- ===== 弹出通知 Toast（权限被拒绝时的兜底） ===== -->
    <Transition name="toast">
      <div class="toast-notify" v-if="toastMsg">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.73 21a2 2 0 0 1-3.46 0" />
        </svg>
        <div class="toast-text">
          <strong>{{ toastMsg.title }}</strong>
          <span v-if="toastMsg.body">{{ toastMsg.body }}</span>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onBeforeUnmount } from 'vue'

/* ============================================================
   持久化
   ============================================================ */
const STORAGE_KEY = 'work-assistant:reminders'

function loadReminders() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveReminders(list) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  } catch {
  }
}

/* ============================================================
   常量
   ============================================================ */
const categories = [
  {
    key: 'work',
    label: '上班',
    color: '#409eff',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="7" width="20" height="14" rx="2"/><path d="M16 21V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v16"/></svg>`
  },
  {
    key: 'water',
    label: '喝水',
    color: '#06b6d4',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z"/></svg>`
  },
  {
    key: 'meeting',
    label: '开会',
    color: '#9b59b6',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`
  },
  {
    key: 'rest',
    label: '休息',
    color: '#16a34a',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/></svg>`
  },
  {
    key: 'other',
    label: '其他',
    color: '#909399',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></svg>`
  }
]

const categoryFilters = [
  {
    key: 'all',
    label: '全部',
    icon: `<svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/></svg>`
  },
  ...categories
]

const statusFilters = [
  { key: 'all', label: '全部', color: '#909399' },
  { key: 'enabled', label: '已启用', color: '#16a34a' },
  { key: 'disabled', label: '已停用', color: '#909399' }
]

const weekdayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

/* ============================================================
   State
   ============================================================ */
const reminders = ref(loadReminders())
const activeCategory = ref('all')
const activeStatus = ref('all')

const showForm = ref(false)
const editingId = ref(null)
const titleInputRef = ref(null)
const toastMsg = ref(null)

const form = ref({
  title: '', category: 'work', repeatType: 'once',
  datetime: '', time: '09:00', weekdays: [], desc: ''
})

/* ============================================================
   通知权限
   ============================================================ */
const notifyPermission = ref(typeof Notification !== 'undefined' ? Notification.permission : 'unsupported')

const permClass = computed(() => {
  if (notifyPermission.value === 'granted') return 'perm-granted'
  if (notifyPermission.value === 'denied') return 'perm-denied'
  return 'perm-default'
})
const permTitle = computed(() => {
  if (notifyPermission.value === 'granted') return '通知已授权'
  if (notifyPermission.value === 'denied') return '通知权限被拒绝'
  if (notifyPermission.value === 'unsupported') return '浏览器不支持通知'
  return '尚未授权通知'
})
const permSub = computed(() => {
  if (notifyPermission.value === 'granted') return '提醒将通过系统通知推送'
  if (notifyPermission.value === 'denied') return '将使用页面内弹窗作为备用提示'
  if (notifyPermission.value === 'unsupported') return '将使用页面内弹窗提示'
  return '点击授权以接收桌面通知'
})

async function requestPermission() {
  if (typeof Notification === 'undefined') return
  const result = await Notification.requestPermission()
  notifyPermission.value = result
}

/* ============================================================
   Computed
   ============================================================ */
function getCategory(key) {
  return categories.find(c => c.key === key) || categories[categories.length - 1]
}

const filteredReminders = computed(() => {
  let list = reminders.value
  if (activeCategory.value !== 'all') list = list.filter(r => r.category === activeCategory.value)
  if (activeStatus.value === 'enabled') list = list.filter(r => r.enabled)
  if (activeStatus.value === 'disabled') list = list.filter(r => !r.enabled)
  return list
})

function countByCategory(key) {
  if (key === 'all') return reminders.value.length
  return reminders.value.filter(r => r.category === key).length
}

function countByStatus(key) {
  if (key === 'all') return reminders.value.length
  if (key === 'enabled') return reminders.value.filter(r => r.enabled).length
  if (key === 'disabled') return reminders.value.filter(r => !r.enabled).length
  return 0
}

const canSave = computed(() => {
  if (!form.value.title.trim()) return false
  if (form.value.repeatType === 'once') return !!form.value.datetime
  if (form.value.repeatType === 'daily') return !!form.value.time
  if (form.value.repeatType === 'weekly') return !!form.value.time && form.value.weekdays.length > 0
  return false
})

/* ============================================================
   下次触发时间计算
   ============================================================ */
function getNextTriggerTime(r) {
  const now = new Date()

  if (r.repeatType === 'once') {
    return new Date(r.datetime)
  }

  if (r.repeatType === 'daily') {
    const [h, m] = r.time.split(':').map(Number)
    const next = new Date(now)
    next.setHours(h, m, 0, 0)
    if (next <= now) next.setDate(next.getDate() + 1)
    return next
  }

  if (r.repeatType === 'weekly') {
    const [h, m] = r.time.split(':').map(Number)
    const sortedDays = [...r.weekdays].sort()
    for (let offset = 0; offset <= 7; offset++) {
      const candidate = new Date(now)
      candidate.setDate(candidate.getDate() + offset)
      candidate.setHours(h, m, 0, 0)
      if (sortedDays.includes(candidate.getDay()) && candidate > now) {
        return candidate
      }
    }
  }
  return null
}

function getCountdownMs(r) {
  const next = getNextTriggerTime(r)
  if (!next) return Infinity
  return next.getTime() - Date.now()
}

function formatCountdown(r) {
  const ms = getCountdownMs(r)
  if (ms === Infinity || ms < 0) return '已过期'
  const totalMin = Math.floor(ms / 60000)
  const days = Math.floor(totalMin / 1440)
  const hours = Math.floor((totalMin % 1440) / 60)
  const mins = totalMin % 60
  if (days > 0) return `${days}天后`
  if (hours > 0) return `${hours}小时${mins}分后`
  return `${mins}分钟后`
}

function formatDateTime(dt) {
  if (!dt) return ''
  const d = new Date(dt)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

function formatWeekdays(days) {
  if (!days || days.length === 0) return ''
  if (days.length === 7) return '每天'
  if (days.length === 5 && [1, 2, 3, 4, 5].every(d => days.includes(d))) return '工作日'
  if (days.length === 2 && [0, 6].every(d => days.includes(d))) return '周末'
  return [...days].sort().map(d => weekdayNames[d].replace('周', '')).join('、')
}

/* ============================================================
   定时器调度
   ============================================================ */
const timers = new Map()

function scheduleReminder(r) {
  clearTimer(r.id)
  if (!r.enabled) return

  const next = getNextTriggerTime(r)
  if (!next) return

  const delay = next.getTime() - Date.now()
  if (delay < 0) {
    // 一次性且已过期：自动停用
    if (r.repeatType === 'once') {
      r.enabled = false
      saveReminders(reminders.value)
    }
    return
  }

  // 最大 setTimeout 限制约 24.8 天，超出则分段调度
  const MAX_DELAY = 2147483647
  if (delay > MAX_DELAY) {
    const id = setTimeout(() => scheduleReminder(r), MAX_DELAY)
    timers.set(r.id, id)
    return
  }

  const id = setTimeout(() => {
    fireReminder(r)
    if (r.repeatType !== 'once') {
      scheduleReminder(r) // 重新调度下一次
    } else {
      r.enabled = false
      saveReminders(reminders.value)
    }
  }, delay)

  timers.set(r.id, id)
}

function clearTimer(id) {
  if (timers.has(id)) {
    clearTimeout(timers.get(id))
    timers.delete(id)
  }
}

function rescheduleAll() {
  reminders.value.forEach(r => scheduleReminder(r))
}

/* ============================================================
   触发通知
   ============================================================ */
function fireReminder(r) {
  const title = r.title
  const body = r.desc || `${getCategory(r.category).label} 提醒`

  if (notifyPermission.value === 'granted') {
    try {
      new Notification(title, {
        body,
        icon: '/favicon.ico',
        tag: r.id
      })
    } catch {
      showToast(title, body)
    }
  } else {
    showToast(title, body)
  }
}

function showToast(title, body) {
  toastMsg.value = { title, body }
  setTimeout(() => {
    toastMsg.value = null
  }, 5000)
}

function testNotify(r) {
  fireReminder(r)
}

/* ============================================================
   操作
   ============================================================ */
function toggleEnabled(r) {
  r.enabled = !r.enabled
  saveReminders(reminders.value)
  if (r.enabled) scheduleReminder(r)
  else clearTimer(r.id)
}

function removeReminder(id) {
  clearTimer(id)
  reminders.value = reminders.value.filter(r => r.id !== id)
  saveReminders(reminders.value)
}

/* ── 表单 ── */
function openAdd() {
  editingId.value = null
  const now = new Date()
  now.setMinutes(now.getMinutes() + 5)
  const pad = n => String(n).padStart(2, '0')
  form.value = {
    title: '', category: 'work', repeatType: 'once',
    datetime: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`,
    time: '09:00', weekdays: [], desc: ''
  }
  showForm.value = true
  nextTick(() => titleInputRef.value?.focus())
}

function openEdit(r) {
  editingId.value = r.id
  form.value = {
    title: r.title, category: r.category, repeatType: r.repeatType,
    datetime: r.datetime || '', time: r.time || '09:00',
    weekdays: [...(r.weekdays || [])], desc: r.desc || ''
  }
  showForm.value = true
  nextTick(() => titleInputRef.value?.focus())
}

function closeForm() {
  showForm.value = false
  editingId.value = null
}

function toggleWeekday(day) {
  const idx = form.value.weekdays.indexOf(day)
  if (idx >= 0) form.value.weekdays.splice(idx, 1)
  else form.value.weekdays.push(day)
}

function saveForm() {
  if (!canSave.value) return

  if (editingId.value) {
    const idx = reminders.value.findIndex(r => r.id === editingId.value)
    if (idx >= 0) {
      Object.assign(reminders.value[idx], { ...form.value })
      scheduleReminder(reminders.value[idx])
    }
  } else {
    const newReminder = {
      id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
      ...form.value,
      enabled: true,
      createdAt: new Date().toISOString()
    }
    reminders.value.unshift(newReminder)
    scheduleReminder(newReminder)
  }

  saveReminders(reminders.value)
  closeForm()
}

/* ============================================================
   生命周期
   ============================================================ */
onMounted(() => {
  rescheduleAll()
  // 页面切回前台时重新校准（防止后台 timer 被节流）
  document.addEventListener('visibilitychange', onVisibilityChange)
})

function onVisibilityChange() {
  if (document.visibilityState === 'visible') {
    rescheduleAll()
  }
}

onBeforeUnmount(() => {
  timers.forEach(id => clearTimeout(id))
  timers.clear()
  document.removeEventListener('visibilitychange', onVisibilityChange)
})

watch(reminders, (v) => saveReminders(v), { deep: true })
</script>

<style lang="scss" scoped>
/* ══ 整体布局 ══ */
.reminder-layout {
  display: flex;
  height: 100%;
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
  position: relative;
}

/* ══ 左侧侧边栏 ══ */
.reminder-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--el-bg-color, #fff);
  border-right: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

.sb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 14px 10px;
  flex-shrink: 0;
}

.sb-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
}

.icon-btn-sm {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary, #909399);
  transition: all .15s;

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
  }
}

/* 权限卡片 */
.perm-card {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 10px 10px;
  padding: 10px;
  border-radius: 9px;
  border: 1.5px solid;

  &.perm-granted {
    background: #f0fdf4;
    border-color: #86efac;
    color: #16a34a;
  }

  &.perm-denied {
    background: #fff1f2;
    border-color: #fca5a5;
    color: #dc2626;
  }

  &.perm-default {
    background: #fffbeb;
    border-color: #fcd34d;
    color: #d97706;
  }
}

.perm-text {
  display: flex;
  flex-direction: column;
  gap: 1px;
  flex: 1;
  min-width: 0;
}

.perm-title {
  font-size: 11px;
  font-weight: 700;
}

.perm-sub {
  font-size: 10px;
  opacity: .8;
  line-height: 1.4;
}

.perm-btn {
  flex-shrink: 0;
  padding: 4px 10px;
  border-radius: 6px;
  border: none;
  background: #d97706;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;

  &:hover {
    background: #b45309;
  }
}

/* 筛选 */
.filter-section {
  padding: 8px 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.filter-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
  color: var(--el-text-color-placeholder, #c0c4cc);
  margin-bottom: 4px;
  padding-left: 4px;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 8px;
  border-radius: 7px;
  cursor: pointer;
  transition: background .12s;
  font-size: 12px;
  color: var(--el-text-color-regular, #606266);

  &:hover {
    background: var(--el-fill-color-lighter, #fafafa);
  }

  &.active {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
    font-weight: 600;
  }
}

.filter-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.filter-icon {
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.filter-name {
  flex: 1;
}

.filter-count {
  font-size: 10px;
  font-family: monospace;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 8px;
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-text-color-secondary, #909399);

  .active & {
    background: var(--el-color-primary-light-7, #79bbff);
    color: #fff;
  }
}

/* 统计 */
.sb-stats {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding: 14px 10px;
  margin-top: auto;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
}

.stat-num {
  font-size: 20px;
  font-weight: 700;
  font-family: monospace;
  color: var(--el-text-color-primary, #303133);

  &.enabled {
    color: #16a34a;
  }
}

.stat-lbl {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--el-border-color-lighter, #ebeef5);
}

/* ══ 右侧主区域 ══ */
.reminder-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-bg-color, #fff);
  flex-shrink: 0;
}

.result-count {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 12px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;

  &.primary {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    color: #fff;

    &:hover {
      background: var(--el-color-primary-dark-2, #337ecc);
    }
  }
}

/* 列表 */
.reminder-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

/* 提醒卡片 */
.reminder-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  border: 1.5px solid var(--el-border-color-lighter, #ebeef5);
  transition: all .15s;
  flex-shrink: 0;

  &:hover {
    border-color: var(--el-color-primary-light-5, #a0cfff);
    box-shadow: 0 2px 8px rgba(64, 158, 255, .08);
  }

  &.disabled {
    opacity: .55;
  }
}

/* 开关 */
.r-switch {
  width: 36px;
  height: 20px;
  border-radius: 10px;
  background: var(--el-border-color, #dcdfe6);
  position: relative;
  cursor: pointer;
  transition: background .2s;
  flex-shrink: 0;

  &.on {
    background: var(--el-color-success, #67c23a);
  }
}

.r-switch-dot {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0, 0, 0, .2);
  transition: transform .2s;

  .r-switch.on & {
    transform: translateX(16px);
  }
}

/* 图标 */
.r-icon {
  width: 34px;
  height: 34px;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* 主体 */
.r-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.r-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.r-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);

  &.disabled {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

.r-category-badge {
  font-size: 10px;
  font-weight: 600;
  padding: 1px 7px;
  border-radius: 6px;
}

.r-time-row {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);

  svg {
    flex-shrink: 0;
  }
}

.countdown {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 7px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);

  &.soon {
    background: #fef3c7;
    color: #d97706;
  }
}

.r-desc {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作 */
.r-actions {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity .15s;

  .reminder-card:hover & {
    opacity: 1;
  }
}

.action-icon-btn {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  border: none;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-text-color-secondary, #909399);
  transition: all .15s;

  &:hover {
    background: var(--el-fill-color, #f0f2f5);
    color: var(--el-text-color-primary, #303133);
  }

  &.danger:hover {
    background: #fff1f2;
    color: #dc2626;
  }
}

/* 空状态 */
.reminder-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
}

.empty-sub {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

/* ══ 弹窗 ══ */
.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, .35);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-box {
  width: 520px;
  max-width: 94vw;
  background: var(--el-bg-color, #fff);
  border-radius: 14px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, .2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.modal-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
}

.modal-close {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  font-size: 14px;
  color: var(--el-text-color-secondary, #909399);
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: var(--el-fill-color, #f0f2f5);
    color: var(--el-text-color-primary, #303133);
  }
}

.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  overflow-y: auto;
  max-height: 65vh;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 14px 20px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular, #606266);

  &.required::after {
    content: ' *';
    color: #dc2626;
  }
}

.form-input, .form-textarea {
  padding: 8px 10px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  font-size: 13px;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  outline: none;
  transition: border-color .15s;
  font-family: inherit;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }
}

.form-textarea {
  resize: vertical;
  min-height: 56px;
}

/* 分类选择 */
.category-picker {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
}

.category-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 12px;
  border-radius: 7px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--el-text-color-secondary, #909399);
  }

  &.active {
    font-weight: 700;
  }
}

/* 重复方式 */
.seg-ctrl {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;
  align-self: flex-start;

  button {
    padding: 6px 16px;
    border: none;
    background: transparent;
    font-size: 12px;
    font-weight: 500;
    color: var(--el-text-color-regular, #606266);
    cursor: pointer;
    transition: all .15s;
    border-right: 1px solid var(--el-border-color, #dcdfe6);

    &:last-child {
      border-right: none;
    }

    &:hover {
      background: var(--el-fill-color-light, #f5f7fa);
    }

    &.active {
      background: var(--el-color-primary, #409eff);
      color: #fff;
    }
  }
}

/* 星期选择 */
.weekday-picker {
  display: flex;
  gap: 6px;
}

.weekday-btn {
  width: 42px;
  height: 36px;
  border-radius: 8px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--el-color-primary-light-5, #a0cfff);
  }

  &.active {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    color: #fff;
  }
}

/* 按钮 */
.modal-btn {
  padding: 8px 22px;
  border-radius: 8px;
  border: none;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;

  &.cancel {
    background: var(--el-fill-color, #f0f2f5);
    color: var(--el-text-color-regular, #606266);

    &:hover {
      background: var(--el-fill-color-dark, #e6e8eb);
    }
  }

  &.confirm {
    background: var(--el-color-primary, #409eff);
    color: #fff;

    &:hover:not(:disabled) {
      background: var(--el-color-primary-dark-2, #337ecc);
    }

    &:disabled {
      opacity: .4;
      cursor: not-allowed;
    }
  }
}

.modal-enter-active, .modal-leave-active {
  transition: all .2s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;

  .modal-box {
    transform: scale(.94) translateY(-10px);
  }
}

/* ══ Toast 通知 ══ */
.toast-notify {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10000;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 16px;
  max-width: 320px;
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  box-shadow: 0 8px 24px rgba(0, 0, 0, .12);
  color: var(--el-color-primary, #409eff);
}

.toast-text {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.toast-text strong {
  font-size: 13px;
  color: var(--el-text-color-primary, #303133);
}

.toast-text span {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.toast-enter-active, .toast-leave-active {
  transition: all .25s ease;
}

.toast-enter-from, .toast-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>