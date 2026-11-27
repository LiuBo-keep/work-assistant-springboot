<template>
  <div class="todo-layout">

    <!-- ===== 左侧：筛选 & 统计 ===== -->
    <div class="todo-sidebar">

      <!-- 顶部 -->
      <div class="sb-head">
        <span class="sb-title">
          <el-icon :size="14"><Checked /></el-icon>待办任务
        </span>
        <el-tooltip content="新建任务" placement="top">
          <button class="icon-btn-sm" @click="openAdd">
            <el-icon :size="14"><Plus /></el-icon>
          </button>
        </el-tooltip>
      </div>

      <!-- 搜索 -->
      <div class="sb-search">
        <el-icon :size="13"><Search /></el-icon>
        <input class="search-input" v-model="keyword" placeholder="搜索任务..." />
        <button v-if="keyword" class="search-clear" @click="keyword = ''">✕</button>
      </div>

      <!-- 状态分组 -->
      <div class="filter-section">
        <div class="filter-label">状态</div>
        <div
          v-for="f in statusFilters" :key="f.key"
          class="filter-item" :class="{ active: activeStatus === f.key }"
          @click="activeStatus = f.key"
        >
          <span class="filter-dot" :style="{ background: f.color }" />
          <span class="filter-name">{{ f.label }}</span>
          <span class="filter-count">{{ countByStatus(f.key) }}</span>
        </div>
      </div>

      <!-- 优先级筛选 -->
      <div class="filter-section">
        <div class="filter-label">优先级</div>
        <div
          v-for="p in priorityFilters" :key="p.key"
          class="filter-item" :class="{ active: activePriority === p.key }"
          @click="activePriority = p.key"
        >
          <el-icon :size="13" class="priority-flag" :style="{ color: p.color }"><Top /></el-icon>
          <span class="filter-name">{{ p.label }}</span>
          <span class="filter-count">{{ countByPriority(p.key) }}</span>
        </div>
      </div>

      <!-- 标签筛选 -->
      <div class="filter-section" v-if="allTags.length">
        <div class="filter-label">标签</div>
        <div class="tag-filter-list">
          <span
            v-for="tag in allTags" :key="tag"
            class="tag-chip" :class="{ active: activeTag === tag }"
            @click="activeTag = activeTag === tag ? '' : tag"
          >{{ tag }}</span>
        </div>
      </div>

      <!-- 统计 -->
      <div class="sb-stats">
        <div class="stat-item">
          <span class="stat-num">{{ todos.filter(t => !t.done).length }}</span>
          <span class="stat-lbl">待完成</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num done">{{ todos.filter(t => t.done).length }}</span>
          <span class="stat-lbl">已完成</span>
        </div>
        <div class="stat-divider" />
        <div class="stat-item">
          <span class="stat-num expired">{{ expiredCount }}</span>
          <span class="stat-lbl">已逾期</span>
        </div>
      </div>

    </div>

    <!-- ===== 右侧：任务列表 & 详情 ===== -->
    <div class="todo-main">

      <!-- 工具栏 -->
      <div class="main-toolbar">
        <div class="toolbar-left">
          <span class="result-count">{{ filteredTodos.length }} 个任务</span>
          <div class="sort-ctrl">
            <label class="sort-label">排序</label>
            <el-select v-model="sortBy" class="sort-select" size="small">
              <el-option label="创建时间" value="createdAt" />
              <el-option label="截止日期" value="dueDate" />
              <el-option label="优先级" value="priority" />
              <el-option label="标题" value="title" />
            </el-select>
            <el-button
              class="sort-dir-btn"
              size="small"
              circle
              @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'"
              :title="sortDir === 'asc' ? '升序' : '降序'"
            >
              <el-icon><Top v-if="sortDir === 'asc'" /><Bottom v-else /></el-icon>
            </el-button>
          </div>
        </div>
        <div class="toolbar-right">
          <el-button size="small" :icon="Delete" v-if="todos.some(t => t.done)" @click="clearDone">
            清除已完成
          </el-button>
          <el-button size="small" type="primary" :icon="Plus" @click="openAdd">新建任务</el-button>
        </div>
      </div>

      <!-- 任务列表 -->
      <div class="todo-list" v-if="filteredTodos.length">
        <div
          v-for="todo in filteredTodos" :key="todo.id"
          class="todo-item"
          :class="{ done: todo.done, expired: isExpired(todo), selected: selectedId === todo.id }"
          @click="selectedId = todo.id"
        >
          <!-- 完成勾选 -->
          <div class="todo-check" @click.stop="toggleDone(todo)">
            <div class="check-circle" :class="{ checked: todo.done }">
              <el-icon v-if="todo.done" :size="11"><Check /></el-icon>
            </div>
          </div>

          <!-- 主体 -->
          <div class="todo-body">
            <div class="todo-title-row">
              <el-icon :size="13" class="priority-flag sm" :style="{ color: getPriorityColor(todo.priority) }"><Top /></el-icon>
              <span class="todo-title" :class="{ done: todo.done }">{{ todo.title }}</span>
              <div class="todo-tags">
                <span class="tag-badge" v-for="tag in todo.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
            <div class="todo-desc" v-if="todo.desc">{{ todo.desc }}</div>
            <div class="todo-meta">
              <span v-if="todo.dueDate" class="due-date" :class="{ expired: isExpired(todo) && !todo.done }">
                <el-icon :size="11"><Calendar /></el-icon>
                {{ formatDate(todo.dueDate) }}
                <span v-if="isExpired(todo) && !todo.done" class="expired-badge">逾期</span>
              </span>
              <span class="created-at">{{ formatDate(todo.createdAt, true) }}</span>
            </div>
          </div>

          <!-- 操作 -->
          <div class="todo-actions" @click.stop>
            <el-tooltip content="编辑" placement="top">
              <button class="action-icon-btn" @click="openEdit(todo)">
                <el-icon :size="13"><Edit /></el-icon>
              </button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <button class="action-icon-btn danger" @click="removeTodo(todo.id)">
                <el-icon :size="13"><Delete /></el-icon>
              </button>
            </el-tooltip>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="todo-empty" v-else>
        <el-icon :size="56" class="empty-svg"><Checked /></el-icon>
        <p class="empty-title">
          {{ keyword || activeStatus !== 'all' || activePriority !== 'all' ? '没有符合条件的任务' : '还没有任何任务'
          }}</p>
        <p class="empty-sub">
          {{ keyword || activeStatus !== 'all' || activePriority !== 'all' ? '换个条件试试' : '点击右上角「新建任务」开始'
          }}</p>
      </div>

    </div>

    <!-- ===== 新建/编辑 弹窗 ===== -->
    <el-dialog
      v-model="showForm"
      :title="editingId ? '编辑任务' : '新建任务'"
      width="500px"
      :close-on-click-modal="false"
      @closed="onDialogClosed"
    >
      <el-form label-position="top">
        <el-form-item label="任务标题" required>
          <el-input v-model="form.title" placeholder="任务名称..." maxlength="100" ref="titleInputRef" @keydown.enter="saveForm" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.desc" type="textarea" :rows="3" placeholder="任务描述（可选）..." />
        </el-form-item>
        <div class="form-row">
          <el-form-item label="优先级" class="form-col">
            <div class="priority-picker">
              <button
                v-for="p in priorities" :key="p.key"
                class="priority-btn" :class="{ active: form.priority === p.key }"
                :style="form.priority === p.key ? { background: p.soft, borderColor: p.color, color: p.color } : {}"
                @click="form.priority = p.key"
              >
                <el-icon :size="13"><Top /></el-icon> {{ p.label }}
              </button>
            </div>
          </el-form-item>
          <el-form-item label="截止日期" class="form-col">
            <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
          </el-form-item>
        </div>
        <el-form-item label="标签">
          <div class="tag-input-wrap">
            <el-tag
              v-for="(tag, i) in form.tags" :key="tag"
              closable size="small" @close="form.tags.splice(i, 1)"
            >{{ tag }}</el-tag>
            <input
              class="tag-input"
              v-model="tagInput"
              placeholder="输入标签后按 Enter"
              @keydown.enter.prevent="addTag"
              @keydown.backspace="tagInput === '' && form.tags.length && form.tags.pop()"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" :disabled="!form.title.trim()" @click="saveForm">
          {{ editingId ? '保存修改' : '创建任务' }}
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { Plus, Search, Check, Checked, Delete, Edit, Top, Bottom, Calendar } from '@element-plus/icons-vue'

