<template>
  <div class="todo-layout">

    <!-- ===== 左侧：筛选 & 统计 ===== -->
    <div class="todo-sidebar">

      <!-- 顶部 -->
      <div class="sb-head">
        <span class="sb-title">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 11 12 14 22 4" />
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
          </svg>
          待办任务
        </span>
        <button class="icon-btn-sm" @click="openAdd" title="新建任务">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
        </button>
      </div>

      <!-- 搜索 -->
      <div class="sb-search">
        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8" />
          <line x1="21" y1="21" x2="16.65" y2="16.65" />
        </svg>
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
          <span class="priority-flag" :style="{ color: p.color }">⚑</span>
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
            <select class="sort-select" v-model="sortBy">
              <option value="createdAt">创建时间</option>
              <option value="dueDate">截止日期</option>
              <option value="priority">优先级</option>
              <option value="title">标题</option>
            </select>
            <button class="sort-dir-btn" @click="sortDir = sortDir === 'asc' ? 'desc' : 'asc'"
                    :title="sortDir === 'asc' ? '升序' : '降序'">
              {{ sortDir === 'asc' ? '↑' : '↓' }}
            </button>
          </div>
        </div>
        <div class="toolbar-right">
          <button class="toolbar-btn" @click="clearDone" v-if="todos.some(t => t.done)" title="清除已完成">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6l-1 14H6L5 6" />
            </svg>
            清除已完成
          </button>
          <button class="toolbar-btn primary" @click="openAdd">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
              <line x1="12" y1="5" x2="12" y2="19" />
              <line x1="5" y1="12" x2="19" y2="12" />
            </svg>
            新建任务
          </button>
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
              <svg v-if="todo.done" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                   stroke-width="3">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
          </div>

          <!-- 主体 -->
          <div class="todo-body">
            <div class="todo-title-row">
              <span class="priority-flag sm" :style="{ color: getPriorityColor(todo.priority) }">⚑</span>
              <span class="todo-title" :class="{ done: todo.done }">{{ todo.title }}</span>
              <div class="todo-tags">
                <span class="tag-badge" v-for="tag in todo.tags" :key="tag">{{ tag }}</span>
              </div>
            </div>
            <div class="todo-desc" v-if="todo.desc">{{ todo.desc }}</div>
            <div class="todo-meta">
              <span v-if="todo.dueDate" class="due-date" :class="{ expired: isExpired(todo) && !todo.done }">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect
                  x="3" y="4" width="18" height="18" rx="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2"
                                                                                                          x2="8"
                                                                                                          y2="6" /><line
                  x1="3" y1="10" x2="21" y2="10" /></svg>
                {{ formatDate(todo.dueDate) }}
                <span v-if="isExpired(todo) && !todo.done" class="expired-badge">逾期</span>
              </span>
              <span class="created-at">{{ formatDate(todo.createdAt, true) }}</span>
            </div>
          </div>

          <!-- 操作 -->
          <div class="todo-actions" @click.stop>
            <button class="action-icon-btn" @click="openEdit(todo)" title="编辑">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </button>
            <button class="action-icon-btn danger" @click="removeTodo(todo.id)" title="删除">
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
      <div class="todo-empty" v-else>
        <svg width="56" height="56" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
             opacity="0.2">
          <polyline points="9 11 12 14 22 4" />
          <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
        </svg>
        <p class="empty-title">
          {{ keyword || activeStatus !== 'all' || activePriority !== 'all' ? '没有符合条件的任务' : '还没有任何任务'
          }}</p>
        <p class="empty-sub">
          {{ keyword || activeStatus !== 'all' || activePriority !== 'all' ? '换个条件试试' : '点击右上角「新建任务」开始'
          }}</p>
      </div>

    </div>

    <!-- ===== 新建/编辑 弹窗 ===== -->
    <Transition name="modal">
      <div class="modal-mask" v-if="showForm" @click.self="closeForm">
        <div class="modal-box">
          <div class="modal-header">
            <span class="modal-title">{{ editingId ? '编辑任务' : '新建任务' }}</span>
            <button class="modal-close" @click="closeForm">✕</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="form-label required">任务标题</label>
              <input class="form-input" v-model="form.title" placeholder="任务名称..." maxlength="100"
                     ref="titleInputRef" />
            </div>
            <div class="form-group">
              <label class="form-label">描述</label>
              <textarea class="form-textarea" v-model="form.desc" placeholder="任务描述（可选）..." rows="3" />
            </div>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">优先级</label>
                <div class="priority-picker">
                  <button
                    v-for="p in priorities" :key="p.key"
                    class="priority-btn" :class="{ active: form.priority === p.key }"
                    :style="form.priority === p.key ? { background: p.color + '22', borderColor: p.color, color: p.color } : {}"
                    @click="form.priority = p.key"
                  >
                    <span>⚑</span> {{ p.label }}
                  </button>
                </div>
              </div>
              <div class="form-group">
                <label class="form-label">截止日期</label>
                <input class="form-input" type="date" v-model="form.dueDate" />
              </div>
            </div>
            <div class="form-group">
              <label class="form-label">标签</label>
              <div class="tag-input-wrap">
                <span class="tag-badge removable" v-for="(tag, i) in form.tags" :key="tag">
                  {{ tag }}<button @click="form.tags.splice(i, 1)">✕</button>
                </span>
                <input
                  class="tag-input"
                  v-model="tagInput"
                  placeholder="输入标签后按 Enter"
                  @keydown.enter.prevent="addTag"
                  @keydown.backspace="tagInput === '' && form.tags.length && form.tags.pop()"
                />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn cancel" @click="closeForm">取消</button>
            <button class="modal-btn confirm" @click="saveForm" :disabled="!form.title.trim()">
              {{ editingId ? '保存修改' : '创建任务' }}
            </button>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'

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
  { key: 'high', label: '高', color: '#dc2626' },
  { key: 'medium', label: '中', color: '#d97706' },
  { key: 'low', label: '低', color: '#16a34a' }
]

