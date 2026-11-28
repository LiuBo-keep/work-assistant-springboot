<template>
  <div class="monitor-page">
    <!-- ===== 顶部标题栏 ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20"><Setting /></el-icon>
        </div>
        <div>
          <h2 class="header-title">用户管理</h2>
          <p class="header-sub">管理系统登录账号与角色权限</p>
        </div>
      </div>
      <div class="header-right">
        <el-button type="primary" :icon="CirclePlus" @click="openCreate">新增用户</el-button>
      </div>
    </div>

    <!-- ===== 用户列表 ===== -->
    <div class="panel">
      <div class="panel-head">
        <span class="panel-title">用户列表</span>
        <el-button size="small" :icon="Refresh" :loading="loading" @click="load">刷新</el-button>
      </div>
      <el-table :data="users" size="small" border v-loading="loading">
        <el-table-column prop="userName" label="用户名" min-width="140" />
        <el-table-column prop="displayName" label="显示名称" min-width="140" />
        <el-table-column label="角色" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ADMIN' ? 'danger' : 'info'" size="small" effect="plain">
              {{ row.role === 'ADMIN' ? '超级管理员' : '普通用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">
              {{ row.enabled ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ row.createdAt }}</template>
        </el-table-column>
        <el-table-column label="操作" min-width="220" align="center" fixed="right">
          <template #default="{ row }">
            <el-button size="small" text type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
            <el-button
              size="small"
              text
              type="warning"
              :icon="Key"
              @click="openResetPassword(row)"
            >重置密码</el-button>
            <el-button size="small" text type="danger" :icon="Delete" @click="doDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ===== 新增 / 编辑弹窗 ===== -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="userName">
          <el-input v-model="form.userName" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="请输入显示名称" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio-button value="NORMAL">普通用户</el-radio-button>
            <el-radio-button value="ADMIN">超级管理员</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-text="启用" inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doSave">确定</el-button>
      </template>
    </el-dialog>

    <!-- ===== 重置密码弹窗 ===== -->
    <el-dialog v-model="resetVisible" title="重置密码" width="420px" :close-on-click-modal="false">
      <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="90px">
        <el-form-item label="用户名">
          <el-input :model-value="resetTarget?.userName" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input v-model="resetForm.password" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="doResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Setting, CirclePlus, Refresh, Edit, Key, Delete } from '@element-plus/icons-vue'
import userService from '@/services/user/user.service.js'

const loading = ref(false)
const saving = ref(false)
const users = ref([])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: '',
  userName: '',
  displayName: '',
  role: 'NORMAL',
  password: '',
  enabled: true
})

const resetVisible = ref(false)
const resetTarget = ref(null)
const resetFormRef = ref()
const resetForm = reactive({ password: '' })

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const resetRules = {
  password: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

async function load() {
  loading.value = true
  try {
    const res = await userService.list()
    users.value = res.data?.data ?? res.data ?? []
  } catch (e) {
    ElMessage.error(e.error || '加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: '',
    userName: '',
    displayName: '',
    role: 'NORMAL',
    password: '',
    enabled: true
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    userName: row.userName,
    displayName: row.displayName,
    role: row.role,
    password: '',
    enabled: row.enabled
  })
  dialogVisible.value = true
}

function openResetPassword(row) {
  resetTarget.value = row
  resetForm.password = ''
  resetVisible.value = true
}

async function doSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    const payload = { ...form }
    if (isEdit.value) {
      payload.password = ''
    }
    await userService.save(payload)
    ElMessage.success(isEdit.value ? '修改成功' : '新增成功')
    dialogVisible.value = false
    load()
  } catch (e) {
    if (e?.error) ElMessage.error(e.error)
  } finally {
    saving.value = false
  }
}

async function doResetPassword() {
  await resetFormRef.value.validate()
  saving.value = true
  try {
    await userService.save({
      id: resetTarget.value.id,
      password: resetForm.password
    })
    ElMessage.success('密码已重置')
    resetVisible.value = false
  } catch (e) {
    if (e?.error) ElMessage.error(e.error)
  } finally {
    saving.value = false
  }
}

async function doDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除用户「${row.userName}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await userService.remove(row.id)
    ElMessage.success('删除成功')
    load()
  } catch (e) {
    if (e?.error) ElMessage.error(e.error)
  }
}

onMounted(load)
</script>

<style lang="scss" scoped>
.monitor-page {
  padding: 16px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .header-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    background: var(--brand-primary, #378add);
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    margin: 0;
    color: var(--color-text-primary);
  }

  .header-sub {
    font-size: 12px;
    color: var(--color-text-secondary);
    margin: 4px 0 0;
  }
}

.panel {
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 16px;
  box-shadow: var(--shadow-sm, 0 1px 4px rgba(0, 0, 0, 0.04));
}

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
</style>