/* ============================================================
   持久化：localStorage
   ============================================================ */
const STORAGE_KEY = 'work-assistant:todos'

function loadTodos() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : []
  } catch {
    return []
  }
}

function saveTodos(list) {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
  } catch {
  }
}

/* ============================================================
   状态
   ============================================================ */
const todos = ref(loadTodos())
const keyword = ref('')
const activeStatus = ref('all')
const activePriority = ref('all')
const activeTag = ref('')
const sortBy = ref('createdAt')
const sortDir = ref('desc')
const selectedId = ref(null)

// 表单
const showForm = ref(false)
const editingId = ref(null)
const tagInput = ref('')
const titleInputRef = ref(null)
const form = ref({ title: '', desc: '', priority: 'medium', dueDate: '', tags: [] })

/* ============================================================
   常量
   ============================================================ */
const priorities = [
  { key: 'high', label: '高', color: 'var(--color-danger)', soft: 'var(--color-danger-soft)' },
  { key: 'medium', label: '中', color: 'var(--color-warning)', soft: 'var(--color-warning-soft)' },
  { key: 'low', label: '低', color: 'var(--color-success)', soft: 'var(--color-success-soft)' }
]

const statusFilters = [
  { key: 'all', label: '全部', color: 'var(--color-text-secondary)' },
  { key: 'todo', label: '待完成', color: 'var(--brand-primary)' },
  { key: 'done', label: '已完成', color: 'var(--color-success)' },
  { key: 'expired', label: '已逾期', color: 'var(--color-danger)' }
]

