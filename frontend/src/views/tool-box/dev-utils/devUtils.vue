<template>
  <div class="dev-utils-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 18 22 12 16 6" />
            <polyline points="8 6 2 12 8 18" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">开发者工具集</h2>
          <p class="header-sub">时间戳 · Cron · 正则 · URL · 进制 · SQL · 加密 · 配置生成</p>
        </div>
      </div>
    </div>

    <!-- ===== Tool Tabs ===== -->
    <div class="tool-tabs">
      <button
        v-for="tab in tabs" :key="tab.key"
        class="tool-tab" :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-icon" v-html="tab.icon" />
        {{ tab.label }}
      </button>
    </div>

    <!-- ===== Tool Panels ===== -->
    <div class="tool-body">
      <TimestampTool v-if="activeTab === 'timestamp'" />
      <CronTool v-if="activeTab === 'cron'" />
      <RegexTool v-if="activeTab === 'regex'" />
      <UrlTool v-if="activeTab === 'url'" />
      <RadixTool v-if="activeTab === 'radix'" />
      <SqlTool v-if="activeTab === 'sql'" />
      <CryptoTool v-if="activeTab === 'crypto'" />
      <ConfigTool v-if="activeTab === 'config'" />
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import TimestampTool from './components/timestampTool.vue'
import CronTool from './components/cronTool.vue'
import RegexTool from './components/regexTool.vue'
import UrlTool from './components/urlTool.vue'
import RadixTool from './components/radixTool.vue'
import SqlTool from './components/sqlTool.vue'
import CryptoTool from './components/cryptoTool.vue'
import ConfigTool from './components/configTool.vue'

const activeTab = ref('timestamp')

const tabs = [
  {
    key: 'timestamp', label: '时间戳',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`
  },
  {
    key: 'cron', label: 'Cron',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`
  },
  {
    key: 'regex', label: '正则',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`
  },
  {
    key: 'url', label: 'URL 解析',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`
  },
  {
    key: 'radix', label: '进制转换',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`
  },
  {
    key: 'sql', label: 'SQL 工具',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`
  },
  {
    key: 'crypto', label: '加密工具',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
  },
  {
    key: 'config', label: '配置生成',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>`
  }
]
</script>

<style lang="scss" scoped>
@use './shared.scss';

.dev-utils-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px 16px;
  box-sizing: border-box;
  gap: 12px;
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: var(--el-color-primary, #409eff);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.header-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
}

.header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.tool-tabs {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  border-bottom: 2px solid var(--el-border-color-lighter, #ebeef5);
  padding-bottom: 0;
}

.tool-tab {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 8px 18px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  transition: all .18s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  border-radius: 8px 8px 0 0;

  .tab-icon {
    display: flex;
    align-items: center;
  }

  &:hover {
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.active {
    color: var(--el-color-primary, #409eff);
    border-bottom-color: var(--el-color-primary, #409eff);
    font-weight: 600;
    background: var(--el-bg-color, #fff);
  }
}

.tool-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>