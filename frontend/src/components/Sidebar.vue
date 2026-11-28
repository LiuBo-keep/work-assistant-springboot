<template>
  <div
    class="sidebar"
    :class="{ 'is-collapse': collapse, 'is-mobile': isMobile, 'is-open': isMobile && mobileOpen }"
    :style="sidebarStyle"
  >

    <!-- 顶部品牌区 -->
    <div class="sidebar-brand" @click="goHome">
      <div class="brand-logo" :style="{ background: themeAccent }">
        <el-icon :size="16">
          <Suitcase />
        </el-icon>
      </div>
      <transition name="brand-text">
        <div v-show="!collapse" class="brand-meta">
          <span class="brand-name">工作助手</span>
          <span class="brand-sub">Work Assistant</span>
        </div>
      </transition>
    </div>

    <!-- 菜单 -->
    <el-menu
      class="sidebar-menu"
      :default-active="route.meta.active || route.path"
      :collapse="collapse"
      :collapse-transition="false"
      unique-opened
      router
      @select="onMenuSelect"
    >
      <template v-for="group in visibleMenuGroups" :key="group.title">
        <div v-show="!collapse" class="menu-group-label">{{ group.title }}</div>

        <el-sub-menu v-for="item in group.items.filter(i => i.subs)" :key="item.index" :index="item.index">
          <template #title>
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </template>
          <el-menu-item v-for="sub in item.subs" :key="sub.index" :index="sub.index">
            <el-icon><component :is="sub.icon" /></el-icon>
            <template #title>{{ sub.title }}</template>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item
          v-for="item in group.items.filter(i => !i.subs)"
          :key="item.index"
          :index="item.index"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <template #title>{{ item.title }}</template>
        </el-menu-item>
      </template>
    </el-menu>

    <!-- 底部版本信息 -->
    <div class="sidebar-footer">
      <span v-show="!collapse" class="version-text">v {{ version }}</span>
      <span v-show="collapse" class="version-dot" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Message,
  PieChart,
  DataAnalysis,
  Setting,
  Lock,
  Wallet,
  Notebook,
  Finished,
  AlarmClock,
  Tools,
  Document,
  MagicStick,
  Switch,
  Monitor,
  Connection,
  Suitcase,
  Star,
  Odometer,
  TrendCharts,
  Coin,
  FolderOpened
} from '@element-plus/icons-vue'
import bus from '../utils/bus'
import browser from '../store/persistent/browser'
import pkg from '../../package.json'

const props = defineProps({
  isMobile: { type: Boolean, default: false },
  mobileOpen: { type: Boolean, default: false }
})
const emit = defineEmits(['close-sidebar'])

const route = useRoute()
const router = useRouter()
const collapse = ref(false)
const version = pkg.version

/* ---- 主题响应 ---- */
const themeBg = ref(localStorage.getItem('wa.theme.bg') || '#1e2d3d')
const themeAccent = ref(localStorage.getItem('wa.theme.accent') || '#378add')

const sidebarStyle = computed(() => ({
  background: themeBg.value,
  borderRight: '1px solid rgba(255,255,255,0.06)',
  transition: 'background 0.25s ease'
}))

const updateCssVar = () => {
  document.documentElement.style.setProperty('--sidebar-accent', themeAccent.value)
  document.documentElement.style.setProperty('--sidebar-bg', themeBg.value)
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

// 点击菜单项后，移动端收起抽屉
const onMenuSelect = () => {
  if (props.isMobile) {
    emit('close-sidebar')
  }
}

const goHome = () => {
  router.push('/')
  if (props.isMobile) emit('close-sidebar')
}

/* ---- 菜单配置（分组） ---- */
const menuGroups = ref([
  {
    title: '工作',
    items: [
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
      }
    ]
  },
  {
    title: '效率',
    items: [
      {
        icon: Notebook, index: 'efficiency-record', title: '效率记录',
        subs: [
          { icon: Wallet, index: '/efficiency-record/notes', title: '工作笔记' },
          { icon: Finished, index: '/efficiency-record/work-to-do', title: '待办任务' },
          { icon: AlarmClock, index: '/efficiency-record/reminder', title: '定时提醒' }
        ]
      }
    ]
  },
  {
    title: '工具',
    items: [
      {
        icon: Tools, index: 'tool-box', title: '工具箱',
        subs: [
          { icon: DataAnalysis, index: '/tool-box/json', title: 'JSON 工具' },
          { icon: PieChart, index: '/tool-box/qrcode', title: '二维码生成' },
          { icon: Lock, index: '/tool-box/token', title: 'Token 解析' },
          { icon: Document, index: '/tool-box/base64pdf', title: 'Base64 转 PDF' },
          { icon: MagicStick, index: '/tool-box/random-string', title: '随机字符串' },
          { icon: Switch, index: '/tool-box/text-diff', title: '文本对比' },
          { icon: Monitor, index: '/tool-box/dev-utils', title: '开发者工具集' },
          { icon: Connection, index: '/tool-box/network-tools', title: '网络工具集' },
          { icon: Star, index: '/tool-box/favorite', title: '工具收藏' }
        ]
      }
    ]
  },
  {
    title: '系统',
    roles: ['ADMIN'],
    items: [
      {
        icon: Odometer, index: 'monitor', title: '系统监控',
        subs: [
          { icon: DataAnalysis, index: '/monitor/overview', title: '系统概览' },
          { icon: TrendCharts, index: '/monitor/trend', title: '资源趋势' },
          { icon: Connection, index: '/monitor/threads', title: '线程监控' },
          { icon: AlarmClock, index: '/monitor/tasks', title: '定时任务' },
          { icon: Coin, index: '/monitor/database', title: '数据库监控' },
          { icon: Monitor, index: '/monitor/api-monitor', title: '接口监控' }
        ]
      },
      {
        icon: FolderOpened, index: 'backup', title: '备份管理',
        subs: [
          { icon: FolderOpened, index: '/backup/database-backup', title: '数据库备份' }
        ]
      },
      {
        icon: Setting, index: 'user', title: '用户管理',
        subs: [
          { icon: Lock, index: '/system/user', title: '用户管理' }
        ]
      }
    ]
  }
])

