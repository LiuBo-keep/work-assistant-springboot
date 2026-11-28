<template>
  <div class="dashboard-container">
    <!-- 顶部问候横幅 -->
    <div class="welcome-banner">
      <div class="welcome-text">
        <h1 class="welcome-title">{{ greeting }}，{{ username }}</h1>
        <p class="welcome-sub">{{ todayText }} · 今天也要高效工作</p>
      </div>
      <div class="welcome-date">
        <span class="date-num">{{ dayNum }}</span>
        <span class="date-unit">{{ monthText }}</span>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section-title">快捷入口</div>
    <div class="quick-grid">
      <div
        v-for="item in quickLinks"
        :key="item.path"
        class="quick-card"
        @click="go(item.path)"
      >
        <div class="quick-icon" :style="{ background: item.bg, color: item.color }">
          <el-icon :size="22"><component :is="item.icon" /></el-icon>
        </div>
        <div class="quick-info">
          <span class="quick-name">{{ item.name }}</span>
          <span class="quick-desc">{{ item.desc }}</span>
        </div>
        <el-icon class="quick-arrow" :size="14"><ArrowRight /></el-icon>
      </div>
    </div>

    <!-- 功能概览 -->
    <div class="section-title">功能概览</div>
    <div class="overview-grid">
      <el-card class="overview-card" shadow="never" v-for="item in overview" :key="item.title">
        <div class="overview-head">
          <el-icon :size="18" :style="{ color: item.color }"><component :is="item.icon" /></el-icon>
          <span class="overview-title">{{ item.title }}</span>
        </div>
        <p class="overview-desc">{{ item.desc }}</p>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  DataAnalysis, Setting, Notebook, Finished, AlarmClock,
  Tools, DataBoard, Message, ArrowRight, CollectionTag
} from '@element-plus/icons-vue'
import browser from '../../store/persistent/browser'

const router = useRouter()
const username = computed(() => browser.username || '朋友')

/* ---- 问候语 ---- */
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const now = new Date()
const weekMap = ['日', '一', '二', '三', '四', '五', '六']
const todayText = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 星期${weekMap[now.getDay()]}`
const dayNum = String(now.getDate()).padStart(2, '0')
const monthText = `${now.getMonth() + 1}月`

/* ---- 快捷入口 ---- */
const quickLinks = [
  { name: '打卡记录', desc: '查看考勤打卡明细', path: '/hrms/reports/card-record', icon: DataAnalysis, bg: '#e6f1fb', color: '#185fa5' },
  { name: '通知设置', desc: '配置打卡提醒渠道', path: '/hrms/config', icon: Setting, bg: '#f0faf6', color: '#0f6e56' },
  { name: '工作笔记', desc: '记录工作灵感与要点', path: '/efficiency-record/notes', icon: Notebook, bg: '#f5efff', color: '#6b3fb0' },
  { name: '待办任务', desc: '管理今日待办事项', path: '/efficiency-record/work-to-do', icon: Finished, bg: '#fff4e6', color: '#854f0b' },
  { name: '定时提醒', desc: '设置周期提醒', path: '/efficiency-record/reminder', icon: AlarmClock, bg: '#fcebeb', color: '#a32d2d' },
  { name: '工具箱', desc: '开发者实用工具集', path: '/tool-box/json', icon: Tools, bg: '#eef2f7', color: '#5a6b7a' },
  { name: '工具收藏', desc: '收藏常用第三方网站', path: '/tool-box/favorite', icon: CollectionTag, bg: '#e6f1fb', color: '#378add' }
]

/* ---- 功能概览 ---- */
const overview = [
  { title: '考勤同步', desc: '工作日自动从 HRMS 拉取打卡记录，支持手动同步', icon: DataBoard, color: '#378add' },
  { title: '智能提醒', desc: '未完成打卡时通过钉钉/PushPlus 推送提醒', icon: Message, color: '#0f6e56' },
  { title: '效率记录', desc: '笔记、待办、提醒一站式管理，数据本地持久化', icon: Notebook, color: '#6b3fb0' }
]

function go(path) {
  router.push(path)
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  min-height: 100%;
  box-sizing: border-box;
}

/* ===== 欢迎横幅 ===== */
.welcome-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 28px 32px;
  border-radius: var(--radius-lg);
  background: linear-gradient(135deg, #1e2d3d 0%, #2a4660 60%, #378add 140%);
  color: #fff;
  margin-bottom: 24px;
  box-shadow: var(--shadow-md);
}

.welcome-title {
  font-size: 22px;
  font-weight: 600;
  margin-bottom: 8px;
}

.welcome-sub {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.65);
}

.welcome-date {
  display: flex;
  align-items: baseline;
  gap: 6px;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: var(--radius-md);
  backdrop-filter: blur(4px);
}

.date-num {
  font-size: 32px;
  font-weight: 700;
}

.date-unit {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.75);
}

/* ===== 区块标题 ===== */
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 24px 0 14px;

  &:first-of-type {
    margin-top: 0;
  }
}

/* ===== 快捷入口 ===== */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: var(--color-bg-card);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: transform 0.15s, box-shadow 0.15s, border-color 0.15s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
    border-color: var(--brand-primary-soft);
  }
}

.quick-icon {
  width: 46px;
  height: 46px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.quick-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.quick-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.quick-desc {
  font-size: 12px;
  color: var(--color-text-secondary);
}

.quick-arrow {
  color: var(--color-text-placeholder);
  transition: transform 0.15s, color 0.15s;
}

.quick-card:hover .quick-arrow {
  transform: translateX(3px);
  color: var(--brand-primary);
}

/* ===== 功能概览 ===== */
.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14px;
}

.overview-card {
  border-radius: var(--radius-md);
}

.overview-head {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.overview-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--color-text-primary);
}

.overview-desc {
  font-size: 12px;
  line-height: 1.6;
  color: var(--color-text-secondary);
}

/* ===== 响应式 ===== */
@media screen and (max-width: 960px) {
  .quick-grid,
  .overview-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media screen and (max-width: 640px) {
  .quick-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-container {
    padding: 12px;
  }

  .welcome-banner {
    padding: 20px;
    margin-bottom: 16px;
  }

  .welcome-title {
    font-size: 18px;
  }

  .welcome-date {
    padding: 8px 14px;
  }

  .date-num {
    font-size: 26px;
  }
}
</style>
