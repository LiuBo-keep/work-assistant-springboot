<template>
  <div class="login-container">
    <!-- 左侧品牌区 -->
    <div class="login-brand">
      <div class="brand-inner">
        <div class="brand-logo">
          <el-icon :size="28"><Suitcase /></el-icon>
        </div>
        <h1 class="brand-title">工作助手</h1>
        <p class="brand-slogan">考勤 · 通知 · 效率，一站式工作台</p>

        <ul class="brand-features">
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>考勤打卡自动同步</span>
          </li>
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>未打卡智能提醒</span>
          </li>
          <li>
            <el-icon><CircleCheckFilled /></el-icon>
            <span>效率工具随手可用</span>
          </li>
        </ul>
      </div>
    </div>

    <!-- 右侧登录区 -->
    <div class="login-panel">
      <div class="login-card">
        <div class="card-head">
          <h2 class="card-title">欢迎回来</h2>
          <p class="card-sub">请登录你的账号继续使用</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          label-position="top"
          @keydown.enter.prevent="login"
        >
          <transition name="el-zoom-in-top">
            <div v-if="errorMsg" class="login-error" role="alert">
              <el-icon><CircleCloseFilled /></el-icon>
              <span>{{ errorMsg }}</span>
            </div>
          </transition>

          <el-form-item prop="userName" label="用户名">
            <el-input
              v-model="loginForm.userName"
              placeholder="请输入用户名"
              size="large"
              clearable
              @input="errorMsg = ''"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item prop="password" label="密码">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              show-password
              clearable
              @input="errorMsg = ''"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </el-form-item>

          <el-button
            type="primary"
            size="large"
            :loading="loading"
            :disabled="disableLogin"
            class="login-btn"
            @click="login"
          >
            {{ loading ? '登录中…' : '登 录' }}
          </el-button>
        </el-form>

        <div class="card-footer">
          <span>工作助手 v{{ version }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { User, Lock, Suitcase, CircleCheckFilled, CircleCloseFilled } from '@element-plus/icons-vue'

import browser from '../../store/persistent/browser'
import userService from '@/services/user/user.service.js'
import pkg from '../../../package.json'

const route = useRoute()
const router = useRouter()

const loginFormRef = ref()
const loading = ref(false)
const errorMsg = ref('')
const version = pkg.version

const loginForm = reactive({
  userName: '',
  password: ''
})

const rules = {
  userName: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const disableLogin = computed(() => !loginForm.userName || !loginForm.password)

async function login() {
  if (disableLogin.value) return

  try {
    loading.value = true
    errorMsg.value = ''
    const response = await userService.login({
      userName: loginForm.userName,
      password: loginForm.password
    })

    if (response.code !== 200) {
      errorMsg.value = response.error || '登录失败，请重试'
      return
    }

    browser.setUsername(loginForm.userName)
    browser.setAccessToken(response.data.token)
    browser.setRole(response.data.role || '')
    browser.setDisplayName(response.data.displayName || loginForm.userName)
    browser.setcCurrentTime(dayjs().format('YYYY-MM-DDTHH:mm:ss'))

    const redirect = route.query.redirect || '/'
    router.push({ path: decodeURIComponent(redirect) })
  } catch (e) {
    console.error(e)
    errorMsg.value = e?.error || e?.message || '登录失败，请重试'
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  background: var(--color-bg-page);
  overflow: hidden;
  transition: background var(--transition-base);
}

/* ===== 左侧品牌区 ===== */
.login-brand {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: linear-gradient(150deg, #1e2d3d 0%, #2a4660 55%, #378add 130%);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    right: -120px;
    top: -120px;
    width: 360px;
    height: 360px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  }

  &::after {
    content: '';
    position: absolute;
    left: -80px;
    bottom: -80px;
    width: 280px;
    height: 280px;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.05) 0%, transparent 70%);
  }
}

.brand-inner {
  position: relative;
  z-index: 1;
  max-width: 420px;
}

.brand-logo {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.12);
  border: 1px solid rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-bottom: 28px;
}

.brand-title {
  font-size: 34px;
  font-weight: 700;
  color: #fff;
  letter-spacing: 0.02em;
  margin-bottom: 12px;
}

.brand-slogan {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.65);
  margin-bottom: 40px;
}

.brand-features {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 16px;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    color: rgba(255, 255, 255, 0.85);
    font-size: 14px;

    .el-icon {
      color: #67c23a;
      font-size: 18px;
    }
  }
}

/* ===== 右侧登录区 ===== */
.login-panel {
  width: 520px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
  background: var(--color-bg-page);
  transition: background var(--transition-base);
}

.login-card {
  width: 100%;
  max-width: 380px;
  background: var(--color-bg-card);
  border-radius: var(--radius-lg);
  padding: 40px 36px;
  box-shadow: var(--shadow-lg);
  transition: background var(--transition-base);
}

.card-head {
  margin-bottom: 32px;

  .card-title {
    font-size: 24px;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: 8px;
  }

  .card-sub {
    font-size: 14px;
    color: var(--color-text-secondary);
  }
}

/* ===== 登录错误提示 ===== */
.login-error {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
  border-radius: var(--radius-md);
  background: var(--el-color-danger-light-9, #fef0f0);
  border: 1px solid var(--el-color-danger-light-7, #fbc4c4);
  color: var(--el-color-danger, #f56c6c);
  font-size: 13px;
  line-height: 1.5;

  .el-icon {
    font-size: 16px;
    flex-shrink: 0;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.1em;
  margin-top: 4px;
  border-radius: var(--radius-md);
  background: var(--brand-primary);
  border-color: var(--brand-primary);

  &:hover {
    background: var(--brand-primary-hover);
    border-color: var(--brand-primary-hover);
  }
}

.card-footer {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: var(--color-text-placeholder);
}

/* ===== 响应式 ===== */
@media screen and (max-width: 860px) {
  .login-brand {
    display: none;
  }

  .login-panel {
    width: 100%;
  }
}
</style>
