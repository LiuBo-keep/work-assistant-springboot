<template>
  <div class="tool-fav-page">

    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><CollectionTag /></el-icon>
        </div>
        <div>
          <h2 class="header-title">工具收藏</h2>
          <p class="header-sub">收藏常用第三方工具网站，一键直达</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="Plus" @click="openAdd">新增收藏</el-button>
      </div>
    </div>

    <!-- ===== 主体：左侧分类 + 右侧卡片 ===== -->
    <div class="main-layout">

      <!-- 左侧分类栏 -->
      <aside class="category-side">
        <div class="side-head">
          <span class="side-title">分类</span>
          <el-tooltip content="新增分类" placement="top">
            <el-icon class="side-add" @click="openAddCategory"><Plus /></el-icon>
          </el-tooltip>
        </div>

        <div class="category-list">
          <div
            class="cat-item"
            :class="{ active: activeCategory === '' }"
            @click="activeCategory = ''"
          >
            <el-icon :size="14"><Grid /></el-icon>
            <span class="cat-name">全部</span>
            <span class="cat-count">{{ favorites.length }}</span>
          </div>
          <div
            class="cat-item"
            :class="{ active: activeCategory === '__none__' }"
            @click="activeCategory = '__none__'"
          >
            <el-icon :size="14"><Files /></el-icon>
            <span class="cat-name">未分类</span>
            <span class="cat-count">{{ uncategorizedCount }}</span>
          </div>
          <div
            v-for="cat in categories"
            :key="cat.id"
            class="cat-item"
            :class="{ active: activeCategory === cat.id }"
            @click="activeCategory = cat.id"
          >
            <el-icon :size="14"><Folder /></el-icon>
            <span class="cat-name">{{ cat.name }}</span>
            <span class="cat-count">{{ categoryCount(cat.id) }}</span>
            <el-icon
              class="cat-del"
              :size="12"
              @click.stop="removeCategory(cat)"
            ><Close /></el-icon>
          </div>
        </div>
      </aside>

      <!-- 右侧收藏区 -->
      <section class="fav-main">
        <!-- 搜索栏 -->
        <div class="fav-toolbar">
          <el-input
            v-model="keyword"
            placeholder="搜索工具名称、描述…"
            clearable
            size="default"
            :prefix-icon="Search"
            @input="noop"
          />
        </div>

        <!-- 收藏卡片网格 -->
        <div v-loading="loading" class="fav-grid-wrap">
          <div v-if="filteredFavorites.length === 0 && !loading" class="empty-state">
            <el-icon :size="42"><CollectionTag /></el-icon>
            <p class="empty-title">{{ keyword ? '未找到匹配的工具' : '还没有收藏的工具' }}</p>
            <p class="empty-sub">点击右上角「新增收藏」，把好用的网站收进来</p>
            <el-button v-if="!keyword" size="small" type="primary" :icon="Plus" @click="openAdd">
              立即添加
            </el-button>
          </div>

          <div v-else class="fav-grid">
            <div
              v-for="item in filteredFavorites"
              :key="item.id"
              class="fav-card"
              @click="openUrl(item)"
            >
              <div class="fav-card-head">
                <div class="fav-icon" :style="iconStyle(item)">
                  <img v-if="item.icon" :src="item.icon" alt="" @error="onIconError(item)" />
                  <span v-else class="fav-icon-fallback">{{ initial(item.name) }}</span>
                </div>
                <div class="fav-title-wrap">
                  <span class="fav-name" :title="item.name">{{ item.name }}</span>
                  <span class="fav-domain">{{ domain(item.url) }}</span>
                </div>
                <div class="fav-actions" @click.stop>
                  <el-tooltip content="编辑" placement="top">
                    <el-icon class="fav-act" @click="openEdit(item)"><Edit /></el-icon>
                  </el-tooltip>
                  <el-popconfirm
                    title="确认删除该收藏？"
                    confirm-button-text="删除"
                    cancel-button-text="取消"
                    confirm-button-type="danger"
                    @confirm="removeFavorite(item)"
                  >
                    <template #reference>
                      <el-icon class="fav-act danger"><Delete /></el-icon>
                    </template>
                  </el-popconfirm>
                </div>
              </div>
              <p class="fav-desc" :title="item.description">{{ item.description || '暂无描述' }}</p>
              <div class="fav-foot">
                <span class="fav-click">
                  <el-icon :size="12"><Pointer /></el-icon>
                  {{ item.clickCount || 0 }} 次
                </span>
                <span class="fav-open">
                  打开
                  <el-icon :size="12"><TopRight /></el-icon>
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>
    </div>

    <!-- ===== 收藏编辑弹窗 ===== -->
    <el-dialog
      v-model="favDialog"
      :title="favForm.id ? '编辑收藏' : '新增收藏'"
      :width="isMobile ? '92%' : '460px'"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="favForm" :label-width="isMobile ? '56px' : '72px'" label-position="left">
        <el-form-item label="名称" required>
          <el-input v-model="favForm.name" placeholder="工具名称，如 JSON 在线解析" />
        </el-form-item>
        <el-form-item label="网址" required>
          <div class="url-row">
            <el-input v-model="favForm.url" placeholder="https://example.com" />
            <el-button
              class="resolve-btn"
              :icon="MagicStick"
              :loading="resolving"
              :disabled="!favForm.url?.trim()"
              @click="autoFill"
            >
              智能填充
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="favForm.icon" placeholder="图标 URL（可选，留空自动取首字）" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="favForm.categoryId" placeholder="选择分类" clearable style="width:100%">
            <el-option
              v-for="cat in categories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="favForm.description"
            type="textarea"
            :rows="2"
            placeholder="一句话描述这个工具的用途（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="favDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveFavorite">保存</el-button>
      </template>
    </el-dialog>

    <!-- ===== 分类新增弹窗 ===== -->
    <el-dialog
      v-model="catDialog"
      title="新增分类"
      :width="isMobile ? '92%' : '360px'"
      align-center
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :label-width="isMobile ? '56px' : '60px'" label-position="left">
        <el-form-item label="名称" required>
          <el-input v-model="catForm.name" placeholder="分类名称，如 开发工具" @keyup.enter="saveCategory" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="catDialog = false">取消</el-button>
        <el-button type="primary" :loading="catSaving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import {
  Plus, Search, Edit, Delete, Close, Grid, Files, Folder,
  CollectionTag, Pointer, TopRight, MagicStick
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import toolFavoriteService from '@/services/tool/tool-favorite.service'

