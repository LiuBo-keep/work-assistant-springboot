<template>
  <div class="notes-layout">

    <!-- ===== 左侧：笔记列表 ===== -->
    <div class="notes-sidebar" :class="{ collapsed: sidebarCollapsed }">

      <!-- 顶部栏 -->
      <div class="sb-head">
        <span class="sb-title">
          <el-icon :size="14"><Notebook /></el-icon>工作笔记
        </span>
        <div class="sb-head-actions">
          <el-tooltip content="新建笔记" placement="top">
            <div class="icon-btn" @click="createNote" aria-label="新建笔记">
              <el-icon :size="14">
                <Plus />
              </el-icon>
            </div>
          </el-tooltip>
          <el-tooltip content="收起列表" placement="top">
            <div class="icon-btn" @click="sidebarCollapsed = !sidebarCollapsed" aria-label="收起列表">
              <el-icon :size="14">
                <Fold />
              </el-icon>
            </div>
          </el-tooltip>
        </div>
      </div>

      <!-- 搜索 -->
      <div class="sb-search">
        <el-input
          v-model="keyword"
          size="small"
          clearable
          placeholder="搜索笔记…"
          :prefix-icon="Search"
          @input="doSearch"
          @clear="doSearch"
        />
      </div>

      <!-- 分类标签 -->
      <div class="sb-cats">
        <span
          v-for="c in ['全部', ...categories]"
          :key="c"
          class="cat-chip"
          :class="{ active: filterCat === (c === '全部' ? '' : c) }"
          @click="filterCat = c === '全部' ? '' : doSearch()"
        >{{ c }}</span>
      </div>

      <!-- 笔记列表 -->
      <div class="sb-list">
        <div v-if="noteList.length === 0" class="sb-empty">
          <el-icon :size="24">
            <Document />
          </el-icon>
          <span>暂无笔记</span>
        </div>
        <div
          v-for="note in noteList"
          :key="note.id"
          class="note-item"
          :class="{ active: currentId === note.id }"
          @click="selectNote(note)"
        >
          <div class="ni-top">
            <span class="ni-tag" :class="`cat-${getCatKey(note.category)}`">
              {{ note.category }}
            </span>
            <span class="ni-time">{{ relativeTime(note.updatedAt) }}</span>
          </div>
          <div class="ni-title">{{ note.title }}</div>
          <div class="ni-preview">{{ preview(note.content) || '暂无内容…' }}</div>
        </div>
      </div>

      <!-- 底部统计 -->
      <div class="sb-foot">共 {{ noteList.length }} 篇笔记</div>
    </div>

    <!-- 展开侧边栏按钮（collapsed 时显示） -->
    <div v-if="sidebarCollapsed" class="sidebar-toggle" @click="sidebarCollapsed = false">
      <el-icon :size="14">
        <Expand />
      </el-icon>
    </div>

    <!-- ===== 右侧：编辑区 ===== -->
    <div class="editor-area" v-if="current">

      <!-- 编辑区头部 -->
      <div class="editor-head">
        <el-input
          v-model="current.title"
          class="title-input"
          placeholder="笔记标题…"
          @input="markDirty"
        />
        <div class="editor-actions">
          <el-select
            v-model="current.category"
            size="small"
            style="width:88px"
            @change="markDirty"
          >
            <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
          </el-select>
          <el-button size="small" :icon="View" @click="togglePreview">
            {{ previewMode ? '编辑' : '预览' }}
          </el-button>
          <el-button
            size="small"
            type="primary"
            :icon="isDirty ? DocumentChecked : Check"
            :loading="saving"
            @click="saveNote"
          >
            {{ isDirty ? '保存' : '已保存' }}
          </el-button>
          <el-popconfirm
            title="确认删除这篇笔记？"
            confirm-button-text="删除"
            cancel-button-text="取消"
            confirm-button-type="danger"
            @confirm="removeNote"
          >
            <template #reference>
              <el-button size="small" :icon="Delete" type="danger" plain />
            </template>
          </el-popconfirm>
        </div>
      </div>

      <!-- 编辑器 / 预览切换 -->
      <div class="editor-body" ref="editorBodyRef">

        <!-- Toast UI Editor -->
        <div v-show="!previewMode" ref="editorRef" class="toastui-editor-wrap" />

        <!-- Markdown 预览（只读） -->
        <div
          v-if="previewMode"
          class="md-preview toastui-editor-contents"
          v-html="renderedHtml"
        />

      </div>

      <!-- 底部元信息 -->
      <div class="editor-foot">
        <div class="foot-meta">
          <span><el-icon :size="11"><Calendar /></el-icon> 创建 {{ formatDate(current.createdAt) }}</span>
          <span><el-icon :size="11"><Clock /></el-icon> 修改 {{ relativeTime(current.updatedAt) }}</span>
          <span><el-icon :size="11"><Document /></el-icon> {{ wordCount }} 字</span>
        </div>
        <div v-if="isDirty" class="foot-unsaved">
          <el-icon :size="11">
            <Warning />
          </el-icon>
          有未保存的更改
        </div>
      </div>

    </div>

    <!-- 空状态（无选中笔记） -->
    <div v-else class="editor-empty">
      <el-icon :size="48" class="empty-icon">
        <Notebook />
      </el-icon>
      <p class="empty-title">选择或新建一篇笔记</p>
      <el-button type="primary" :icon="Plus" @click="createNote">新建笔记</el-button>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import {
  Plus, Search, Fold, Expand, View, Delete,
  Document, DocumentChecked, Check,
  Notebook, Calendar, Clock, Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import notesService from '@/services/efficiency-record/notes/Notes.service'

/* ---- Toast UI Editor ---- */
let Editor = null
let editorInst = null

/* ---- 状态 ---- */
const keyword = ref('')
const filterCat = ref('')
const noteList = ref([])
const currentId = ref(null)
const current = ref(null)   // 当前编辑的笔记副本
const isDirty = ref(false)
const saving = ref(false)
const previewMode = ref(false)
const sidebarCollapsed = ref(false)
const renderedHtml = ref('')
const editorRef = ref(null)
const editorBodyRef = ref(null)

const categories = ['工作', '开发', '会议', '其他']

/* ---- 计算属性 ---- */
const wordCount = computed(() =>
  current.value ? notesService.constructor.wordCount(current.value.content) : 0
)

/* ---- 工具函数（来自 service） ---- */
const relativeTime = notesService.constructor.relativeTime
const formatDate = notesService.constructor.formatDate
const preview = notesService.constructor.preview

/* ---- 分类 key（用于 CSS class） ---- */
function getCatKey(cat) {
  const map = { '工作': 'work', '开发': 'dev', '会议': 'meet', '其他': 'other' }
  return map[cat] || 'other'
}

/* ---- 搜索 ---- */
function doSearch() {
  noteList.value = notesService.search({
    keyword: keyword.value,
    category: filterCat.value
  })
}

/* ---- 选中笔记 ---- */
async function selectNote(note) {
  if (isDirty.value) {
    const ok = await ElMessage.warning('当前笔记有未保存的更改，切换将丢失修改') || true
    if (!ok) return
  }
  currentId.value = note.id
  current.value = { ...note }
  isDirty.value = false
  previewMode.value = false
  await nextTick()
  initEditor(note.content)
}

/* ---- 新建笔记 ---- */
async function createNote() {
  const res = notesService.add({
    title: '无标题',
    content: '',
    category: '其他'
  })
  if (res.code !== 200) return
  doSearch()
  await selectNote(res.data)
  // 自动聚焦标题
  await nextTick()
  document.querySelector('.title-input input')?.select()
}

/* ---- 保存笔记 ---- */
async function saveNote() {
  if (!current.value) return
  saving.value = true
  // 从编辑器同步最新内容
  if (editorInst && !previewMode.value) {
    current.value.content = editorInst.getMarkdown()
  }
  const res = notesService.update(current.value.id, {
    title: current.value.title,
    content: current.value.content,
    category: current.value.category
  })
  saving.value = false
  if (res.code !== 200) return ElMessage.error(res.error || '保存失败')
  isDirty.value = false
  // 更新列表中的数据
  current.value = { ...res.data }
  doSearch()
}

/* ---- 删除笔记 ---- */
function removeNote() {
  if (!current.value) return
  notesService.remove(current.value.id)
  ElMessage.success('已删除')
  current.value = null
  currentId.value = null
  isDirty.value = false
  doSearch()
  destroyEditor()
}

/* ---- 标记已修改 ---- */
function markDirty() {
  isDirty.value = true
}

/* ---- 切换预览模式 ---- */
async function togglePreview() {
  if (!previewMode.value) {
    // 切换到预览：同步内容 → 渲染 HTML
    if (editorInst) {
      current.value.content = editorInst.getMarkdown()
    }
    if (Editor) {
      renderedHtml.value = Editor.factory({
        el: document.createElement('div'),
        initialValue: current.value.content,
        viewer: true
      })?.getHTML?.() || ''
      // 用 Viewer 渲染
      const viewer = Editor.factory({
        el: document.createElement('div'),
        viewer: true,
        initialValue: current.value.content
      })
      renderedHtml.value = viewer?.getHTML?.() || markdownToHtml(current.value.content)
    }
  } else {
    // 切换到编辑
    await nextTick()
    initEditor(current.value.content)
  }
  previewMode.value = !previewMode.value
}

/* ---- 简易 Markdown 转 HTML（预览降级方案） ---- */
function markdownToHtml(md) {
  return md
    .replace(/```([\s\S]*?)```/g, '<pre><code>$1</code></pre>')
    .replace(/`([^`]+)`/g, '<code>$1</code>')
    .replace(/^#{3}\s(.+)$/gm, '<h3>$1</h3>')
    .replace(/^#{2}\s(.+)$/gm, '<h2>$1</h2>')
    .replace(/^#{1}\s(.+)$/gm, '<h1>$1</h1>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/\*([^*]+)\*/g, '<em>$1</em>')
    .replace(/^[-*]\s(.+)$/gm, '<li>$1</li>')
    .replace(/(<li>.*<\/li>)/gs, '<ul>$1</ul>')
    .replace(/\n\n/g, '</p><p>')
    .replace(/^(?!<[hup])(.+)$/gm, '<p>$1</p>')
}

/* ---- 初始化 Toast UI Editor ---- */
async function initEditor(initialValue = '') {
  if (!editorRef.value) return

  // 清除旧实例
  destroyEditor()

  try {
    if (!Editor) {
      const mod = await import('@toast-ui/editor')
      Editor = mod.default
    }
    editorInst = new Editor({
      el: editorRef.value,
      height: '100%',
      initialValue: initialValue || '',
      initialEditType: 'markdown',
      previewStyle: 'vertical',
      language: 'zh-CN',
      toolbarItems: [
        ['heading', 'bold', 'italic', 'strike'],
        ['hr', 'quote'],
        ['ul', 'ol', 'task'],
        ['table', 'link'],
        ['code', 'codeblock']
      ],
      events: {
        change: () => markDirty()
      }
    })
  } catch (e) {
    console.warn('[Notes] Toast UI Editor 初始化失败', e)
  }
}

function destroyEditor() {
  if (editorInst) {
    try {
      editorInst.destroy()
    } catch {
    }
    editorInst = null
  }
}

/* ---- 快捷键 Ctrl+S 保存 ---- */
function onKeyDown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 's') {
    e.preventDefault()
    if (isDirty.value) saveNote()
  }
}

/* ---- 离开前提示未保存 ---- */
function onBeforeUnload(e) {
  if (isDirty.value) {
    e.preventDefault()
    e.returnValue = ''
  }
}

/* ---- 生命周期 ---- */
onMounted(() => {
  doSearch()
  window.addEventListener('keydown', onKeyDown)
  window.addEventListener('beforeunload', onBeforeUnload)
})

onBeforeUnmount(() => {
  destroyEditor()
  window.removeEventListener('keydown', onKeyDown)
  window.removeEventListener('beforeunload', onBeforeUnload)
})
</script>

<style lang="scss" scoped>
/* ===== 整体布局 ===== */
.notes-layout {
  display: flex;
  height: 100%;
  min-height: calc(100vh - 56px);
  background: #f5f7fa;
  overflow: hidden;
  position: relative;
}

/* ===== 左侧列表 ===== */
.notes-sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  transition: width 0.2s ease, opacity 0.2s ease;
  overflow: hidden;

  &.collapsed {
    width: 0;
    opacity: 0;
    pointer-events: none;
  }
}

.sb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.sb-title {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;
  white-space: nowrap;

  .el-icon {
    color: #909399;
  }
}

.sb-head-actions {
  display: flex;
  align-items: center;
  gap: 2px;
}

.icon-btn {
  width: 24px;
  height: 24px;
  border-radius: 5px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: #f0f2f5;
    color: #303133;
  }
}

.sb-search {
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.sb-cats {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  padding: 6px 10px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.cat-chip {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 11px;
  font-weight: 500;
  cursor: pointer;
  border: 0.5px solid #ebeef5;
  color: #909399;
  background: #fff;
  white-space: nowrap;
  transition: all 0.15s;

  &:hover {
    border-color: #c6e2ff;
    color: #409eff;
  }

  &.active {
    background: #ecf5ff;
    color: #409eff;
    border-color: #b3d8ff;
  }
}

.sb-list {
  flex: 1;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ebeef5;
    border-radius: 2px;
  }
}

.sb-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 32px 0;
  color: #c0c4cc;
  font-size: 12px;
}

.note-item {
  padding: 9px 12px;
  cursor: pointer;
  border-left: 2px solid transparent;
  transition: background 0.12s;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;
    border-left-color: #409eff;
  }
}

.ni-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 3px;
}

.ni-tag {
  padding: 1px 5px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 500;

  &.cat-work {
    background: #E6F1FB;
    color: #0C447C;
  }

  &.cat-dev {
    background: #EEEDFE;
    color: #534AB7;
  }

  &.cat-meet {
    background: #E1F5EE;
    color: #085041;
  }

  &.cat-other {
    background: #f1f1f1;
    color: #909399;
  }
}

.ni-time {
  font-size: 10px;
  color: #c0c4cc;
}

.ni-title {
  font-size: 12px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 3px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ni-preview {
  font-size: 11px;
  color: #c0c4cc;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sb-foot {
  padding: 6px 12px;
  font-size: 11px;
  color: #c0c4cc;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

/* 展开按钮 */
.sidebar-toggle {
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 48px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-left: none;
  border-radius: 0 6px 6px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  z-index: 10;

  &:hover {
    color: #409eff;
  }
}

/* ===== 编辑区 ===== */
.editor-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

.editor-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.title-input {
  flex: 1;

  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    border: none;
    padding: 0;
  }

  :deep(.el-input__inner) {
    font-size: 16px;
    font-weight: 500;
    color: #303133;
  }
}

.editor-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.editor-body {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.toastui-editor-wrap {
  flex: 1;
  height: 100%;

  :deep(.toastui-editor-defaultUI) {
    border: none !important;
    height: 100%;
  }

  :deep(.toastui-editor-toolbar) {
    border-bottom: 1px solid #f0f0f0;
  }

  :deep(.toastui-editor-main) {
    height: calc(100% - 48px);
  }
}

.md-preview {
  flex: 1;
  padding: 20px 28px;
  overflow-y: auto;
  font-size: 14px;
  line-height: 1.8;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ebeef5;
    border-radius: 2px;
  }
}

.editor-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 14px;
  border-top: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.foot-meta {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 11px;
  color: #c0c4cc;

  span {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }
}

.foot-unsaved {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: #e6a23c;
}

/* ===== 空状态 ===== */
.editor-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: #fff;

  .empty-icon {
    color: #dcdfe6;
  }

  .empty-title {
    font-size: 14px;
    color: #c0c4cc;
  }
}
</style>