/* ---- 按当前用户角色过滤可见菜单 ---- */
const currentRole = computed(() => browser.role || '')
const visibleMenuGroups = computed(() => {
  return menuGroups.value
    .map(group => {
      if (!group.roles || group.roles.length === 0) {
        return group
      }
      return group.roles.includes(currentRole.value)
        ? group
        : { ...group, items: [] }
    })
    .filter(group => group.items.length > 0)
})

onMounted(() => {
  bus.on('collapse', handleCollapse)
  bus.on('theme-change', handleThemeChange)
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
  top: var(--header-height);
  left: 0;
  bottom: 0;
  width: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  transition: width var(--transition-base), background 0.25s ease, transform 0.25s ease;
  overflow: hidden;
  z-index: 99;

  &.is-collapse {
    width: var(--sidebar-collapsed-width);
  }
}

/* ===== 移动端抽屉 ===== */
@media screen and (max-width: 768px) {
  .sidebar {
    position: fixed;
    top: var(--header-height);
    left: 0;
    bottom: 0;
    width: var(--sidebar-width);
    transform: translateX(-100%);
    box-shadow: none;
    z-index: 100;

    &.is-collapse {
      width: var(--sidebar-width);
    }

    &.is-open {
      transform: translateX(0);
      box-shadow: 4px 0 24px rgba(0, 0, 0, 0.35);
    }
  }
}

/* ===== 品牌区 ===== */
.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 56px;
  padding: 0 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  cursor: pointer;
  flex-shrink: 0;
  overflow: hidden;
  transition: background 0.15s;

  &:hover {
    background: rgba(255, 255, 255, 0.04);
  }
}

.brand-logo {
  width: 30px;
  height: 30px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  transition: background 0.25s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
}

.brand-meta {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
  white-space: nowrap;
  overflow: hidden;
}

.brand-name {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  letter-spacing: 0.02em;
}

.brand-sub {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 0.03em;
}

.brand-text-enter-active,
.brand-text-leave-active {
  transition: opacity 0.15s ease;
}
.brand-text-enter-from,
.brand-text-leave-to {
  opacity: 0;
}

/* ===== 菜单分组标签 ===== */
.menu-group-label {
  padding: 14px 18px 5px;
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
  height: 42px !important;
  line-height: 42px !important;
  padding: 0 14px !important;
  color: rgba(255, 255, 255, 0.7) !important;
  background: transparent !important;
  font-size: 13px !important;
  border-radius: 6px;
  margin: 2px 8px;
  width: calc(100% - 16px) !important;

  .el-icon {
    font-size: 15px !important;
    margin-right: 8px !important;
    color: rgba(255, 255, 255, 0.45);
  }

  &:hover {
    background: rgba(255, 255, 255, 0.05) !important;
    color: #fff !important;

    .el-icon {
      color: rgba(255, 255, 255, 0.75) !important;
    }
  }
}

:deep(.el-sub-menu__icon-arrow) {
  color: rgba(255, 255, 255, 0.25) !important;
  font-size: 11px !important;
}

:deep(.el-menu--inline) {
  background: rgba(0, 0, 0, 0.14) !important;
}

:deep(.el-menu-item) {
  height: 36px !important;
  line-height: 36px !important;
  margin: 1px 8px !important;
  padding: 0 12px !important;
  border-radius: 6px !important;
  font-size: 13px !important;
  color: rgba(255, 255, 255, 0.55) !important;
  background: transparent !important;
  width: calc(100% - 16px) !important;
  box-sizing: border-box;
  position: relative;

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

  /* 激活项：左侧指示条 + 高亮底色，跟随主题色 */
  &.is-active {
    background: rgba(255, 255, 255, 0.06) !important;
    color: var(--sidebar-accent, #378add) !important;
    font-weight: 600;

    .el-icon {
      color: var(--sidebar-accent, #378add) !important;
    }

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 18px;
      border-radius: 0 2px 2px 0;
      background: var(--sidebar-accent, #378add);
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

    &.is-active::before {
      display: none;
    }
  }

  .el-sub-menu__title {
    padding: 0 !important;
    justify-content: center;
    margin: 2px 4px;
    width: calc(100% - 8px) !important;

    .el-icon {
      margin-right: 0 !important;
    }
  }
}

/* ===== 底部 ===== */
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

.version-dot {
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
}
</style>
