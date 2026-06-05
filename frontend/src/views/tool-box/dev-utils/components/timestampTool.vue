<template>
  <div class="panel-wrap">
    <div class="two-col">

      <!-- 时间戳 → 时间 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">时间戳 → 日期时间</span>
          <button class="icon-btn sm" @click="tsInput = String(Date.now())">当前时间</button>
        </div>
        <div class="card-body">
          <div class="field-row">
            <label class="field-label">时间戳</label>
            <div class="input-with-seg">
              <input class="field-input mono flex1" v-model="tsInput" placeholder="1716000000000" @input="tsToDate" />
              <div class="seg-ctrl">
                <button :class="{ active: tsUnit === 'ms' }" @click="tsUnit='ms'; tsToDate()">毫秒</button>
                <button :class="{ active: tsUnit === 's' }" @click="tsUnit='s';  tsToDate()">秒</button>
              </div>
            </div>
          </div>
          <div v-if="tsResult.error" class="result-err">{{ tsResult.error }}</div>
          <template v-else-if="tsResult.utc">
            <div class="result-grid">
              <div class="rg-item" v-for="r in tsResult.rows" :key="r.label">
                <span class="rg-label">{{ r.label }}</span>
                <div class="rg-val-wrap">
                  <span class="rg-val mono">{{ r.val }}</span>
                  <button class="copy-mini" @click="copyText(r.val)" title="复制">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="9" y="9" width="13" height="13" rx="2" />
                      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>

      <!-- 时间 → 时间戳 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">日期时间 → 时间戳</span>
        </div>
        <div class="card-body">
          <div class="field-row">
            <label class="field-label">日期时间</label>
            <input class="field-input" type="datetime-local" v-model="dtInput" @input="dateToTs" />
          </div>
          <template v-if="dtResult.ms">
            <div class="result-grid">
              <div class="rg-item" v-for="r in dtResult.rows" :key="r.label">
                <span class="rg-label">{{ r.label }}</span>
                <div class="rg-val-wrap">
                  <span class="rg-val mono">{{ r.val }}</span>
                  <button class="copy-mini" @click="copyText(r.val)" title="复制">
                    <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="9" y="9" width="13" height="13" rx="2" />
                      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 时区快查 -->
    <div class="card mt12">
      <div class="card-header">
        <span class="card-label">全球时区快查</span>
        <span class="card-sub">基于当前本地时间</span>
      </div>
      <div class="tz-grid">
        <div class="tz-item" v-for="tz in timezones" :key="tz.zone">
          <span class="tz-name">{{ tz.label }}</span>
          <span class="tz-time mono" :id="'tz-'+tz.zone">{{ tz.time }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onBeforeUnmount } from 'vue'

/* ============================================================
1. 时间戳工具
============================================================ */
const tsInput = ref(String(Date.now()))
const tsUnit = ref('ms')
const tsResult = ref({})
const dtInput = ref('')
const dtResult = ref({})

function tsToDate() {
  const raw = tsInput.value.trim()
  if (!raw) {
    tsResult.value = {}
    return
  }
  let ms = Number(raw)
  if (isNaN(ms)) {
    tsResult.value = { error: '无效的数字' }
    return
  }
  if (tsUnit.value === 's') ms = ms * 1000
  const d = new Date(ms)
  if (isNaN(d.getTime())) {
    tsResult.value = { error: '无效的时间戳' }
    return
  }

  tsResult.value = {
    utc: true,
    rows: [
      { label: 'UTC', val: d.toUTCString() },
      { label: 'ISO 8601', val: d.toISOString() },
      { label: '北京时间', val: d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }) },
      { label: '毫秒戳', val: String(ms) },
      { label: '秒级戳', val: String(Math.floor(ms / 1000)) },
      {
        label: '星期',
        val: ['日', '一', '二', '三', '四', '五', '六'][d.getDay()] + ' (周' + ['日', '一', '二', '三', '四', '五', '六'][d.getDay()] + ')'
      }
    ]
  }
}

function dateToTs() {
  if (!dtInput.value) {
    dtResult.value = {}
    return
  }
  const d = new Date(dtInput.value)
  if (isNaN(d.getTime())) {
    dtResult.value = {}
    return
  }
  const ms = d.getTime()
  dtResult.value = {
    ms,
    rows: [
      { label: '毫秒时间戳', val: String(ms) },
      { label: '秒级时间戳', val: String(Math.floor(ms / 1000)) },
      { label: 'ISO 8601', val: d.toISOString() },
      { label: 'UTC 字符串', val: d.toUTCString() }
    ]
  }
}

// 初始化：当前时间
tsToDate()
const now = new Date()
const pad = n => String(n).padStart(2, '0')
dtInput.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
dateToTs()

// 时区
const timezones = ref([
  { label: '北京 (CST)', zone: 'Asia/Shanghai' },
  { label: '东京 (JST)', zone: 'Asia/Tokyo' },
  { label: '首尔 (KST)', zone: 'Asia/Seoul' },
  { label: '新加坡', zone: 'Asia/Singapore' },
  { label: '伦敦 (GMT)', zone: 'Europe/London' },
  { label: '巴黎 (CET)', zone: 'Europe/Paris' },
  { label: '纽约 (EST)', zone: 'America/New_York' },
  { label: '洛杉矶 (PST)', zone: 'America/Los_Angeles' },
  { label: '迪拜 (GST)', zone: 'Asia/Dubai' },
  { label: 'UTC', zone: 'UTC' }
].map(tz => ({ ...tz, time: '' })))

function updateTimes() {
  const now = new Date()
  timezones.value.forEach(tz => {
    tz.time = now.toLocaleString('zh-CN', { timeZone: tz.zone, hour12: false })
  })
}

updateTimes()
const tzTimer = setInterval(updateTimes, 1000)
onBeforeUnmount(() => clearInterval(tzTimer))

</script>

<style lang="scss">
@import '../shared.scss';
</style>