<template>
  <div class="page-wrap">

    <!-- 锁屏层：未解锁时显示 -->
    <div v-if="!unlocked" class="lock-screen">
      <div class="lock-card">
        <div class="lock-icon">
          <el-icon :size="32">
            <Lock />
          </el-icon>
        </div>
        <h3 class="lock-title">{{ hasVault ? '密码库已锁定' : '设置主密码' }}</h3>
        <p class="lock-desc">
          {{ hasVault ? '输入主密码解锁密码库' : '首次使用，请设置一个主密码来保护你的密码库' }}
        </p>

        <el-form @submit.prevent="handleUnlock" style="width:100%">
          <el-form-item>
            <el-input
              v-model="masterPwd"
              type="password"
              show-password
              placeholder="输入主密码"
              size="default"
              :prefix-icon="Key"
              v-focus
              @keydown.enter="handleUnlock"
            />
          </el-form-item>

          <!-- 首次设置：确认主密码 -->
          <el-form-item v-if="!hasVault">
            <el-input
              v-model="masterPwdConfirm"
              type="password"
              show-password
              placeholder="再次确认主密码"
              size="default"
              :prefix-icon="Key"
              @keydown.enter="handleUnlock"
            />
          </el-form-item>

          <el-button
            type="primary"
            style="width:100%"
            size="default"
            :loading="unlocking"
            @click="handleUnlock"
          >
            {{ hasVault ? '解锁' : '创建密码库' }}
          </el-button>
        </el-form>

        <!-- 导入备份（已有 vault 时显示） -->
        <div v-if="hasVault" class="lock-footer">
          <span class="lock-link" @click="showImport = true">从备份文件恢复</span>
        </div>
      </div>
    </div>

    <!-- 已解锁：密码列表 -->
    <template v-else>

      <!-- ① 搜索区 -->
      <div class="search-card">
        <div class="search-grid">
          <div class="search-field">
            <span class="field-label">关键词</span>
            <el-input
              v-model="keyword"
              clearable
              placeholder="搜索名称、账号、备注…"
              size="small"
              :prefix-icon="Search"
              @input="doSearch"
              @clear="doSearch"
            />
          </div>
          <div class="search-field">
            <span class="field-label">分类</span>
            <el-select
              v-model="filterCategory"
              clearable
              placeholder="全部分类"
              size="small"
              style="width:100%"
              @change="doSearch"
            >
              <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
            </el-select>
          </div>
        </div>
        <div class="search-footer">
          <el-button size="small" :icon="Refresh" @click="reset">重置</el-button>
          <el-button size="small" :icon="Download" @click="exportVault">导出备份</el-button>
          <el-button size="small" :icon="Upload" @click="showImport = true">导入备份</el-button>
          <el-button size="small" :icon="Lock2" @click="lockVault">锁定</el-button>
          <el-button size="small" type="primary" :icon="Plus" @click="openAdd">新增密码</el-button>
        </div>
      </div>

      <!-- ② 数据表卡 -->
      <div class="table-card" v-loading="loading">
        <div class="table-bar">
          <span class="table-title">
            <el-icon><Lock /></el-icon>密码列表
          </span>
          <span class="table-meta">共 {{ tableData.length }} 条</span>
        </div>

        <el-table :data="tableData" stripe size="small" style="width:100%">
          <el-table-column prop="name" label="名称">
            <template #default="{ row }">
              <span class="name-cell">{{ row.name }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="category" label="分类">
            <template #default="{ row }">
              <el-tag size="small" effect="plain" :type="categoryType(row.category)">
                {{ row.category }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="account" label="账号">
            <template #default="{ row }">
              <span class="mono-cell">{{ row.account || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="url" label="网站地址" min-width="150">
            <template #default="{ row }">
              <a
                v-if="row.url"
                class="url-link"
                :href="row.url.startsWith('http') ? row.url : 'https://' + row.url"
                target="_blank"
                rel="noopener noreferrer"
              >
                <el-icon :size="12">
                  <Link />
                </el-icon>
                <span class="url-text">{{ row.url }}</span>
              </a>
              <span v-else class="muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="密码">
            <template #default="{ row }">
              <div class="pwd-cell">
                <span class="pwd-text">{{ row._show ? row.password : '••••••••••' }}</span>
                <el-tooltip :content="row._show ? '隐藏密码' : '查看密码'" placement="top">
                  <el-icon class="pwd-action" @click="toggleShow(row)">
                    <View v-if="!row._show" />
                    <Hide v-else />
                  </el-icon>
                </el-tooltip>
                <el-tooltip content="复制密码" placement="top">
                  <el-icon class="pwd-action" @click="copyPwd(row.password)">
                    <CopyDocument />
                  </el-icon>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="100">
            <template #default="{ row }">
              <span class="muted">{{ row.remark || '—' }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="updateDate" label="更新时间">
            <template #default="{ row }">
              <span class="muted">{{ row.updateDate }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)">编辑</el-button>
              <el-popconfirm
                title="确认删除该密码？"
                confirm-button-text="删除"
                cancel-button-text="取消"
                confirm-button-type="danger"
                @confirm="remove(row)"
              >
                <template #reference>
                  <el-button link type="danger" size="small" :icon="Delete">删除</el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="tableData.length === 0 && !loading" class="empty-state">
          <el-icon :size="36">
            <Lock />
          </el-icon>
          <p>暂无密码记录</p>
          <el-button size="small" type="primary" :icon="Plus" @click="openAdd">立即添加</el-button>
        </div>
      </div>

    </template>

    <!-- 新增/编辑弹窗 -->
    <AddAndEdit ref="dialogRef" @saved="doSearch" />

    <!-- 导入备份弹窗 -->
    <el-dialog
      v-model="showImport"
      title="从备份文件恢复"
      width="420px"
      :close-on-click-modal="false"
    >
      <div class="import-wrap">
        <el-upload
          :auto-upload="false"
          :show-file-list="false"
          accept=".json"
          :on-change="onFileSelect"
        >
          <el-button :icon="Upload" size="small">选择备份文件 (.json)</el-button>
        </el-upload>
        <p v-if="importFileName" class="import-file-name">{{ importFileName }}</p>
        <el-input
          v-model="importPwd"
          type="password"
          show-password
          placeholder="备份文件的主密码"
          size="small"
          style="margin-top:12px"
        />
        <p class="import-warn">
          <el-icon>
            <WarningFilled />
          </el-icon>
          导入将覆盖当前所有密码数据，请谨慎操作
        </p>
      </div>
      <template #footer>
        <el-button size="small" @click="showImport = false">取消</el-button>
        <el-button
          size="small"
          type="primary"
          :loading="importing"
          :disabled="!importFileText || !importPwd"
          @click="doImport"
        >确认导入
        </el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Search, Refresh, Plus, Edit, Delete,
  Lock, View, Hide, CopyDocument,
  Key, Download, Upload, WarningFilled, Link
} from '@element-plus/icons-vue'
import { Lock as Lock2 } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import passwordService from '@/services/password/password.service'
import AddAndEdit from './components/AddAndEdit.vue'

/* ---- 锁屏状态 ---- */
const unlocked = ref(false)
const hasVault = ref(false)
const masterPwd = ref('')
const masterPwdConfirm = ref('')
const unlocking = ref(false)

/* ---- 列表状态 ---- */
const loading = ref(false)
const dialogRef = ref(null)
const keyword = ref('')
const filterCategory = ref('')
const tableData = ref([])

/* ---- 导入状态 ---- */
const showImport = ref(false)
const importFileName = ref('')
const importFileText = ref('')
const importPwd = ref('')
const importing = ref(false)

const categories = ['工作', '开发', '数据库', '社交', '金融', '其他']

/* ---- 主密码解锁 ---- */
async function handleUnlock() {
  if (!masterPwd.value.trim()) return ElMessage.warning('请输入主密码')

  if (!hasVault.value) {
    // 首次设置
    if (masterPwd.value !== masterPwdConfirm.value) {
      return ElMessage.error('两次输入的主密码不一致')
    }
    if (masterPwd.value.length < 6) {
      return ElMessage.warning('主密码至少 6 位')
    }
    unlocking.value = true
    const res = await passwordService.setupMasterPassword(masterPwd.value)
    unlocking.value = false
    if (res.code !== 200) return ElMessage.error(res.error || '设置失败')
    ElMessage.success('密码库创建成功')
    hasVault.value = true
    unlocked.value = true
    await doSearch()
  } else {
    // 解锁
    unlocking.value = true
    const res = await passwordService.unlock(masterPwd.value)
    unlocking.value = false
    if (res.code !== 200) return ElMessage.error(res.error || '主密码错误')
    unlocked.value = true
    masterPwd.value = ''
    await doSearch()
  }
}

function lockVault() {
  passwordService.lock()
  unlocked.value = false
  tableData.value = []
  masterPwd.value = ''
}

/* ---- 分类 tag ---- */
function categoryType(cat) {
  const map = { '工作': '', '开发': 'info', '数据库': 'success', '社交': 'warning', '金融': 'danger', '其他': 'info' }
  return map[cat] ?? 'info'
}

/* ---- 搜索 ---- */
async function doSearch() {
  loading.value = true
  const raw = await passwordService.search({ keyword: keyword.value, category: filterCategory.value })
  tableData.value = raw.map(item => ({ ...item, _show: false }))
  loading.value = false
}

async function reset() {
  keyword.value = ''
  filterCategory.value = ''
  await doSearch()
}

/* ---- 密码显示/复制 ---- */
function toggleShow(row) {
  row._show = !row._show
}

async function copyPwd(pwd) {
  try {
    await navigator.clipboard.writeText(pwd)
    ElMessage.success('密码已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

/* ---- CRUD ---- */
function openAdd() {
  dialogRef.value?.open()
}

function openEdit(row) {
  dialogRef.value?.open(row)
}

function remove(row) {
  passwordService.remove(row.id)
  ElMessage.success('已删除')
  doSearch()
}

/* ---- 导出 ---- */
function exportVault() {
  const res = passwordService.exportBackup()
  if (res.code === 200) ElMessage.success('备份文件已下载')
  else ElMessage.error(res.error || '导出失败')
}

/* ---- 导入 ---- */
function onFileSelect(file) {
  importFileName.value = file.name
  const reader = new FileReader()
  reader.onload = e => {
    importFileText.value = e.target.result
  }
  reader.readAsText(file.raw)
}

async function doImport() {
  if (!importFileText.value || !importPwd.value) return
  importing.value = true
  const res = await passwordService.importBackup(importFileText.value, importPwd.value)
  importing.value = false
  if (res.code !== 200) return ElMessage.error(res.error || '导入失败')
  showImport.value = false
  importFileName.value = ''
  importFileText.value = ''
  importPwd.value = ''
  hasVault.value = true
  unlocked.value = true
  ElMessage.success('导入成功')
  await doSearch()
}

/* ---- 初始化 ---- */
onMounted(() => {
  hasVault.value = passwordService.hasVault()
  // 如果已解锁（同页面内刷新除外），自动加载
  if (passwordService.isUnlocked()) {
    unlocked.value = true
    doSearch()
  }
})
</script>

<style lang="scss" scoped>
.page-wrap {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 12px;
  background: #f5f7fa;
  min-height: 100%;
  box-sizing: border-box;
}

/* ===== 锁屏层 ===== */
.lock-screen {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: calc(100vh - 120px);
}

.lock-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  padding: 36px 32px 28px;
  width: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.lock-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: #e6f1fb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #185fa5;
  margin-bottom: 4px;
}

.lock-title {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
  margin: 0;
}

.lock-desc {
  font-size: 12px;
  color: #909399;
  text-align: center;
  line-height: 1.6;
  margin: 0;
}

.lock-footer {
  margin-top: 4px;
}

.lock-link {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

/* ===== 搜索卡 ===== */
.search-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px 10px;
}

.search-grid {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 10px;

  .search-field:nth-child(1) {
    width: 280px;
    flex-shrink: 0;
  }

  .search-field:nth-child(2) {
    width: 180px;
    flex-shrink: 0;
  }
}

.search-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .el-input, .el-select {
    width: 100% !important;
  }
}

.field-label {
  font-size: 11px;
  color: #909399;
  letter-spacing: 0.03em;
}

.search-footer {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

/* ===== 表格卡 ===== */
.table-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

.table-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border-bottom: 1px solid #f0f0f0;
}

.table-title {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  font-weight: 500;
  color: #303133;

  .el-icon {
    font-size: 14px;
    color: #909399;
  }
}

.table-meta {
  font-size: 12px;
  color: #c0c4cc;
}

:deep(.el-table th.el-table__cell) {
  font-size: 11px;
  letter-spacing: 0.03em;
  background: #fafafa !important;
  color: #909399;
  font-weight: 500;
}

:deep(.el-table td.el-table__cell) {
  font-size: 12px;
}

:deep(.el-table__body),
:deep(.el-table__header) {
  table-layout: fixed !important;
  width: 100% !important;
}

.name-cell {
  font-weight: 500;
  color: #303133;
}

.mono-cell {
  font-family: monospace;
  font-size: 11px;
  color: #606266;
}

.muted {
  font-size: 12px;
  color: #c0c4cc;
}

/* 网站地址链接 */
.url-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #409eff;
  text-decoration: none;
  max-width: 100%;

  &:hover {
    text-decoration: underline;
  }

  .url-text {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.pwd-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.pwd-text {
  font-family: monospace;
  font-size: 12px;
  letter-spacing: 1px;
  color: #606266;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pwd-action {
  font-size: 14px;
  color: #c0c4cc;
  cursor: pointer;
  flex-shrink: 0;
  transition: color 0.15s;

  &:hover {
    color: #409eff;
  }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 0;
  color: #c0c4cc;

  p {
    font-size: 13px;
  }
}

/* ===== 导入弹窗 ===== */
.import-wrap {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.import-file-name {
  font-size: 12px;
  color: #409eff;
  margin: 8px 0 0;
  font-family: monospace;
}

.import-warn {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: #e6a23c;
  margin-top: 12px;

  .el-icon {
    font-size: 14px;
  }
}
</style>