/* ---- 移动端检测 ---- */
const mobileQuery = window.matchMedia('(max-width: 768px)')
const isMobile = ref(mobileQuery.matches)
const onMobileChange = (e) => { isMobile.value = e.matches }

/* ---- 数据状态 ---- */
const loading = ref(false)
const saving = ref(false)
const catSaving = ref(false)
const resolving = ref(false)
const categories = ref([])
const favorites = ref([])
const activeCategory = ref('')
const keyword = ref('')

/* ---- 弹窗状态 ---- */
const favDialog = ref(false)
const catDialog = ref(false)
const favForm = ref({})
const catForm = ref({})

/* ---- 计算属性 ---- */
const uncategorizedCount = computed(() => favorites.value.filter(f => !f.categoryId).length)

const filteredFavorites = computed(() => {
  let list = favorites.value
  if (activeCategory.value === '__none__') {
    list = list.filter(f => !f.categoryId)
  } else if (activeCategory.value) {
    list = list.filter(f => f.categoryId === activeCategory.value)
  }
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(f =>
      (f.name || '').toLowerCase().includes(kw) ||
      (f.description || '').toLowerCase().includes(kw) ||
      (f.url || '').toLowerCase().includes(kw)
    )
  }
  return list
})

/* ---- 工具函数 ---- */
function categoryCount(id) {
  return favorites.value.filter(f => f.categoryId === id).length
}

function initial(name) {
  return (name || '?').trim().charAt(0).toUpperCase()
}

function domain(url) {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return url || ''
  }
}

function noop() {}

/* ---- 图标样式 ---- */
const brandColors = ['#378add', '#16a34a', '#d97706', '#dc2626', '#185fa5', '#9b59b6']
function iconStyle(item) {
  if (item.icon) return {}
  const idx = (item.name || '').length % brandColors.length
  return {
    background: brandColors[idx] + '1f',
    color: brandColors[idx]
  }
}

function onIconError(item) {
  item.icon = ''
}

/* ---- 加载数据 ---- */
async function loadData() {
  loading.value = true
  try {
    const res = await toolFavoriteService.list()
    const data = res?.data || res || {}
    categories.value = data.categories || []
    favorites.value = data.favorites || []
  } catch (e) {
    ElMessage.error(e?.error || '加载失败')
  } finally {
    loading.value = false
  }
}

