<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑密码' : '新增密码'"
    width="480px"
    :close-on-click-modal="false"
    :show-close="true"
    @closed="resetForm"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
      size="small"
    >
      <!-- 名称 -->
      <el-form-item label="名称" prop="name">
        <el-input v-model="form.name" clearable placeholder="例：OA系统、GitLab…" />
      </el-form-item>

      <!-- 分类 -->
      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" style="width:100%" placeholder="选择分类">
          <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
        </el-select>
      </el-form-item>

      <!-- 账号 -->
      <el-form-item label="账号 / 用户名" prop="account">
        <el-input v-model="form.account" clearable placeholder="输入账号或用户名" />
      </el-form-item>

      <!-- 密码 -->
      <el-form-item label="密码" prop="password">
        <div class="pwd-input-wrap">
          <el-input
            v-model="form.password"
            :type="showPwd ? 'text' : 'password'"
            placeholder="输入密码"
            style="flex:1"
          >
            <template #suffix>
              <el-icon class="pwd-icon" @click="showPwd = !showPwd">
                <View v-if="showPwd" />
                <Hide v-else />
              </el-icon>
            </template>
          </el-input>
          <el-button size="small" title="随机生成强密码" @click="generatePassword">
            <el-icon><Refresh /></el-icon>
          </el-button>
          <el-button size="small" title="复制密码" @click="copyPassword">
            <el-icon><CopyDocument /></el-icon>
          </el-button>
        </div>

        <!-- 密码强度条 -->
        <div v-if="form.password" class="strength-wrap">
          <div class="strength-bars">
            <div
              v-for="i in 5"
              :key="i"
              class="strength-bar"
              :class="strengthClass(i)"
            />
          </div>
          <span class="strength-text" :style="{ color: strengthColor }">
            {{ strengthLabel }}
          </span>
        </div>
      </el-form-item>

      <!-- 备注 -->
      <el-form-item label="备注（可选）" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="添加备注…"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button size="small" @click="visible = false">取消</el-button>
      <el-button size="small" type="primary" :loading="saving" @click="submit">
        保存
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { View, Hide, Refresh, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import passwordService from '@/views/password/password.service'

/* ---- 对外事件 ---- */
const emit = defineEmits(['saved'])

/* ---- 状态 ---- */
const visible  = ref(false)
const saving   = ref(false)
const showPwd  = ref(false)
const isEdit   = ref(false)
const editId   = ref(null)

const categories = ['工作', '开发', '数据库', '社交', '金融', '其他']

const form = reactive({
  name:     '',
  category: '其他',
  account:  '',
  password: '',
  remark:   ''
})

const rules = {
  name:     [{ required: true, message: '名称必填', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  password: [{ required: true, message: '密码必填', trigger: 'blur' }]
}

const formRef = ref(null)

/* ---- 密码强度计算 ---- */
const strengthScore = computed(() => {
  const p = form.password
  if (!p) return 0
  let score = 0
  if (p.length >= 8)  score++
  if (p.length >= 12) score++
  if (/[A-Z]/.test(p) && /[a-z]/.test(p)) score++
  if (/\d/.test(p))   score++
  if (/[^A-Za-z0-9]/.test(p)) score++
  return score
})

const strengthLabel = computed(() => {
  const map = ['', '非常弱', '弱', '一般', '强', '非常强']
  return map[strengthScore.value] || ''
})

const strengthColor = computed(() => {
  const s = strengthScore.value
  if (s <= 1) return '#e24b4a'
  if (s <= 2) return '#e24b4a'
  if (s === 3) return '#f9a825'
  return '#43a047'
})

function strengthClass(i) {
  const s = strengthScore.value
  if (s === 0) return ''
  if (s <= 2 && i <= s)  return 'weak'
  if (s === 3 && i <= 3) return 'medium'
  if (s >= 4 && i <= s)  return 'strong'
  return ''
}

/* ---- 随机生成密码 ---- */
function generatePassword() {
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%^&*'
  form.password = Array.from({ length: 16 }, () =>
    chars[Math.floor(Math.random() * chars.length)]
  ).join('')
  showPwd.value = true
}

/* ---- 复制密码 ---- */
async function copyPassword() {
  if (!form.password) return
  try {
    await navigator.clipboard.writeText(form.password)
    ElMessage.success('密码已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

/* ---- 提交 ---- */
async function submit() {
  await formRef.value?.validate(async (valid) => {
    if (!valid) return
    saving.value = true
    try {
      const res = isEdit.value
        ? passwordService.update(editId.value, { ...form })
        : passwordService.add({ ...form })
      if (res.code !== 200) {
        ElMessage.error(res.error || '操作失败')
      } else {
        ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
        visible.value = false
        emit('saved')
      }
    } finally {
      saving.value = false
    }
  })
}

/* ---- 重置 ---- */
function resetForm() {
  formRef.value?.resetFields()
  Object.assign(form, { name: '', category: '其他', account: '', password: '', remark: '' })
  showPwd.value = false
  isEdit.value  = false
  editId.value  = null
}

/* ---- 对外暴露 ---- */
function open(record = null) {
  if (record) {
    isEdit.value  = true
    editId.value  = record.id
    Object.assign(form, {
      name:     record.name,
      category: record.category,
      account:  record.account,
      password: record.password,
      remark:   record.remark
    })
    showPwd.value = false
  }
  visible.value = true
}

defineExpose({ open })
</script>

<style lang="scss" scoped>
.pwd-input-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.pwd-icon {
  cursor: pointer;
  color: var(--el-text-color-secondary);
  &:hover { color: var(--el-text-color-primary); }
}

.strength-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
}

.strength-bars {
  display: flex;
  gap: 3px;
}

.strength-bar {
  width: 32px;
  height: 3px;
  border-radius: 2px;
  background: var(--el-border-color-lighter);
  transition: background 0.2s;

  &.weak   { background: #e24b4a; }
  &.medium { background: #f9a825; }
  &.strong { background: #43a047; }
}

.strength-text {
  font-size: 11px;
  font-weight: 500;
  transition: color 0.2s;
}
</style>