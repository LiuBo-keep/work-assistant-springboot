<template>
  <div class="page-wrap">

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
        <el-button size="small" type="primary" :icon="Plus" @click="openAdd">新增密码</el-button>
      </div>
    </div>

    <!-- ② 数据表卡 -->
    <div class="table-card">
      <div class="table-bar">
        <span class="table-title">
          <el-icon><Lock /></el-icon>密码列表
        </span>
        <span class="table-meta">共 {{ tableData.length }} 条</span>
      </div>

      <el-table :data="tableData" stripe size="small" style="width:100%">
        <el-table-column prop="name"     label="名称">
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
        <el-table-column prop="account"  label="账号">
          <template #default="{ row }">
            <span class="mono-cell">{{ row.account || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="密码">
          <template #default="{ row }">
            <div class="pwd-cell">
              <span class="pwd-text">
                {{ row._show ? row.password : '••••••••••' }}
              </span>
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
        <el-table-column prop="remark"    label="备注">
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
            <el-button link type="primary" size="small" :icon="Edit" @click="openEdit(row)">
              编辑
            </el-button>
            <el-popconfirm
              title="确认删除该密码？"
              confirm-button-text="删除"
              cancel-button-text="取消"
              confirm-button-type="danger"
              @confirm="remove(row)"
            >
              <template #reference>
                <el-button link type="danger" size="small" :icon="Delete">
                  删除
                </el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="tableData.length === 0" class="empty-state">
        <el-icon :size="36"><Lock /></el-icon>
        <p>暂无密码记录</p>
        <el-button size="small" type="primary" :icon="Plus" @click="openAdd">
          立即添加
        </el-button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <AddAndEdit ref="dialogRef" @saved="doSearch" />

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import {
  Search, Refresh, Plus, Edit, Delete,
  Lock, View, Hide, CopyDocument
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import passwordService from '@/views/password/password.service'
import AddAndEdit from './components/AddAndEdit.vue'

/* ---- refs ---- */
const dialogRef      = ref(null)
const keyword        = ref('')
const filterCategory = ref('')
const tableData      = ref([])

const categories = ['工作', '开发', '数据库', '社交', '金融', '其他']

/* ---- 分类 → el-tag type ---- */
function categoryType(cat) {
  const map = {
    '工作': '',
    '开发': 'info',
    '数据库': 'success',
    '社交': 'warning',
    '金融': 'danger',
    '其他': 'info'
  }
  return map[cat] ?? 'info'
}

/* ---- 搜索/过滤 ---- */
function doSearch() {
  const raw = passwordService.search({
    keyword:  keyword.value,
    category: filterCategory.value
  })
  // 挂载 _show 临时字段
  tableData.value = raw.map(item => ({ ...item, _show: false }))
}

function reset() {
  keyword.value        = ''
  filterCategory.value = ''
  doSearch()
}

/* ---- 密码显示/隐藏 ---- */
function toggleShow(row) {
  row._show = !row._show
}

/* ---- 复制密码 ---- */
async function copyPwd(pwd) {
  try {
    await navigator.clipboard.writeText(pwd)
    ElMessage.success('密码已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

/* ---- 新增 / 编辑 ---- */
function openAdd()      { dialogRef.value?.open() }
function openEdit(row)  { dialogRef.value?.open(row) }

/* ---- 删除 ---- */
function remove(row) {
  passwordService.remove(row.id)
  ElMessage.success('已删除')
  doSearch()
}

onMounted(() => doSearch())
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

/* 搜索卡 */
.search-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 14px 16px 10px;
}

.search-grid {
  display: grid;
  grid-template-columns: 1fr 200px;
  gap: 8px;
  margin-bottom: 10px;
}

.search-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .el-input,
  .el-select { width: 100% !important; }
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

/* 表格卡 */
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

  .el-icon { font-size: 14px; color: #909399; }
}

.table-meta { font-size: 12px; color: #c0c4cc; }

:deep(.el-table th.el-table__cell) {
  font-size: 11px;
  letter-spacing: 0.03em;
  background: #fafafa !important;
  color: #909399;
  font-weight: 500;
}
:deep(.el-table td.el-table__cell) { font-size: 12px; }
:deep(.el-table__body),
:deep(.el-table__header) {
  table-layout: fixed !important;
  width: 100% !important;
}

.name-cell { font-weight: 500; color: #303133; }
.mono-cell { font-family: monospace; font-size: 11px; color: #606266; }
.muted     { font-size: 12px; color: #c0c4cc; }

/* 密码单元格 */
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
  &:hover { color: #409eff; }
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 48px 0;
  color: #c0c4cc;

  p { font-size: 13px; }
}
</style>