const priorityFilters = [
  { key: 'all', label: '全部优先级', color: 'var(--color-text-secondary)' },
  { key: 'high', label: '高', color: 'var(--color-danger)' },
  { key: 'medium', label: '中', color: 'var(--color-warning)' },
  { key: 'low', label: '低', color: 'var(--color-success)' }
]

/* ============================================================
   Computed
   ============================================================ */
const allTags = computed(() => {
  const set = new Set()
  todos.value.forEach(t => t.tags?.forEach(tag => set.add(tag)))
  return [...set]
})

const expiredCount = computed(() =>
  todos.value.filter(t => !t.done && isExpired(t)).length
)

const filteredTodos = computed(() => {
  let list = todos.value

  // 关键词
  if (keyword.value) {
    const kw = keyword.value.toLowerCase()
    list = list.filter(t => t.title.toLowerCase().includes(kw) || t.desc?.toLowerCase().includes(kw))
  }

  // 状态
  if (activeStatus.value === 'todo') list = list.filter(t => !t.done && !isExpired(t))
  if (activeStatus.value === 'done') list = list.filter(t => t.done)
  if (activeStatus.value === 'expired') list = list.filter(t => !t.done && isExpired(t))

  // 优先级
  if (activePriority.value !== 'all') list = list.filter(t => t.priority === activePriority.value)

  // 标签
  if (activeTag.value) list = list.filter(t => t.tags?.includes(activeTag.value))

  // 排序
  const dir = sortDir.value === 'asc' ? 1 : -1
  list = [...list].sort((a, b) => {
    if (sortBy.value === 'priority') {
      const order = { high: 0, medium: 1, low: 2 }
      return dir * (order[a.priority] - order[b.priority])
    }
    if (sortBy.value === 'dueDate') {
      if (!a.dueDate && !b.dueDate) return 0
      if (!a.dueDate) return 1
      if (!b.dueDate) return -1
      return dir * a.dueDate.localeCompare(b.dueDate)
    }
    if (sortBy.value === 'title') return dir * a.title.localeCompare(b.title, 'zh')
    return dir * (new Date(a.createdAt) - new Date(b.createdAt))
  })

  return list
})

/* ============================================================
   工具函数
   ============================================================ */
function isExpired(todo) {
  if (!todo.dueDate || todo.done) return false
  return new Date(todo.dueDate) < new Date(new Date().toDateString())
}

function formatDate(dateStr, relative = false) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (relative) {
    const now = new Date()
    const diff = Math.floor((now - d) / 86400000)
    if (diff === 0) return '今天'
    if (diff === 1) return '昨天'
    if (diff < 7) return `${diff}天前`
  }
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function getPriorityColor(priority) {
  return priorities.find(p => p.key === priority)?.color || 'var(--color-text-secondary)'
}

function countByStatus(key) {
  if (key === 'all') return todos.value.length
  if (key === 'todo') return todos.value.filter(t => !t.done && !isExpired(t)).length
  if (key === 'done') return todos.value.filter(t => t.done).length
  if (key === 'expired') return todos.value.filter(t => !t.done && isExpired(t)).length
  return 0
}

function countByPriority(key) {
  if (key === 'all') return todos.value.length
  return todos.value.filter(t => t.priority === key).length
}

/* ============================================================
   操作
   ============================================================ */
function toggleDone(todo) {
  todo.done = !todo.done
  todo.doneAt = todo.done ? new Date().toISOString() : null
  saveTodos(todos.value)
}