const statusFilters = [
  { key: 'all', label: '全部', color: '#909399' },
  { key: 'todo', label: '待完成', color: '#409eff' },
  { key: 'done', label: '已完成', color: '#16a34a' },
  { key: 'expired', label: '已逾期', color: '#dc2626' }
]

const priorityFilters = [
  { key: 'all', label: '全部优先级', color: '#909399' },
  { key: 'high', label: '高', color: '#dc2626' },
  { key: 'medium', label: '中', color: '#d97706' },
  { key: 'low', label: '低', color: '#16a34a' }
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
  return priorities.find(p => p.key === priority)?.color || '#909399'
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
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
}

/* ══ 左侧侧边栏 ══ */
.todo-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: var(--el-bg-color, #fff);
  border-right: 1px solid var(--el-border-color-lighter, #ebeef5);
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

/* 搜索 */
.sb-search {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 10px 8px;
  padding: 6px 8px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 7px;

  &:focus-within {
    border-color: var(--el-color-primary, #409eff);
  }
}

.search-input {
  flex: 1;
  border: none;
  background: transparent;
  outline: none;
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);

  &::placeholder {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

.search-clear {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);

  &:hover {
    color: var(--el-text-color-secondary, #909399);
  }
}

/* 筛选分区 */
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
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-text-color-secondary, #909399);

  .active & {
    background: var(--el-color-primary-light-7, #79bbff);
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
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
  }

  &.active {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
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

  &.done {
    color: #16a34a;
  }

  &.expired {
    color: #dc2626;
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
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-bg-color, #fff);
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
  color: var(--el-text-color-secondary, #909399);
}

.sort-ctrl {
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-label {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
}

.sort-select {
  height: 26px;
  padding: 0 6px;
  border-radius: 5px;
  font-size: 11px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-regular, #606266);
  outline: none;
  cursor: pointer;
}

.sort-dir-btn {
  width: 24px;
  height: 24px;
  border-radius: 5px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-regular, #606266);
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
  }
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.primary {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    color: #fff;

    &:hover {
      background: var(--el-color-primary-dark-2, #337ecc);
    }
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
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  border: 1.5px solid var(--el-border-color-lighter, #ebeef5);
  cursor: pointer;
  transition: all .15s;
  flex-shrink: 0;

  &:hover {
    border-color: var(--el-color-primary-light-5, #a0cfff);
    box-shadow: 0 2px 8px rgba(64, 158, 255, .1);
  }

  &.selected {
    border-color: var(--el-color-primary, #409eff);
    box-shadow: 0 2px 12px rgba(64, 158, 255, .15);
  }

  &.done {
    opacity: .6;
  }

  &.expired:not(.done) {
    border-color: #fca5a5;
    background: #fff5f5;
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
  border: 2px solid var(--el-border-color, #dcdfe6);
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all .18s;
  cursor: pointer;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
  }

  &.checked {
    background: var(--el-color-success, #67c23a);
    border-color: var(--el-color-success, #67c23a);
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
  color: var(--el-text-color-primary, #303133);
  flex: 1;
  word-break: break-all;

  &.done {
    text-decoration: line-through;
    color: var(--el-text-color-placeholder, #c0c4cc);
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
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);

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
  color: var(--el-text-color-secondary, #909399);
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
  color: var(--el-text-color-secondary, #909399);

  &.expired {
    color: #dc2626;
    font-weight: 600;
  }
}

.expired-badge {
  padding: 0 5px;
  border-radius: 4px;
  background: #fca5a5;
  color: #7f1d1d;
  font-size: 9px;
  font-weight: 700;
}

.created-at {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
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
.todo-empty {
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
  width: 500px;
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

.form-row {
  display: flex;
  gap: 14px;

  .form-group {
    flex: 1;
  }
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
  min-height: 72px;
}

/* 优先级选择 */
.priority-picker {
  display: flex;
  gap: 7px;
}

.priority-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  padding: 6px 0;
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

/* 标签输入 */
.tag-input-wrap {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 5px;
  padding: 6px 8px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  min-height: 38px;
  cursor: text;

  &:focus-within {
    border-color: var(--el-color-primary, #409eff);
  }
}

.tag-input {
  border: none;
  outline: none;
  background: transparent;
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
  min-width: 80px;
  flex: 1;

  &::placeholder {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

/* 弹窗按钮 */
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

/* 弹窗动画 */
.modal-enter-active, .modal-leave-active {
  transition: all .2s ease;
}

.modal-enter-from, .modal-leave-to {
  opacity: 0;

  .modal-box {
    transform: scale(.94) translateY(-10px);
  }
}
</style>