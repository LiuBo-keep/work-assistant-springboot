<template>
  <div class="network-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <line x1="2" y1="12" x2="22" y2="12" />
            <path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">网络工具集</h2>
          <p class="header-sub">HTTP 客户端 · WebSocket 调试 · MQTT 客户端 · 并发压测</p>
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
      <HttpClient v-if="activeTab === 'http'" />
      <WsClient v-if="activeTab === 'ws'" />
      <MqttClient v-if="activeTab === 'mqtt'" />
      <StressTest v-if="activeTab === 'stress'" />
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import HttpClient from '@/views/tool-box/network-tools/components/httpClient.vue'
import WsClient from '@/views/tool-box/network-tools/components/wsClient.vue'
import MqttClient from '@/views/tool-box/network-tools/components/mqttClient.vue'
import StressTest from '@/views/tool-box/network-tools/components/stressTest.vue'

const activeTab = ref('http')

const tabs = [
  {
    key: 'http',
    label: 'HTTP 客户端',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <line x1="22" y1="2" x2="11" y2="13"/>
      <polygon points="22 2 15 22 11 13 2 9 22 2"/>
    </svg>`
  },
  {
    key: 'ws',
    label: 'WebSocket',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M5 12h14M12 5l7 7-7 7"/>
    </svg>`
  },
  {
    key: 'mqtt',
    label: 'MQTT',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"/>
    </svg>`
  },
  {
    key: 'stress',
    label: '并发压测',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
      <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
    </svg>`
  }
]
</script>

<style lang="scss" scoped>
.network-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px 16px;
  box-sizing: border-box;
  gap: 12px;
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  align-items: center;
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

/* ── Tabs ── */
.tool-tabs {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  border-bottom: 2px solid var(--el-border-color-lighter, #ebeef5);
}

.tool-tab {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 8px 20px;
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

/* ── Body ── */
.tool-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
</style>