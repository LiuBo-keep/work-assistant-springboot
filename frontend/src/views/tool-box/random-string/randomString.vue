<template>
  <div class="rand-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 3 21 3 21 8" />
            <line x1="4" y1="20" x2="21" y2="3" />
            <polyline points="21 16 21 21 16 21" />
            <line x1="15" y1="15" x2="21" y2="21" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">随机字符串生成</h2>
          <p class="header-sub">批量生成各类随机数据：邮箱、IP、域名、GUID、哈希值等</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge" :class="totalGenerated > 0 ? 'status-valid' : 'status-neutral'">
          <span class="status-dot" />
          {{ totalGenerated > 0 ? `已生成 ${totalGenerated} 条` : '等待生成' }}
        </div>
      </div>
    </div>

    <!-- ===== Main Layout ===== -->
    <div class="main-layout">

      <!-- ── 左列：配置区 ── -->
      <div class="col col-left">

        <!-- 全局数量 -->
        <div class="card">
          <div class="card-header">
            <span class="card-label">全局设置</span>
          </div>
          <div class="config-body">
            <div class="cfg-row">
              <label class="cfg-label">每项生成数量</label>
              <div class="qty-ctrl">
                <button class="qty-btn" @click="globalQty = Math.max(1, globalQty - 1)">−</button>
                <input class="qty-input" type="number" v-model.number="globalQty" min="1" max="50" />
                <button class="qty-btn" @click="globalQty = Math.min(50, globalQty + 1)">+</button>
              </div>
            </div>
            <div class="cfg-row">
              <label class="cfg-label">分隔符</label>
              <div class="seg-ctrl">
                <button
                  v-for="s in separators" :key="s.val"
                  :class="{ active: separator === s.val }"
                  @click="separator = s.val"
                >{{ s.label }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 生成类型选择 -->
        <div class="card types-card">
          <div class="card-header">
            <span class="card-label">生成类型</span>
            <div class="card-actions">
              <button class="icon-btn" @click="selectAll">全选</button>
              <button class="icon-btn" @click="clearSelect">清选</button>
            </div>
          </div>
          <div class="types-list">
            <label
              v-for="t in types"
              :key="t.key"
              class="type-item"
              :class="{ checked: t.checked }"
            >
              <input type="checkbox" v-model="t.checked" hidden />
              <div class="type-check">
                <svg v-if="t.checked" width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="3">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
              </div>
              <div class="type-icon" :style="{ background: t.color + '18', color: t.color }">
                <span v-html="t.icon" />
              </div>
              <div class="type-info">
                <span class="type-name">{{ t.name }}</span>
                <span class="type-example">{{ t.example }}</span>
              </div>
            </label>
          </div>
        </div>

        <!-- 生成按钮 -->
        <button class="gen-btn" @click="generateAll" :disabled="!anyChecked">
          <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
            <polyline points="23 4 23 10 17 10" />
            <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
          </svg>
          生成随机数据
        </button>

      </div>

      <!-- ── 右列：结果区 ── -->
      <div class="col col-right">

        <!-- 结果为空 -->
        <div v-if="results.length === 0" class="card empty-card">
          <div class="empty-wrap">
            <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
                 opacity="0.2">
              <polyline points="16 3 21 3 21 8" />
              <line x1="4" y1="20" x2="21" y2="3" />
              <polyline points="21 16 21 21 16 21" />
              <line x1="15" y1="15" x2="21" y2="21" />
            </svg>
            <p class="empty-title">在左侧选择类型并点击生成</p>
            <p class="empty-sub">支持同时生成多种随机数据，每类最多 50 条</p>
          </div>
        </div>

        <!-- 结果列表 -->
        <template v-else>
          <!-- 顶部工具栏 -->
          <div class="result-toolbar">
            <span class="result-summary">共 {{ totalGenerated }} 条结果</span>
            <div class="toolbar-right">
              <button class="icon-btn accent" @click="copyAll">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" />
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                </svg>
                {{ copiedAll ? '已复制 ✓' : '复制全部' }}
              </button>
              <button class="icon-btn" @click="generateAll">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
                重新生成
              </button>
              <button class="icon-btn danger" @click="results = []">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6l-1 14H6L5 6" />
                  <path d="M10 11v6M14 11v6M9 6V4h6v2" />
                </svg>
                清空
              </button>
            </div>
          </div>

          <!-- 各类型结果卡片 -->
          <div class="results-scroll">
            <div v-for="r in results" :key="r.key" class="result-card">
              <div class="rc-header">
                <div class="rc-icon" :style="{ background: r.color + '18', color: r.color }">
                  <span v-html="r.icon" />
                </div>
                <span class="rc-name">{{ r.name }}</span>
                <span class="rc-count">{{ r.items.length }} 条</span>
                <div class="rc-actions">
                  <button class="icon-btn sm" @click="refreshOne(r)">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="23 4 23 10 17 10" />
                      <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                    </svg>
                    刷新
                  </button>
                  <button class="icon-btn sm" @click="copyGroup(r)">
                    <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="9" y="9" width="13" height="13" rx="2" />
                      <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                    </svg>
                    {{ r.copied ? '已复制 ✓' : '复制' }}
                  </button>
                </div>
              </div>
              <div class="rc-items">
                <div
                  v-for="(item, idx) in r.items"
                  :key="idx"
                  class="rc-item"
                  @click="copyItem(item, r.key + idx)"
                  :class="{ 'item-copied': copiedItem === r.key + idx }"
                  title="点击复制"
                >
                  <span class="item-idx">{{ String(idx + 1).padStart(2, '0') }}</span>
                  <span class="item-val">{{ item }}</span>
                  <svg class="item-copy-icon" width="11" height="11" viewBox="0 0 24 24" fill="none"
                       stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2" />
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                  </svg>
                  <svg v-if="copiedItem === r.key + idx" class="item-ok-icon" width="11" height="11" viewBox="0 0 24 24"
                       fill="none" stroke="currentColor" stroke-width="2.5">
                    <polyline points="20 6 9 17 4 12" />
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </template>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

/* ============================================================
   配置
   ============================================================ */
const globalQty = ref(5)
const separator = ref('\n')
const separators = [
  { label: '换行', val: '\n' },
  { label: '逗号', val: ',' },
  { label: '分号', val: ';' },
  { label: '空格', val: ' ' }
]

/* ============================================================
   类型定义
   ============================================================ */
const types = reactive([
  {
    key: 'email', name: '电子邮箱', checked: true,
    color: '#409eff',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></svg>`,
    example: 'user@example.com'
  },
  {
    key: 'ipv4', name: 'IPv4 地址', checked: true,
    color: '#67c23a',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`,
    example: '192.168.1.1'
  },
  {
    key: 'ipv6', name: 'IPv6 地址', checked: true,
    color: '#e6a23c',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`,
    example: '2001:0db8:85a3::8a2e'
  },
  {
    key: 'domain', name: '域名地址', checked: true,
    color: '#9b59b6',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/></svg>`,
    example: 'my-service.io'
  },
  {
    key: 'mac', name: 'MAC 地址', checked: true,
    color: '#f56c6c',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="3" width="20" height="14" rx="2"/><line x1="8" y1="21" x2="16" y2="21"/><line x1="12" y1="17" x2="12" y2="21"/></svg>`,
    example: 'A1:B2:C3:D4:E5:F6'
  },
  {
    key: 'guid', name: 'GUID / UUID', checked: true,
    color: '#1abc9c',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
    example: '550e8400-e29b-41d4-a716'
  },
  {
    key: 'sha256', name: 'SHA-256 哈希', checked: false,
    color: '#e74c3c',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    example: 'a665a459...2fd4e179'
  },
  {
    key: 'sha1', name: 'SHA-1 哈希', checked: false,
    color: '#d35400',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    example: 'aaf4c61d...dce13da9'
  },
  {
    key: 'base64', name: 'Base64 字符串', checked: false,
    color: '#2980b9',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="4 17 10 11 4 5"/><line x1="12" y1="19" x2="20" y2="19"/></svg>`,
    example: 'SGVsbG8gV29ybGQ='
  },
  {
    key: 'md5', name: 'MD5 哈希', checked: false,
    color: '#8e44ad',
    icon: `<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    example: '5d41402a...9cfc9d56'
  }
])

const anyChecked = computed(() => types.some(t => t.checked))

/* ============================================================
   结果状态
   ============================================================ */
const results = ref([])
const copiedAll = ref(false)
const copiedItem = ref('')
const totalGenerated = computed(() => results.value.reduce((s, r) => s + r.items.length, 0))

/* ============================================================
   随机生成函数
   ============================================================ */

function randInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min
}

function randHex(len) {
  let s = ''
  for (let i = 0; i < len; i++) s += '0123456789abcdef'[randInt(0, 15)]
  return s
}

function randChoice(arr) {
  return arr[randInt(0, arr.length - 1)]
}

// ── 电子邮箱 ──
const emailPrefixes = ['alice', 'bob', 'charlie', 'diana', 'evan', 'fiona', 'george', 'helen', 'ivan', 'julia', 'kevin', 'linda', 'mike', 'nancy', 'oscar', 'peter', 'queen', 'rachel', 'sam', 'tina', 'user', 'admin', 'info', 'support', 'hello', 'dev', 'test', 'random', 'web', 'app']
const emailDomains = ['gmail.com', 'yahoo.com', 'outlook.com', 'hotmail.com', 'icloud.com', 'proton.me', 'qq.com', '163.com', '126.com', 'foxmail.com', 'example.com', 'mail.com', 'zoho.com', 'yandex.com']

function genEmail() {
  const prefix = randChoice(emailPrefixes) + (Math.random() > 0.5 ? randInt(1, 999) : '')
  return `${prefix}@${randChoice(emailDomains)}`
}

// ── IPv4 ──
function genIPv4() {
  return [randInt(1, 254), randInt(0, 255), randInt(0, 255), randInt(1, 254)].join('.')
}

// ── IPv6 ──
function genIPv6() {
  const groups = []
  for (let i = 0; i < 8; i++) groups.push(randHex(4))
  return groups.join(':')
}

// ── 域名 ──
const domainWords = ['cloud', 'fast', 'smart', 'next', 'open', 'free', 'web', 'net', 'data', 'code', 'app', 'pro', 'plus', 'hub', 'lab', 'dev', 'sys', 'tech', 'ai', 'io']
const domainTLDs = ['com', 'net', 'org', 'io', 'co', 'app', 'dev', 'tech', 'ai', 'cloud', 'info', 'online', 'site', 'store', 'blog']

function genDomain() {
  const parts = randInt(1, 2) === 1
    ? [randChoice(domainWords)]
    : [randChoice(domainWords), randChoice(domainWords)]
  const sep = Math.random() > 0.5 ? '-' : ''
  return `${parts.join(sep)}.${randChoice(domainTLDs)}`
}

// ── MAC 地址 ──
function genMAC() {
  const parts = []
  for (let i = 0; i < 6; i++) parts.push(randHex(2).toUpperCase())
  return parts.join(':')
}

// ── GUID / UUID v4 ──
function genGUID() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16)
  })
}

// ── SHA-256（Web Crypto API，异步）──
async function genSHA256() {
  const data = new TextEncoder().encode(Math.random().toString(36) + Date.now())
  const hashBuf = await crypto.subtle.digest('SHA-256', data)
  return Array.from(new Uint8Array(hashBuf)).map(b => b.toString(16).padStart(2, '0')).join('')
}

// ── SHA-1 ──
async function genSHA1() {
  const data = new TextEncoder().encode(Math.random().toString(36) + Date.now())
  const hashBuf = await crypto.subtle.digest('SHA-1', data)
  return Array.from(new Uint8Array(hashBuf)).map(b => b.toString(16).padStart(2, '0')).join('')
}

// ── Base64（随机 16 字节）──
function genBase64() {
  const bytes = new Uint8Array(16)
  crypto.getRandomValues(bytes)
  return btoa(String.fromCharCode(...bytes))
}

// ── MD5（纯 JS 实现，无需外部库）──
function md5(str) {
  function safeAdd(x, y) {
    const lsw = (x & 0xffff) + (y & 0xffff)
    return (((x >> 16) + (y >> 16) + (lsw >> 16)) << 16) | (lsw & 0xffff)
  }

  function bitRotateLeft(num, cnt) {
    return (num << cnt) | (num >>> (32 - cnt))
  }

  function md5cmn(q, a, b, x, s, t) {
    return safeAdd(bitRotateLeft(safeAdd(safeAdd(a, q), safeAdd(x, t)), s), b)
  }

  function md5ff(a, b, c, d, x, s, t) {
    return md5cmn((b & c) | ((~b) & d), a, b, x, s, t)
  }

  function md5gg(a, b, c, d, x, s, t) {
    return md5cmn((b & d) | (c & (~d)), a, b, x, s, t)
  }

  function md5hh(a, b, c, d, x, s, t) {
    return md5cmn(b ^ c ^ d, a, b, x, s, t)
  }

  function md5ii(a, b, c, d, x, s, t) {
    return md5cmn(c ^ (b | (~d)), a, b, x, s, t)
  }

  const bytes = new TextEncoder().encode(str)
  const len8 = bytes.length
  const len32 = Math.ceil((len8 + 9) / 64) * 16
  const M = new Int32Array(len32)
  for (let i = 0; i < len8; i++) M[i >> 2] |= bytes[i] << ((i % 4) * 8)
  M[len8 >> 2] |= 0x80 << ((len8 % 4) * 8)
  M[len32 - 2] = len8 * 8

  let [a, b, c, d] = [1732584193, -271733879, -1732584194, 271733878]

  for (let i = 0; i < len32; i += 16) {
    const [A, B, C, D] = [a, b, c, d]
    a = md5ff(a, b, c, d, M[i], 7, -680876936)
    d = md5ff(d, a, b, c, M[i + 1], 12, -389564586)
    c = md5ff(c, d, a, b, M[i + 2], 17, 606105819)
    b = md5ff(b, c, d, a, M[i + 3], 22, -1044525330)
    a = md5ff(a, b, c, d, M[i + 4], 7, -176418897)
    d = md5ff(d, a, b, c, M[i + 5], 12, 1200080426)
    c = md5ff(c, d, a, b, M[i + 6], 17, -1473231341)
    b = md5ff(b, c, d, a, M[i + 7], 22, -45705983)
    a = md5ff(a, b, c, d, M[i + 8], 7, 1770035416)
    d = md5ff(d, a, b, c, M[i + 9], 12, -1958414417)
    c = md5ff(c, d, a, b, M[i + 10], 17, -42063)
    b = md5ff(b, c, d, a, M[i + 11], 22, -1990404162)
    a = md5ff(a, b, c, d, M[i + 12], 7, 1804603682)
    d = md5ff(d, a, b, c, M[i + 13], 12, -40341101)
    c = md5ff(c, d, a, b, M[i + 14], 17, -1502002290)
    b = md5ff(b, c, d, a, M[i + 15], 22, 1236535329)
    a = md5gg(a, b, c, d, M[i + 1], 5, -165796510)
    d = md5gg(d, a, b, c, M[i + 6], 9, -1069501632)
    c = md5gg(c, d, a, b, M[i + 11], 14, 643717713)
    b = md5gg(b, c, d, a, M[i], 20, -373897302)
    a = md5gg(a, b, c, d, M[i + 5], 5, -701558691)
    d = md5gg(d, a, b, c, M[i + 10], 9, 38016083)
    c = md5gg(c, d, a, b, M[i + 15], 14, -660478335)
    b = md5gg(b, c, d, a, M[i + 4], 20, -405537848)
    a = md5gg(a, b, c, d, M[i + 9], 5, 568446438)
    d = md5gg(d, a, b, c, M[i + 14], 9, -1019803690)
    c = md5gg(c, d, a, b, M[i + 3], 14, -187363961)
    b = md5gg(b, c, d, a, M[i + 8], 20, 1163531501)
    a = md5gg(a, b, c, d, M[i + 13], 5, -1444681467)
    d = md5gg(d, a, b, c, M[i + 2], 9, -51403784)
    c = md5gg(c, d, a, b, M[i + 7], 14, 1735328473)
    b = md5gg(b, c, d, a, M[i + 12], 20, -1926607734)
    a = md5hh(a, b, c, d, M[i + 5], 4, -378558)
    d = md5hh(d, a, b, c, M[i + 8], 11, -2022574463)
    c = md5hh(c, d, a, b, M[i + 11], 16, 1839030562)
    b = md5hh(b, c, d, a, M[i + 14], 23, -35309556)
    a = md5hh(a, b, c, d, M[i + 1], 4, -1530992060)
    d = md5hh(d, a, b, c, M[i + 4], 11, 1272893353)
    c = md5hh(c, d, a, b, M[i + 7], 16, -155497632)
    b = md5hh(b, c, d, a, M[i + 10], 23, -1094730640)
    a = md5hh(a, b, c, d, M[i + 13], 4, 681279174)
    d = md5hh(d, a, b, c, M[i], 11, -358537222)
    c = md5hh(c, d, a, b, M[i + 3], 16, -722521979)
    b = md5hh(b, c, d, a, M[i + 6], 23, 76029189)
    a = md5hh(a, b, c, d, M[i + 9], 4, -640364487)
    d = md5hh(d, a, b, c, M[i + 12], 11, -421815835)
    c = md5hh(c, d, a, b, M[i + 15], 16, 530742520)
    b = md5hh(b, c, d, a, M[i + 2], 23, -995338651)
    a = md5ii(a, b, c, d, M[i], 6, -198630844)
    d = md5ii(d, a, b, c, M[i + 7], 10, 1126891415)
    c = md5ii(c, d, a, b, M[i + 14], 15, -1416354905)
    b = md5ii(b, c, d, a, M[i + 5], 21, -57434055)
    a = md5ii(a, b, c, d, M[i + 12], 6, 1700485571)
    d = md5ii(d, a, b, c, M[i + 3], 10, -1894986606)
    c = md5ii(c, d, a, b, M[i + 10], 15, -1051523)
    b = md5ii(b, c, d, a, M[i + 1], 21, -2054922799)
    a = md5ii(a, b, c, d, M[i + 8], 6, 1873313359)
    d = md5ii(d, a, b, c, M[i + 15], 10, -30611744)
    c = md5ii(c, d, a, b, M[i + 6], 15, -1560198380)
    b = md5ii(b, c, d, a, M[i + 13], 21, 1309151649)
    a = md5ii(a, b, c, d, M[i + 4], 6, -145523070)
    d = md5ii(d, a, b, c, M[i + 11], 10, -1120210379)
    c = md5ii(c, d, a, b, M[i + 2], 15, 718787259)
    b = md5ii(b, c, d, a, M[i + 9], 21, -343485551)
    a = safeAdd(a, A)
    b = safeAdd(b, B)
    c = safeAdd(c, C)
    d = safeAdd(d, D)
  }

  return [a, b, c, d].map(n => {
    let s = ''
    for (let j = 0; j < 4; j++) s += ('0' + ((n >> (j * 8)) & 0xff).toString(16)).slice(-2)
    return s
  }).join('')
}

function genMD5() {
  return md5(Math.random().toString(36) + Date.now().toString(36))
}

/* ============================================================
   生成入口
   ============================================================ */
const generatorMap = {
  email: () => Promise.resolve(genEmail()),
  ipv4: () => Promise.resolve(genIPv4()),
  ipv6: () => Promise.resolve(genIPv6()),
  domain: () => Promise.resolve(genDomain()),
  mac: () => Promise.resolve(genMAC()),
  guid: () => Promise.resolve(genGUID()),
  sha256: () => genSHA256(),
  sha1: () => genSHA1(),
  base64: () => Promise.resolve(genBase64()),
  md5: () => Promise.resolve(genMD5())
}

async function generateAll() {
  const checkedTypes = types.filter(t => t.checked)
  const newResults = []

  for (const t of checkedTypes) {
    const items = []
    for (let i = 0; i < globalQty.value; i++) {
      items.push(await generatorMap[t.key]())
    }
    newResults.push({
      key: t.key,
      name: t.name,
      icon: t.icon,
      color: t.color,
      items,
      copied: false
    })
  }

  results.value = newResults
}

async function refreshOne(r) {
  const t = types.find(t => t.key === r.key)
  if (!t) return
  const items = []
  for (let i = 0; i < globalQty.value; i++) {
    items.push(await generatorMap[t.key]())
  }
  r.items = items
}

/* ============================================================
   复制操作
   ============================================================ */
async function copyItem(text, id) {
  await navigator.clipboard.writeText(text).catch(() => {
  })
  copiedItem.value = id
  setTimeout(() => {
    copiedItem.value = ''
  }, 1500)
}

async function copyGroup(r) {
  const text = r.items.join(separator.value)
  await navigator.clipboard.writeText(text).catch(() => {
  })
  r.copied = true
  setTimeout(() => {
    r.copied = false
  }, 2000)
}

async function copyAll() {
  const lines = []
  for (const r of results.value) {
    lines.push(`# ${r.name}`)
    lines.push(...r.items)
    lines.push('')
  }
  await navigator.clipboard.writeText(lines.join('\n')).catch(() => {
  })
  copiedAll.value = true
  setTimeout(() => {
    copiedAll.value = false
  }, 2000)
}

/* ============================================================
   全选 / 清选
   ============================================================ */
function selectAll() {
  types.forEach(t => t.checked = true)
}

function clearSelect() {
  types.forEach(t => t.checked = false)
}
</script>

<style lang="scss" scoped>
/* ── 整体容器 ── */
.rand-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
}

/* ── Page Header ── */
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

.header-right {
  display: flex;
  align-items: center;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 5px 14px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  border: 1.5px solid;
  transition: all .2s;

  &.status-neutral {
    background: var(--el-fill-color, #f0f2f5);
    border-color: var(--el-border-color, #dcdfe6);
    color: var(--el-text-color-secondary, #909399);
  }

  &.status-valid {
    background: #f0fdf4;
    border-color: #86efac;
    color: #16a34a;
  }
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;

  .status-neutral & {
    background: var(--el-text-color-placeholder, #c0c4cc);
  }

  .status-valid & {
    background: #16a34a;
    box-shadow: 0 0 0 2px #86efac;
  }
}

/* ── Main Layout ── */
.main-layout {
  display: flex;
  gap: 16px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.col {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;

  &.col-left {
    width: 300px;
    flex-shrink: 0;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, .08);
      border-radius: 2px;
    }
  }

  &.col-right {
    flex: 1;
    min-width: 0;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
}

/* ── Card Base ── */
.card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  flex-shrink: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
}

.card-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
  color: var(--el-text-color-secondary, #909399);
  flex: 1;
}

.card-actions {
  display: flex;
  gap: 6px;
}

/* ── Config Body ── */
.config-body {
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cfg-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.cfg-label {
  font-size: 12px;
  color: var(--el-text-color-regular, #606266);
  white-space: nowrap;
}

/* ── Qty Control ── */
.qty-ctrl {
  display: flex;
  align-items: center;
  gap: 0;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;
}

.qty-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: var(--el-fill-color-light, #f5f7fa);
  font-size: 16px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  transition: all .15s;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
  }
}

.qty-input {
  width: 44px;
  height: 28px;
  border: none;
  border-left: 1px solid var(--el-border-color, #dcdfe6);
  border-right: 1px solid var(--el-border-color, #dcdfe6);
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
  background: var(--el-bg-color, #fff);
  outline: none;
  -moz-appearance: textfield;

  &::-webkit-inner-spin-button, &::-webkit-outer-spin-button {
    -webkit-appearance: none;
  }
}

/* ── Seg Control ── */
.seg-ctrl {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;

  button {
    flex: 1;
    padding: 5px 8px;
    border: none;
    background: transparent;
    font-size: 11px;
    font-weight: 500;
    color: var(--el-text-color-regular, #606266);
    cursor: pointer;
    transition: all .15s;
    white-space: nowrap;
    border-right: 1px solid var(--el-border-color, #dcdfe6);

    &:last-child {
      border-right: none;
    }

    &:hover {
      background: var(--el-fill-color-light, #f5f7fa);
    }

    &.active {
      background: var(--el-color-primary, #409eff);
      color: #fff;
    }
  }
}

/* ── Types Card ── */
.types-card {
  flex: 1;
}

.types-list {
  overflow-y: auto;
  flex: 1;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

.type-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 14px;
  cursor: pointer;
  transition: background .12s;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--el-fill-color-lighter, #fafafa);
  }

  &.checked {
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

.type-check {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: all .15s;
  background: var(--el-bg-color, #fff);

  .checked & {
    background: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    color: #fff;
  }
}

.type-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.type-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
  min-width: 0;
}

.type-name {
  font-size: 12px;
  font-weight: 500;
  color: var(--el-text-color-primary, #303133);
}

.type-example {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ── Gen Button ── */
.gen-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  padding: 11px 0;
  border-radius: 10px;
  border: none;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all .18s;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    background: var(--el-color-primary-dark-2, #337ecc);
    box-shadow: 0 4px 14px rgba(64, 158, 255, .4);
    transform: translateY(-1px);
  }

  &:disabled {
    opacity: .4;
    cursor: not-allowed;
  }
}

/* ── Icon Button ── */
.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.danger:hover {
    border-color: #fca5a5;
    color: #dc2626;
    background: #fff1f2;
  }

  &.accent {
    background: var(--el-color-primary-light-9, #ecf5ff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
    color: var(--el-color-primary, #409eff);
    font-weight: 600;
  }

  &.sm {
    padding: 3px 8px;
    font-size: 10px;
  }
}

/* ── Right Column ── */
.empty-card {
  flex: 1;

  .card {
    height: 100%;
  }
}

.empty-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 40px;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
}

.empty-sub {
  margin: 0;
  font-size: 12px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

/* ── Result Toolbar ── */
.result-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2px;
  flex-shrink: 0;
}

.result-summary {
  font-size: 12px;
  color: var(--el-text-color-secondary, #909399);
}

.toolbar-right {
  display: flex;
  gap: 6px;
}

/* ── Results Scroll ── */
.results-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-bottom: 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

/* ── Result Card ── */
.result-card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  overflow: hidden;
  flex-shrink: 0;
}

.rc-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-fill-color-lighter, #fafafa);
}

.rc-icon {
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.rc-name {
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
  flex: 1;
}

.rc-count {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
}

.rc-actions {
  display: flex;
  gap: 5px;
}

/* ── Items ── */
.rc-items {
  display: flex;
  flex-direction: column;
}

.rc-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 12px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  cursor: pointer;
  transition: background .12s;
  position: relative;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--el-fill-color-lighter, #fafafa);

    .item-copy-icon {
      opacity: 1;
    }
  }

  &.item-copied {
    background: #f0fdf4;
  }
}

.item-idx {
  font-size: 10px;
  font-family: monospace;
  color: var(--el-text-color-placeholder, #c0c4cc);
  width: 18px;
  flex-shrink: 0;
}

.item-val {
  flex: 1;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
  word-break: break-all;
  line-height: 1.5;
}

.item-copy-icon {
  flex-shrink: 0;
  color: var(--el-text-color-placeholder, #c0c4cc);
  opacity: 0;
  transition: opacity .15s;
}

.item-ok-icon {
  flex-shrink: 0;
  color: #16a34a;
}
</style>