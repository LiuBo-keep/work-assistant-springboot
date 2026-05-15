<template>
  <el-popover
    v-model:visible="visible"
    placement="bottom-end"
    :width="256"
    trigger="click"
    popper-class="theme-popper"
    :show-arrow="false"
  >
    <!-- 触发按钮 -->
    <template #reference>
      <div class="theme-trigger" title="切换主题" aria-label="切换主题">
        <el-icon :size="17">
          <Brush />
        </el-icon>
      </div>
    </template>

    <!-- 面板 -->
    <div class="theme-panel">

      <!-- 颜色选择 -->
      <p class="tp-section-title">主题颜色</p>
      <div class="tp-color-grid">
        <div
          v-for="(t, i) in themes"
          :key="t.name"
          class="tp-color-dot"
          :class="{ active: currentIndex === i }"
          :style="{ background: t.accent }"
          :title="t.name"
          :aria-label="t.name"
          @click="pickTheme(i)"
        >
          <el-icon v-if="currentIndex === i" :size="13">
            <Check />
          </el-icon>
        </div>
      </div>

      <!-- 外观模式 -->
      <p class="tp-section-title" style="margin-top:14px">外观模式</p>
      <div class="tp-mode-row">
        <button
          class="tp-mode-btn"
          :class="{ active: colorMode === 'light' }"
          @click="setMode('light')"
        >
          <el-icon :size="13">
            <Sunny />
          </el-icon>
          浅色
        </button>
        <button
          class="tp-mode-btn"
          :class="{ active: colorMode === 'dark' }"
          @click="setMode('dark')"
        >
          <el-icon :size="13">
            <Moon />
          </el-icon>
          深色
        </button>
      </div>

    </div>
  </el-popover>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Brush, Check, Sunny, Moon } from '@element-plus/icons-vue'
import bus from '../utils/bus'

/* ---- 主题列表 ---- */
const themes = [
  { name: '蓝色', bg: '#1e2d3d', accent: '#378add' },
  { name: '靛蓝', bg: '#1a2040', accent: '#5c6bc0' },
  { name: '紫色', bg: '#231d38', accent: '#7c4dff' },
  { name: '青色', bg: '#0d2b2b', accent: '#00897b' },
  { name: '绿色', bg: '#162318', accent: '#43a047' },
  { name: '玫红', bg: '#2d1a26', accent: '#e91e8c' },
  { name: '珊瑚', bg: '#2d1a15', accent: '#e64a19' },
  { name: '琥珀', bg: '#251c0a', accent: '#f9a825' },
  { name: '玫瑰', bg: '#2b1a1e', accent: '#e53935' },
  { name: '石墨', bg: '#1a1a1a', accent: '#757575' }
]

const STORAGE_IDX = 'wa.theme.index'
const STORAGE_MODE = 'wa.theme.mode'

/* ---- 状态 ---- */
const visible = ref(false)
const currentIndex = ref(0)
const colorMode = ref('light')

/* ---- 计算属性（供父组件 / 外部读取） ---- */
const currentTheme = computed(() => themes[currentIndex.value])

/* ---- 工具 ---- */
function darken(hex, n) {
  const r = Math.max(0, parseInt(hex.slice(1, 3), 16) - n)
  const g = Math.max(0, parseInt(hex.slice(3, 5), 16) - n)
  const b = Math.max(0, parseInt(hex.slice(5, 7), 16) - n)
  return `rgb(${r},${g},${b})`
}

/* ---- 应用主题 ---- */
function applyTheme() {
  const t = currentTheme.value
  const bg = colorMode.value === 'dark' ? darken(t.bg, 15) : t.bg

  // 注入全局 CSS 变量
  document.documentElement.style.setProperty('--theme-bg', bg)
  document.documentElement.style.setProperty('--theme-accent', t.accent)
  document.documentElement.style.setProperty('--theme-mode', colorMode.value)

  // 通知其他组件（Header背景、Sidebar激活色等）
  bus.emit('theme-change', { bg, accent: t.accent, mode: colorMode.value })

  // 持久化
  localStorage.setItem(STORAGE_IDX, String(currentIndex.value))
  localStorage.setItem(STORAGE_MODE, colorMode.value)
}

/* ---- 操作 ---- */
function pickTheme(i) {
  currentIndex.value = i
  applyTheme()
}

function setMode(m) {
  colorMode.value = m
  applyTheme()
}

/* ---- 生命周期 ---- */
onMounted(() => {
  const savedIdx = localStorage.getItem(STORAGE_IDX)
  const savedMode = localStorage.getItem(STORAGE_MODE)
  if (savedIdx !== null) currentIndex.value = Number(savedIdx)
  if (savedMode !== null) colorMode.value = savedMode
  applyTheme()
})

/* ---- 暴露给父组件 ---- */
defineExpose({ currentTheme, colorMode, applyTheme })
</script>

<style lang="scss" scoped>
.theme-trigger {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  transition: background 0.15s, color 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    color: #fff;
  }
}
</style>

<style>
/* Popover 容器 */
.theme-popper.el-popover {
  padding: 14px 16px !important;
  border-radius: 10px !important;
  border: 0.5px solid var(--color-border-tertiary) !important;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.12) !important;
}

/* 分组标题 */
.theme-panel .tp-section-title {
  font-size: 11px;
  font-weight: 500;
  color: var(--color-text-secondary);
  letter-spacing: 0.05em;
  text-transform: uppercase;
  margin-bottom: 8px;
}

/* 颜色网格 */
.theme-panel .tp-color-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 7px;
}

.theme-panel .tp-color-dot {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  cursor: pointer;
  border: 2px solid transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform 0.12s, border-color 0.12s;
}

.theme-panel .tp-color-dot:hover {
  transform: scale(1.1);
}

.theme-panel .tp-color-dot.active {
  border-color: rgba(255, 255, 255, 0.8);
  outline: 2px solid currentColor;
  outline-offset: 1px;
}

/* 模式切换 */
.theme-panel .tp-mode-row {
  display: flex;
  gap: 6px;
}

.theme-panel .tp-mode-btn {
  flex: 1;
  height: 32px;
  border-radius: 6px;
  border: 0.5px solid var(--color-border-secondary);
  background: var(--color-background-secondary);
  color: var(--color-text-secondary);
  font-size: 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  transition: background 0.15s, color 0.15s, border-color 0.15s;
  font-family: inherit;
}

.theme-panel .tp-mode-btn.active {
  background: var(--color-text-primary);
  color: var(--color-background-primary);
  border-color: transparent;
}
</style>