function removeTodo(id) {
  todos.value = todos.value.filter(t => t.id !== id)
  if (selectedId.value === id) selectedId.value = null
  saveTodos(todos.value)
}

function clearDone() {
  todos.value = todos.value.filter(t => !t.done)
  saveTodos(todos.value)
}

/* ── 表单 ── */
function openAdd() {
  editingId.value = null
  form.value = { title: '', desc: '', priority: 'medium', dueDate: '', tags: [] }
  tagInput.value = ''
  showForm.value = true
  nextTick(() => titleInputRef.value?.focus())
}

function openEdit(todo) {
  editingId.value = todo.id
  form.value = {
    title: todo.title,
    desc: todo.desc || '',
    priority: todo.priority,
    dueDate: todo.dueDate || '',
    tags: [...(todo.tags || [])]
  }
  tagInput.value = ''
  showForm.value = true
  nextTick(() => titleInputRef.value?.focus())
}

function closeForm() {
  showForm.value = false
  editingId.value = null
}

function onDialogClosed() {
  editingId.value = null
  tagInput.value = ''
}

function addTag() {
  const t = tagInput.value.trim()
  if (t && !form.value.tags.includes(t)) form.value.tags.push(t)
  tagInput.value = ''
}

function saveForm() {
  if (!form.value.title.trim()) return
  if (tagInput.value.trim()) addTag()

  if (editingId.value) {
    const idx = todos.value.findIndex(t => t.id === editingId.value)
    if (idx >= 0) Object.assign(todos.value[idx], { ...form.value, updatedAt: new Date().toISOString() })
  } else {
    todos.value.unshift({
      id: Date.now().toString(36) + Math.random().toString(36).slice(2, 6),
      ...form.value,
      done: false,
      doneAt: null,
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    })
  }
  saveTodos(todos.value)
  closeForm()
}

// 监听变化自动保存
watch(todos, (v) => saveTodos(v), { deep: true })
</script>

<style lang="scss" scoped>
/* ══ 整体布局 ══ */
.todo-layout {
  display: flex;
  height: 100%;
  background: var(--color-bg-page);
  overflow: hidden;
}

/* ══ 左侧侧边栏 ══ */
.todo-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--color-bg-card);
  border-right: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  gap: 0;

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
  color: var(--color-text-primary);
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
  color: var(--color-text-secondary);
  transition: all .15s;

  &:hover {
    background: var(--brand-primary-fade);
    color: var(--brand-primary);
  }
}

/* 搜索 */
.sb-search {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 10px 8px;
  padding: 6px 8px;
  background: var(--color-bg-hover);
  border: 1px solid var(--color-border-light);
  border-radius: 7px;

  &:focus-within {
    border-color: var(--brand-primary);
  }
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 12px;
  color: var(--color-text-primary);

  &::placeholder {
    color: var(--color-text-placeholder);
  }
}

.search-clear {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 11px;
  color: var(--color-text-placeholder);

  &:hover {
    color: var(--color-text-secondary);
  }
}

/* 筛选分区 */
.filter-section {
  padding: 8px 10px;
  border-bottom: 1px solid var(--color-border-light);
}

.filter-label {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
  color: var(--color-text-placeholder);
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
  color: var(--color-text-regular);

  &:hover {
    background: var(--color-bg-hover);
  }

  &.active {
    background: var(--brand-primary-fade);
    color: var(--brand-primary);
    font-weight: 600;
  }
}

.filter-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  flex-shrink: 0;
}

.priority-flag {
  font-size: 13px;
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
  background: var(--color-bg-hover);
  color: var(--color-text-secondary);

  .active & {
    background: var(--brand-primary);
    color: #fff;
  }
}

/* 标签筛选 */
.tag-filter-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  padding: 4px 0;
}

.tag-chip {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  border: 1px solid var(--color-border);
  background: var(--color-bg-card);
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--brand-primary);
    color: var(--brand-primary);
  }

  &.active {
    background: var(--brand-primary);
    border-color: var(--brand-primary);
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
  border-top: 1px solid var(--color-border-light);
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
  color: var(--color-text-primary);

  &.done {
    color: var(--color-success);
  }

  &.expired {
    color: var(--color-danger);
  }
}

.stat-lbl {
  font-size: 10px;
  color: var(--color-text-placeholder);
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: var(--color-border-light);
}