/* ---- 收藏 CRUD ---- */
function openAdd() {
  favForm.value = { id: undefined, name: '', url: '', icon: '', description: '', categoryId: activeCategory.value === '__none__' ? '' : activeCategory.value }
  favDialog.value = true
}

function openEdit(item) {
  favForm.value = {
    id: item.id,
    name: item.name,
    url: item.url,
    icon: item.icon || '',
    description: item.description || '',
    categoryId: item.categoryId || ''
  }
  favDialog.value = true
}

async function autoFill() {
  const raw = favForm.value.url?.trim()
  if (!raw) return ElMessage.warning('请先输入网址')
  resolving.value = true
  try {
    const res = await toolFavoriteService.resolve(raw)
    const data = res?.data || res || {}
    if (data.name) favForm.value.name = data.name
    if (data.icon) favForm.value.icon = data.icon
    if (data.name || data.icon) {
      ElMessage.success('已自动填充')
    } else {
      ElMessage.info('未能解析到网页信息，请手动填写')
    }
  } catch (e) {
    ElMessage.error(e?.error || '解析失败')
  } finally {
    resolving.value = false
  }
}

async function saveFavorite() {
  if (!favForm.value.name?.trim()) return ElMessage.warning('请输入工具名称')
  if (!favForm.value.url?.trim()) return ElMessage.warning('请输入工具网址')
  let url = favForm.value.url.trim()
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url

  saving.value = true
  try {
    await toolFavoriteService.saveFavorite({
      ...favForm.value,
      url,
      categoryId: favForm.value.categoryId || undefined
    })
    ElMessage.success('保存成功')
    favDialog.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e?.error || '保存失败')
  } finally {
    saving.value = false
  }
}

async function removeFavorite(item) {
  try {
    await toolFavoriteService.deleteFavorite(item.id)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) {
    ElMessage.error(e?.error || '删除失败')
  }
}

function openUrl(item) {
  const url = item.url
  window.open(url, '_blank', 'noopener,noreferrer')
  toolFavoriteService.click(item.id).then(() => {
    item.clickCount = (item.clickCount || 0) + 1
  }).catch(() => {})
}

/* ---- 分类 CRUD ---- */
function openAddCategory() {
  catForm.value = { name: '' }
  catDialog.value = true
}

async function saveCategory() {
  if (!catForm.value.name?.trim()) return ElMessage.warning('请输入分类名称')
  catSaving.value = true
  try {
    await toolFavoriteService.saveCategory({ name: catForm.value.name.trim() })
    ElMessage.success('分类已添加')
    catDialog.value = false
    await loadData()
  } catch (e) {
    ElMessage.error(e?.error || '保存失败')
  } finally {
    catSaving.value = false
  }
}

async function removeCategory(cat) {
  try {
    await toolFavoriteService.deleteCategory(cat.id)
    ElMessage.success('分类已删除（其下收藏已归入未分类）')
    if (activeCategory.value === cat.id) activeCategory.value = ''
    await loadData()
  } catch (e) {
    ElMessage.error(e?.error || '删除失败')
  }
}

/* ---- 初始化 ---- */
onMounted(() => {
  mobileQuery.addEventListener('change', onMobileChange)
  loadData()
})

onBeforeUnmount(() => {
  mobileQuery.removeEventListener('change', onMobileChange)
})
</script>

<style lang="scss" scoped>
.tool-fav-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px 16px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--color-bg-page);
  overflow: hidden;
}

/* ===== 顶部标题 ===== */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-hover));
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 3px 10px rgba(55, 138, 221, 0.3);
}

.header-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
}

.header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ===== 主体布局 ===== */
.main-layout {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ===== 左侧分类 ===== */
.category-side {
  width: 200px;
  flex-shrink: 0;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid var(--color-border-light);
}

.side-title {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-text-secondary);
}

.side-add {
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.15s;

  &:hover {
    color: var(--brand-primary);
  }
}

.category-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.cat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 7px;
  cursor: pointer;
  font-size: 13px;
  color: var(--color-text-regular);
  transition: all 0.15s;

  &:hover {
    background: var(--color-bg-hover);
  }

  &.active {
    background: var(--brand-primary-fade);
    color: var(--brand-primary);
    font-weight: 600;
  }

  .cat-name {
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .cat-count {
    font-size: 11px;
    font-family: monospace;
    color: var(--color-text-placeholder);
  }

  .cat-del {
    color: var(--color-text-placeholder);
    opacity: 0;
    transition: all 0.15s;

    &:hover {
      color: var(--color-danger);
    }
  }

  &:hover .cat-del {
    opacity: 1;
  }
}

