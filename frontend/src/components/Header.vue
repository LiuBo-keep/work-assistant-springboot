<template>
  <div class="header" :style="headerStyle">

    <!-- 折叠按钮 -->
    <div class="collapse-btn" @click="collapseChange" title="折叠菜单">
      <el-icon :size="18">
        <!-- 移动端显示汉堡图标，桌面端显示折叠/展开图标 -->
        <Menu v-if="isMobile" />
        <Expand v-else-if="collapse" />
        <Fold v-else />
      </el-icon>
    </div>

    <!-- 当前页面标题 -->
    <div class="page-title">
      <span class="title-text">{{ pageTitle }}</span>
    </div>

    <div class="spacer" />

    <!-- 右侧操作区 -->
    <div class="header-right">

      <!-- ① 消息通知 -->
      <NotificationBell />

      <!-- ② 主题切换 -->
      <ThemeSwitcher ref="themeSwitcherRef" />

      <div class="hd-sep" />

      <!-- ③ 用户下拉 -->
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-trigger">
          <div class="user-avatar" :style="{ background: accentColor }">
            {{ avatarText }}
          </div>
          <span class="user-name">{{ username }}</span>
          <el-icon :size="11" class="user-caret">
            <ArrowDown />
          </el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="changePassword">
              <el-icon>
                <Key />
              </el-icon>
              修改密码
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon>
                <SwitchButton />
              </el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

    </div>

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="pwdDialogVisible"
      title="修改密码"
      width="420px"
      :close-on-click-modal="false"
      append-to-body
      @closed="resetPwdForm"
    >
      <el-form
        ref="pwdFormRef"
        :model="pwdForm"
        :rules="pwdRules"
        label-position="top"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="pwdForm.oldPassword"
            type="password"
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="pwdForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="pwdForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="pwdSubmitting" @click="submitChangePassword">
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Fold, Expand, Menu, ArrowDown, SwitchButton, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import bus from '../utils/bus'
import browser from '../store/persistent/browser'
import userService from '@/services/user/user.service.js'
import NotificationBell from './NotificationBell.vue'
import ThemeSwitcher from './ThemeSwitcher.vue'

const props = defineProps({
  isMobile: { type: Boolean, default: false }
})
const emit = defineEmits(['toggle-sidebar'])

/* ---- 路由 & 折叠 ---- */
const router = useRouter()
const route = useRoute()
const collapse = ref(false)

const collapseChange = () => {
  if (props.isMobile) {
    emit('toggle-sidebar')
    return
  }
  collapse.value = !collapse.value
  bus.emit('collapse', collapse.value)
}

const pageTitle = computed(() => route.meta?.title || '工作助手')

/* ---- 用户 ---- */
const username = computed(() => browser.displayName || browser.username || 'User')
const avatarText = computed(() => (username.value || 'U').charAt(0).toUpperCase())

const handleCommand = (command) => {
  if (command === 'logout') {
    browser.clearAuth()
    router.push('/login')
  } else if (command === 'changePassword') {
    pwdDialogVisible.value = true
  }
}

/* ---- 修改密码 ---- */
const pwdDialogVisible = ref(false)
const pwdSubmitting = ref(false)
const pwdFormRef = ref()
const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于 6 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

function resetPwdForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
  pwdFormRef.value?.clearValidate()
}

async function submitChangePassword() {
  try {
    await pwdFormRef.value.validate()
  } catch {
    return
  }

  pwdSubmitting.value = true
  try {
    const response = await userService.changePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    if (response.code !== 200) {
      ElMessage.error(response.error || '修改失败')
      return
    }
    ElMessage.success('密码修改成功')
    pwdDialogVisible.value = false
  } catch (e) {
    console.error(e)
    ElMessage.error(e?.error || e?.message || '修改失败')
  } finally {
    pwdSubmitting.value = false
  }
}

/* ---- 主题响应 ---- */
const headerBg = ref(localStorage.getItem('wa.theme.bg') || '#1e2d3d')
const accentColor = ref(localStorage.getItem('wa.theme.accent') || '#378add')

const headerStyle = computed(() => ({
  background: headerBg.value,
  transition: 'background 0.25s ease'
}))

function onThemeChange({ bg, accent }) {
  headerBg.value = bg
  accentColor.value = accent
  localStorage.setItem('wa.theme.bg', bg)
  localStorage.setItem('wa.theme.accent', accent)
}

/* ---- 生命周期 ---- */
onMounted(() => {
  bus.on('theme-change', onThemeChange)
  // 仅桌面端窄窗口自动折叠；移动端交给 Layout 的抽屉逻辑处理
  if (!props.isMobile && document.body.clientWidth < 1000) collapseChange()
})

onBeforeUnmount(() => {
  bus.off('theme-change', onThemeChange)
})
</script>

<style lang="scss" scoped>
.header {
  width: 100%;
  height: var(--header-height);
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
  display: flex;
  align-items: center;
  flex-shrink: 0;
  box-sizing: border-box;
  padding-right: 8px;
  position: relative;
  z-index: 100;
}

.collapse-btn {
  width: 48px;
  height: var(--header-height);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.55);
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.07);
    color: #fff;
  }
}

/* 页面标题 */
.page-title {
  display: flex;
  align-items: center;
  padding-left: 2px;
  flex-shrink: 0;
}

.title-text {
  font-size: 14px;
  font-weight: 500;
  color: rgba(255, 255, 255, 0.9);
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.hd-sep {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 4px;
  flex-shrink: 0;
}

.spacer {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.user-trigger {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 0 10px;
  height: 34px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
  }
}

.user-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  border: 1.5px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 600;
  color: #fff;
  flex-shrink: 0;
  transition: background 0.25s;
}

.user-name {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  white-space: nowrap;
}

.user-caret {
  color: rgba(255, 255, 255, 0.35);
}

:deep(.el-dropdown) {
  outline: none;
}
</style>