/* ══ 右侧主区域 ══ */
.todo-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 工具栏 */
.main-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-bottom: 1px solid var(--color-border-light);
  background: var(--color-bg-card);
  flex-shrink: 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.result-count {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.sort-ctrl {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-label {
  font-size: 11px;
  color: var(--color-text-secondary);
}

.sort-select {
  width: 110px;
}

.sort-dir-btn {
  border: 1px solid var(--color-border);
  background: transparent;
  color: var(--color-text-regular);

  &:hover {
    border-color: var(--brand-primary);
    color: var(--brand-primary);
  }
}

/* 任务列表 */
.todo-list {
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

/* 任务卡片 */
.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  background: var(--color-bg-card);
  border-radius: 10px;
  border: 1.5px solid var(--color-border-light);
  cursor: pointer;
  transition: all .15s;
  flex-shrink: 0;

  &:hover {
    border-color: var(--brand-primary-soft);
    box-shadow: var(--shadow-sm);
  }

  &.selected {
    border-color: var(--brand-primary);
    box-shadow: var(--shadow-md);
  }

  &.done {
    opacity: .6;
  }

  &.expired:not(.done) {
    border-color: var(--color-danger-soft);
    background: var(--color-danger-soft);
  }
}

/* 勾选圈 */
.todo-check {
  flex-shrink: 0;
  padding-top: 2px;
}

.check-circle {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  border: 2px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .18s;
  cursor: pointer;

  &:hover {
    border-color: var(--brand-primary);
  }

  &.checked {
    background: var(--color-success);
    border-color: var(--color-success);
    color: #fff;
  }
}

/* 任务主体 */
.todo-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.todo-title-row {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.priority-flag.sm {
  font-size: 12px;
  flex-shrink: 0;
}

.todo-title {
  font-size: 13px;
  font-weight: 500;
  color: var(--color-text-primary);
  flex: 1;
  word-break: break-all;

  &.done {
    text-decoration: line-through;
    color: var(--color-text-placeholder);
  }
}

.todo-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-badge {
  font-size: 10px;
  padding: 1px 7px;
  border-radius: 8px;
  background: var(--brand-primary-fade);
  color: var(--brand-primary);

  &.removable {
    display: flex;
    align-items: center;
    gap: 3px;

    button {
      border: none;
      background: transparent;
      cursor: pointer;
      font-size: 9px;
      padding: 0;
      color: inherit;
      line-height: 1;
    }
  }
}

.todo-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-secondary);

  &.expired {
    color: var(--color-danger);
    font-weight: 600;
  }
}

.expired-badge {
  padding: 0 5px;
  border-radius: 4px;
  background: var(--color-danger);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
}

.created-at {
  font-size: 11px;
  color: var(--color-text-placeholder);
}

/* 操作按钮 */
.todo-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity .15s;

  .todo-item:hover & {
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
  color: var(--color-text-secondary);
  transition: all .15s;

  &:hover {
    background: var(--color-bg-hover);
    color: var(--color-text-primary);
  }

  &.danger:hover {
    background: var(--color-danger-soft);
    color: var(--color-danger);
  }
}

/* 空状态 */
.todo-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.empty-svg {
  color: var(--color-border);
  opacity: .5;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.empty-sub {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-placeholder);
}

/* ══ 弹窗表单 ══ */
.form-row {
  display: flex;
  gap: 14px;

  .form-col {
    flex: 1;
  }
}

/* 优先级选择 */
.priority-picker {
  display: flex;
  gap: 7px;
  width: 100%;
}

.priority-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 6px 0;
  border-radius: 7px;
  border: 1.5px solid var(--color-border);
  background: transparent;
  font-size: 12px;
  font-weight: 500;
  color: var(--color-text-regular);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--color-text-secondary);
  }

  &.active {
    font-weight: 700;
  }
}

/* 标签输入 */
.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
  padding: 6px 8px;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  min-height: 32px;
  cursor: text;
  width: 100%;
  box-sizing: border-box;

  &:focus-within {
    border-color: var(--brand-primary);
  }
}

.tag-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 12px;
  color: var(--color-text-primary);
  min-width: 80px;
  flex: 1;

  &::placeholder {
    color: var(--color-text-placeholder);
  }
}

/* ══ 移动端适配 ══ */
@media screen and (max-width: 768px) {
  .todo-layout {
    flex-direction: column;
  }

  .todo-sidebar {
    width: 100%;
    flex-shrink: 0;
    max-height: 200px;
    border-right: none;
    border-bottom: 1px solid var(--color-border-light);
    overflow-x: auto;
    overflow-y: hidden;
  }

  .todo-main {
    flex: 1;
    min-height: 0;
  }

  .main-toolbar {
    padding: 10px 12px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .todo-list {
    padding: 10px;
  }

  .todo-item {
    padding: 10px 12px;
  }
}
</style>