/* ===== 右侧收藏区 ===== */
.fav-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: hidden;
}

.fav-toolbar {
  flex-shrink: 0;

  .el-input {
    max-width: 360px;
  }
}

/* ===== 网址输入 + 智能填充 ===== */
.url-row {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.resolve-btn {
  flex-shrink: 0;
}

.fav-grid-wrap {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 2px 2px 8px;
}

.fav-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
}

/* ===== 收藏卡片 ===== */
.fav-card {
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  cursor: pointer;
  transition: all 0.18s;
  box-shadow: var(--shadow-sm);

  &:hover {
    border-color: var(--brand-primary);
    box-shadow: var(--shadow-md);
    transform: translateY(-2px);
  }
}

.fav-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.fav-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  font-weight: 700;
  font-size: 18px;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.fav-icon-fallback {
  font-size: 18px;
}

.fav-title-wrap {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.fav-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  display: block;
  max-width: 100%;
}

.fav-domain {
  font-size: 11px;
  color: var(--color-text-placeholder);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.fav-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  opacity: 0;
  transition: opacity 0.15s;
}

.fav-card:hover .fav-actions {
  opacity: 1;
}

.fav-act {
  font-size: 14px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.15s;

  &:hover {
    color: var(--brand-primary);
  }

  &.danger:hover {
    color: var(--color-danger);
  }
}

.fav-desc {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 38px;
}

.fav-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-light);
}

.fav-click {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--color-text-placeholder);
}

.fav-open {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 500;
  color: var(--brand-primary);
}

/* ===== 空状态 ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  height: 100%;
  min-height: 320px;
  color: var(--color-text-placeholder);

  .empty-title {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }

  .empty-sub {
    font-size: 12px;
    margin: 0;
  }
}

/* ===== 移动端适配 ===== */
@media screen and (max-width: 768px) {
  .tool-fav-page {
    padding: 12px 12px 10px;
    gap: 12px;
  }

  .page-header {
    .header-icon {
      width: 34px;
      height: 34px;
      border-radius: 8px;
    }

    .header-title {
      font-size: 15px;
    }

    .header-sub {
      display: none;
    }

    /* 新增按钮在窄屏改为纯图标，避免挤压标题 */
    .header-right .el-button {
      padding: 8px 10px;
    }
  }

  /* 左栏分类改为顶部横向通栏 */
  .main-layout {
    flex-direction: column;
    gap: 10px;
  }

  .category-side {
    width: 100%;
    flex-direction: row;
    align-items: center;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 6px 8px;
    border-radius: var(--radius-md);
    flex-shrink: 0;
  }

  .side-head {
    flex-shrink: 0;
    border-bottom: none;
    padding: 0 6px 0 0;
  }

  .category-list {
    display: flex;
    gap: 6px;
    padding: 0;
    overflow-x: auto;
    overflow-y: hidden;
    flex: 1;
  }

  .cat-item {
    flex-shrink: 0;
    padding: 6px 12px;
    white-space: nowrap;
  }

  /* 收藏卡片单列 */
  .fav-grid {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .fav-toolbar .el-input {
    max-width: 100%;
  }

  .fav-grid-wrap {
    padding: 2px 0 8px;
  }

  /* 卡片：统一间距，避免描述为空时撑高 */
  .fav-card {
    padding: 12px;
    gap: 8px;
  }

  .fav-desc {
    min-height: auto;
  }

  /* 编辑/删除按钮在移动端始终可见（无 hover），用绝对定位放到右上角，不挤压标题 */
  .fav-card {
    position: relative;
  }

  .fav-actions {
    position: absolute;
    top: 10px;
    right: 10px;
    opacity: 1;
  }

  /* 标题区给右上角按钮留出空间，避免重叠 */
  .fav-card-head {
    padding-right: 44px;
    align-items: flex-start;
  }

  /* 标题允许换行，避免长标题被硬截断 */
  .fav-name {
    white-space: normal;
    word-break: break-all;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  /* 描述为空时隐藏，避免出现孤立无意义内容 */
  .fav-desc:empty {
    display: none;
  }

  .fav-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
  }

  /* ===== 弹窗移动端适配 ===== */
  /* 网址行：智能填充按钮换行到输入框下方，避免溢出 */
  .url-row {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }

  .resolve-btn {
    width: 100%;
    margin-left: 0;
  }
}
</style>
