<template>
  <div class="sidebar" :class="{ 'is-collapse': collapse }" :style="sidebarStyle">

    <el-menu
      class="sidebar-menu"
      :default-active="route.meta.active"
      :collapse="collapse"
      :collapse-transition="false"
      unique-opened
      router
    >
      <div v-show="!collapse" class="menu-group-label">主菜单</div>

      <template v-for="item in menuItems" :key="item.index">
        <el-sub-menu v-if="item.subs" :index="item.index">
          <template #title>
            <el-icon>
              <component :is="item.icon" />
            </el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item
            v-for="sub in item.subs"
            :key="sub.index"
            :index="sub.index"
          >
            <el-icon>
              <component :is="sub.icon" />
            </el-icon>
            <template #title>{{ sub.title }}</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item v-else :index="item.index">
          <el-icon>
            <component :is="item.icon" />
          </el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>

    <div class="sidebar-footer">
      <span class="version-text">{{ collapse ? version : `v ${version}` }}</span>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import {
  Message,
  PieChart,
  DataAnalysis,
  Setting,
  Lock,
  Wallet,
  Notebook,
  Tools,
  Document,
  MagicStick,
  Switch
} from '@element-plus/icons-vue'
import bus from '../utils/bus'
import pkg from '../../package.json'

const route = useRoute()
const collapse = ref(false)
const version = pkg.version

/* ---- 主题响应 ---- */
const themeBg = ref(localStorage.getItem('wa.theme.bg') || '#1e2d3d')
const themeAccent = ref(localStorage.getItem('wa.theme.accent') || '#378add')

const sidebarStyle = computed(() => ({
  background: themeBg.value,
  borderRight: `1px solid rgba(255,255,255,0.06)`,
  transition: 'background 0.25s ease'
}))

/* 激活菜单项颜色注入（CSS 变量） */
const updateCssVar = () => {
  document.documentElement.style.setProperty('--sidebar-accent', themeAccent.value)
}

const handleThemeChange = ({ bg, accent }) => {
  themeBg.value = bg
  themeAccent.value = accent
  localStorage.setItem('wa.theme.bg', bg)
  localStorage.setItem('wa.theme.accent', accent)
  updateCssVar()
}

const handleCollapse = (msg) => {
  collapse.value = msg
  bus.emit('collapse-content', msg)
}

/* ---- 菜单配置 ---- */
const menuItems = ref([
  {
    icon: Message, index: 'hrms', title: '打卡管理',
    subs: [
      { icon: DataAnalysis, index: '/hrms/reports/card-record', title: '打卡记录' },
      { icon: Setting, index: '/hrms/config', title: '通知设置' }
    ]
  },
  {
    icon: Lock, index: 'password', title: '密码管理',
    subs: [
      { icon: Wallet, index: '/password/list', title: '密码包' }
    ]
  },
  {
    icon: Notebook, index: 'efficiency-record', title: '效率记录',
    subs: [
      { icon: Wallet, index: '/efficiency-record/notes', title: '工作笔记' }
    ]
  },
  {
    icon: Tools, index: 'tool-box', title: '工具箱',
    subs: [
      { icon: DataAnalysis, index: '/tool-box/json', title: 'JSON工具' },
      { icon: PieChart, index: '/tool-box/qrcode', title: '二维码生成' },
      { icon: Lock, index: '/tool-box/token', title: 'TOKEN 解析' },
      { icon: Document, index: '/tool-box/base64pdf', title: 'Base64PDF转换' },
      { icon: MagicStick, index: '/tool-box/random-string', title: '随机字符串' },
      { icon: Switch, index: '/tool-box/text-diff', title: '文本对比' }
    ]
  }
])

onMounted(() => {
  bus.on('collapse', handleCollapse)
  bus.on('theme-change', handleThemeChange)
  // 读取持久化主题
  const savedBg = localStorage.getItem('wa.theme.bg')
  const savedAccent = localStorage.getItem('wa.theme.accent')
  if (savedBg) themeBg.value = savedBg
  if (savedAccent) themeAccent.value = savedAccent
  updateCssVar()
})

onBeforeUnmount(() => {
  bus.off('collapse', handleCollapse)
  bus.off('theme-change', handleThemeChange)
})
</script>

<style lang="scss" scoped>
.sidebar {
  position: absolute;
  top: 56px;
  left: 0;
  bottom: 0;
  width: 220px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width 0.22s ease, background 0.25s ease;
  overflow: hidden;
  z-index: 99;

  &.is-collapse {
    width: 64px;
  }
}

.menu-group-label {
  padding: 16px 16px 5px;
  font-size: 10px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.25);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-menu {
  flex: 1;
  border: none !important;
  background: transparent !important;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 0;
  }
}

:deep(.el-sub-menu__title) {
  height: 40px !important;
  line-height: 40px !important;
  padding: 0 14px !important;
  color: rgba(255, 255, 255, 0.7) !important;
  background: transparent !important;
  font-size: 13px !important;

  .el-icon {
    font-size: 15px !important;
    margin-right: 8px !important;
    color: rgba(255, 255, 255, 0.45);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #fff !important;
  }
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.25) !important;
  font-size: 11px !important;
}

:deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.12) !important;
}

:deep(.el-menu-item) {
  height: 36px !important;
  line-height: 36px !important;
  margin: 1px 8px !important;
  padding: 0 12px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  color: rgba(255, 255, 255, 0.5) !important;
  background: transparent !important;
  width: calc(100% - 16px) !important;
  box-sizing: border-box;

  .el-icon {
    font-size: 14px !important;
    margin-right: 8px !important;
    color: rgba(255, 255, 255, 0.4);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.06) !important;
    color: rgba(255, 255, 255, 0.9) !important;

    .el-icon {
      color: rgba(255, 255, 255, 0.75) !important;
    }
  }

  /* 激活项：使用 CSS 变量跟随主题色 */
  &.is-active {
    background: var(--sidebar-accent, #378add) !important;
    color: #fff !important;

    .el-icon {
      color: #fff !important;
    }
  }
}

:deep(.el-menu--collapse) {
  .el-menu-item {
    margin: 2px 4px !important;
    width: calc(100% - 8px) !important;
    padding: 0 !important;
    justify-content: center;

    .el-icon {
      margin-right: 0 !important;
    }
  }

  .el-sub-menu__title {
    padding: 0 !important;
    justify-content: center;

    .el-icon {
      margin-right: 0 !important;
    }
  }
}

.sidebar-footer {
  flex-shrink: 0;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.08);
}

.version-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.2);
  letter-spacing: 0.04em;
  white-space: nowrap;
}
</style>