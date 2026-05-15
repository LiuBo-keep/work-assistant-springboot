<template>
  <div class="header" :style="headerStyle">

    <!-- 折叠按钮 -->
    <div class="collapse-btn" @click="collapseChange" title="折叠菜单">
      <el-icon :size="18">
        <Expand v-if="collapse" />
        <Fold v-else />
      </el-icon>
    </div>
    <div class="hd-divider" />

    <!-- 品牌区 -->
    <div class="brand" @click="goHome">
      <div class="brand-icon" :style="{ background: accentColor }">
        <el-icon :size="14">
          <Suitcase />
        </el-icon>
      </div>
      <span class="brand-name">工作助手</span>
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
            <el-dropdown-item command="logout">
              <el-icon>
                <SwitchButton />
              </el-icon>
              退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { Fold, Expand, ArrowDown, SwitchButton, Suitcase } from '@element-plus/icons-vue'
import bus from '../utils/bus'
import browser from '../store/persistent/browser'
import NotificationBell from './NotificationBell.vue'
import ThemeSwitcher from './ThemeSwitcher.vue'

/* ---- 路由 & 折叠 ---- */
const router = useRouter()
const collapse = ref(false)

const collapseChange = () => {
  collapse.value = !collapse.value
  bus.emit('collapse', collapse.value)
}
const goHome = () => router.push('/')

/* ---- 用户 ---- */
const username = computed(() => browser.username || 'User')
const avatarText = computed(() => (username.value || 'U').charAt(0).toUpperCase())

const handleCommand = (command) => {
  if (command === 'logout') {
    browser.setAccessToken('')
    router.push('/login')
  }
}

/* ---- 主题响应（监听 ThemeSwitcher 广播，更新 Header 自身背景和头像色） ---- */
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
  if (document.body.clientWidth < 1000) collapseChange()
})

onBeforeUnmount(() => {
  bus.off('theme-change', onThemeChange)
})
</script>

<style lang="scss" scoped>
.header {
  width: 100%;
  height: 56px;
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
  height: 56px;
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

.hd-divider {
  width: 1px;
  height: 24px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 2px;
  flex-shrink: 0;
}

.hd-sep {
  width: 1px;
  height: 20px;
  background: rgba(255, 255, 255, 0.1);
  margin: 0 4px;
  flex-shrink: 0;
}

.brand {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 14px 0 10px;
  height: 56px;
  cursor: pointer;
  flex-shrink: 0;
  transition: opacity 0.15s;

  &:hover {
    opacity: 0.85;
  }
}

.brand-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  transition: background 0.25s;
}

.brand-name {
  font-size: 14px;
  font-weight: 500;
  color: #fff;
  letter-spacing: 0.02em;
  white-space: nowrap;
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