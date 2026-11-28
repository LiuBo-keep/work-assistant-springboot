<template>
  <div class="wrapper">
    <VHead :is-mobile="isMobile" @toggle-sidebar="toggleSidebar" />
    <VSidebar :is-mobile="isMobile" :mobile-open="mobileOpen" @close-sidebar="closeSidebar" />
    <!-- 移动端遮罩 -->
    <transition name="mask-fade">
      <div v-if="isMobile && mobileOpen" class="sidebar-mask" @click="closeSidebar" />
    </transition>
    <div class="content-box" :class="{ 'content-collapse': collapse }">
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
        <el-backtop target=".content" :right="24" :bottom="24" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import VHead    from './Header.vue'
import VSidebar from './Sidebar.vue'
import bus      from '../utils/bus'

const collapse = ref(false)
const isMobile = ref(false)
const mobileOpen = ref(false)

const MOBILE_BREAKPOINT = 768

const handleCollapse = (msg) => { collapse.value = msg }

const updateViewport = () => {
  isMobile.value = window.innerWidth <= MOBILE_BREAKPOINT
  // 切回桌面端时，重置抽屉状态
  if (!isMobile.value) mobileOpen.value = false
}

const toggleSidebar = () => {
  if (isMobile.value) {
    mobileOpen.value = !mobileOpen.value
  } else {
    // 桌面端沿用原有折叠逻辑
    collapse.value = !collapse.value
    bus.emit('collapse', collapse.value)
  }
}

const closeSidebar = () => { mobileOpen.value = false }

onMounted(() => {
  bus.on('collapse-content', handleCollapse)
  updateViewport()
  window.addEventListener('resize', updateViewport)
})

onBeforeUnmount(() => {
  bus.off('collapse-content', handleCollapse)
  window.removeEventListener('resize', updateViewport)
})
</script>

<style lang="scss" scoped>
.wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: var(--color-bg-page);
  transition: background var(--transition-base);
}

/* 内容区：左侧留出侧边栏宽度 */
.content-box {
  position: absolute;
  top: var(--header-height);
  left: var(--sidebar-width);
  right: 0;
  bottom: 0;
  transition: left var(--transition-base);
  overflow: hidden;

  &.content-collapse {
    left: var(--sidebar-collapsed-width);
  }
}

.content {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
  box-sizing: border-box;

  &::-webkit-scrollbar { width: 6px; }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.12);
    border-radius: 3px;
  }
  &::-webkit-scrollbar-track { background: transparent; }
}

/* ===== 移动端遮罩 ===== */
.sidebar-mask {
  position: fixed;
  top: var(--header-height);
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 98;
}

.mask-fade-enter-active,
.mask-fade-leave-active {
  transition: opacity 0.2s ease;
}
.mask-fade-enter-from,
.mask-fade-leave-to {
  opacity: 0;
}

/* ===== 移动端断点 ===== */
@media screen and (max-width: 768px) {
  .content-box,
  .content-box.content-collapse {
    left: 0;
  }
}

/* 页面切换动画 */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: opacity 0.18s ease, transform 0.18s ease;
}
.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>
