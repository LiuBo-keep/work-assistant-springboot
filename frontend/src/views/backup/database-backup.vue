<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><FolderOpened /></el-icon>
        </div>
        <div>
          <h2 class="header-title">数据库备份</h2>
          <p class="header-sub">数据库逻辑备份、恢复导入与备份文件管理</p>
        </div>
      </div>
      <div class="header-right">
        <el-upload
          :show-file-list="false"
          accept=".sql"
          :before-upload="onImportFile"
        >
          <el-button :icon="Upload" :loading="importing">导入备份</el-button>
        </el-upload>
        <el-button type="primary" :icon="CirclePlus" @click="openBackupDialog">立即备份</el-button>
      </div>
    </div>

    <!-- ===== 备份记录列表 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">备份记录</span>
        <el-button size="small" :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
      <el-table :data="backups" size="small" border v-loading="loading">
        <el-table-column prop="fileName" label="文件名" min-width="240" show-overflow-tooltip />
        <el-table-column label="类型" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.backupType === 'PARTIAL' ? 'warning' : 'info'" size="small" effect="plain">
              {{ row.backupType === 'PARTIAL' ? '部分' : '全量' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="文件大小" min-width="120" align="right">
          <template #default="{ row }">{{ formatBytes(row.fileSize) }}</template>
        </el-table-column>
        <el-table-column prop="tableCount" label="表数量" min-width="90" align="right" />
        <el-table-column label="状态" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'SUCCESS' ? 'success' : 'danger'" size="small">
              {{ row.status === 'SUCCESS' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="备份时间" min-width="170">
          <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" align="center" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'SUCCESS'"
              size="small"
              text
              type="primary"
              :icon="Download"
              @click="doDownload(row)"
            >下载</el-button>
            <el-button
              size="small"
              text
              type="danger"
              :icon="Delete"
              @click="doDelete(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ===== 备份模式选择弹窗 ===== -->
    <el-dialog
      v-model="backupDialogVisible"
      title="新建备份"
      width="560px"
      :close-on-click-modal="false"
    >
      <div class="backup-dialog">
        <el-radio-group v-model="backupMode" class="mode-group">
          <el-radio-button value="FULL">全量备份</el-radio-button>
          <el-radio-button value="PARTIAL">部分备份</el-radio-button>
        </el-radio-group>

        <template v-if="backupMode === 'FULL'">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="全量备份将备份数据库中所有表的数据"
          />
        </template>

        <template v-else>
          <el-alert
            type="warning"
            :closable="false"
            show-icon
            title="请勾选需要备份的表（可多选）"
            style="margin-bottom: 12px"
          />
          <div class="table-select-wrap">
            <el-checkbox-group v-model="selectedTables" class="table-checkbox-group">
              <el-checkbox
                v-for="t in allTables"
                :key="t"
                :value="t"
                class="table-checkbox"
              >{{ t }}</el-checkbox>
            </el-checkbox-group>
            <el-empty v-if="!allTables.length" description="暂无可用表" :image-size="60" />
          </div>
          <div class="table-select-tip">
            已选 <b>{{ selectedTables.length }}</b> 张表
          </div>
        </template>
      </div>

      <template #footer>
        <el-button @click="backupDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="backing"
          :disabled="backupMode === 'PARTIAL' && !selectedTables.length"
          @click="doBackup"
        >开始备份</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { FolderOpened, Refresh, CirclePlus, Upload, Download, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import backupService from '@/services/backup/backup.service'

/* ---- 状态 ---- */
const loading = ref(false)
const backing = ref(false)
const importing = ref(false)
const backups = ref([])
const backupDialogVisible = ref(false)
const backupMode = ref('FULL')
const allTables = ref([])
const selectedTables = ref([])

/* ---- 格式化 ---- */
function formatBytes(bytes) {
  if (bytes == null) return '--'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

function formatTime(str) {
  if (!str) return '--'
  return str.replace('T', ' ').substring(0, 19)
}

/* ---- 数据加载 ---- */
async function load() {
  loading.value = true
  try {
    const res = await backupService.list()
    if (res.code === 200) {
      backups.value = res.data?.data ?? res.data ?? []
    }
  } finally {
    loading.value = false
  }
}

async function loadTables() {
  const res = await backupService.tables()
  if (res.code === 200) {
    allTables.value = res.data?.data ?? res.data ?? []
  }
}

/* ---- 操作 ---- */
function openBackupDialog() {
  backupMode.value = 'FULL'
  selectedTables.value = []
  backupDialogVisible.value = true
  loadTables()
}

async function doBackup() {
  backing.value = true
  try {
    const tables = backupMode.value === 'PARTIAL' ? selectedTables.value : null
    const res = await backupService.backup(tables)
    if (res.code === 200) {
      const data = res.data?.data ?? res.data
      if (data?.success) {
        ElMessage.success('备份完成')
        backupDialogVisible.value = false
      } else {
        ElMessage.error('备份失败：' + (data?.record?.errorMsg || '未知错误'))
      }
      await load()
    }
  } finally {
    backing.value = false
  }
}

async function onImportFile(file) {
  importing.value = true
  try {
    await ElMessageBox.confirm(
      `导入将向现有表写入数据，可能产生重复或冲突。确定导入「${file.name}」吗？`,
      '导入确认',
      { type: 'warning', confirmButtonText: '确定导入', cancelButtonText: '取消' }
    )
    const res = await backupService.importSql(file)
    if (res.code === 200) {
      const data = res.data?.data ?? res.data
      ElMessage.success(`导入成功，共执行 ${data?.executedStatements ?? 0} 条语句`)
    }
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('导入失败：' + (e?.data?.msg || e?.error || '未知错误'))
    }
  } finally {
    importing.value = false
  }
  return false
}

async function doDownload(row) {
  try {
    const res = await backupService.download(row.id)
    const blob = res.data instanceof Blob ? res.data : new Blob([res.data])
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = row.fileName
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) {
    ElMessage.error('下载失败')
  }
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除备份「${row.fileName}」吗？`, '删除确认', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
    const res = await backupService.remove(row.id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      await load()
    }
  } catch (e) {
    if (e !== 'cancel' && e !== 'close') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  load()
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
}

/* ===== 备份弹窗 ===== */
.backup-dialog {
  .mode-group {
    width: 100%;
    margin-bottom: 16px;

    :deep(.el-radio-button) {
      flex: 1;
    }

    :deep(.el-radio-button__inner) {
      width: 100%;
    }
  }

  .table-select-wrap {
    max-height: 320px;
    overflow-y: auto;
    border: 1px solid var(--color-border);
    border-radius: 8px;
    padding: 12px;
  }

  .table-checkbox-group {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 8px 12px;
  }

  .table-checkbox {
    margin-right: 0;
  }

  .table-select-tip {
    margin-top: 10px;
    font-size: 13px;
    color: var(--color-text-secondary);

    b {
      color: var(--brand-primary);
    }
  }
}
</style>
