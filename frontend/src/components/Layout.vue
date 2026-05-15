<template>
  <div class="wrapper">
    <VHead />
    <VSidebar />
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

const handleCollapse = (msg) => { collapse.value = msg }

onMounted(()       => bus.on('collapse-content',  handleCollapse))
onBeforeUnmount(() => bus.off('collapse-content', handleCollapse))
</script>

<style lang="scss" scoped>
.wrapper {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100vh;
  overflow: hidden;
  background: #f5f7fa;
}

/* 内容区：左侧留出侧边栏宽度 */
.content-box {
  position: absolute;
  top: 56px;               /* Header 高度 */
  left: 220px;             /* Sidebar 展开宽度 */
  right: 0;
  bottom: 0;
  transition: left 0.22s ease;
  overflow: hidden;

  &.content-collapse {
    left: 64px;            /* Sidebar 折叠宽度 */
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