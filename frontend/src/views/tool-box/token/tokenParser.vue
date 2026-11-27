<template>
  <div class="jwt-page">

    <!-- Header -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="5" y="2" width="14" height="20" rx="2" />
            <path d="M9 7h6M9 11h6M9 15h4" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">JWT Token 解析</h2>
          <p class="header-sub">解码、验证 JSON Web Token</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge" :class="statusClass">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
    </div>

    <!-- Main Layout -->
    <div class="main-layout">

      <!-- Left: Token Input -->
      <div class="col col-left">
        <div class="card">
          <div class="card-header">
            <span class="card-label">Encoded</span>
            <div class="card-actions">
              <button class="icon-btn" title="粘贴" @click="pasteToken">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
                  <rect x="8" y="2" width="8" height="4" rx="1" />
                </svg>
                粘贴
              </button>
              <button class="icon-btn" title="清除" @click="clearAll">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6l-1 14H6L5 6" />
                  <path d="M10 11v6M14 11v6" />
                  <path d="M9 6V4h6v2" />
                </svg>
                清空
              </button>
              <button class="icon-btn accent" @click="loadSample">示例</button>
            </div>
          </div>
          <div class="token-textarea-wrap">
            <textarea
              ref="tokenInput"
              v-model="rawToken"
              class="token-textarea"
              placeholder="将 JWT 粘贴到此处..."
              spellcheck="false"
              @input="onTokenInput"
            />
            <!-- Colored token overlay -->
            <div class="token-highlight" aria-hidden="true">
              <span class="part-header">{{ parts[0] || '' }}</span>
              <span v-if="parts[0] && parts[1]" class="part-dot">.</span>
              <span class="part-payload">{{ parts[1] || '' }}</span>
              <span v-if="parts[1] && parts[2]" class="part-dot">.</span>
              <span class="part-signature">{{ parts[2] || '' }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Right: Decoded panels -->
      <div class="col col-right">

        <!-- Header -->
        <div class="card decoded-card">
          <div class="card-header">
            <div class="part-tag tag-header">HEADER</div>
            <span class="card-sub">算法 &amp; Token 类型</span>
            <button class="copy-btn" @click="copyJSON('header')" title="复制">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copied === 'header' ? '已复制' : '复制' }}
            </button>
          </div>
          <div class="json-viewer" v-if="decodedHeader">
            <JsonViewer :data="decodedHeader" />
          </div>
          <div v-else class="json-empty">
            <span>等待输入...</span>
          </div>
        </div>

        <!-- Payload -->
        <div class="card decoded-card">
          <div class="card-header">
            <div class="part-tag tag-payload">PAYLOAD</div>
            <span class="card-sub">数据</span>
            <button class="copy-btn" @click="copyJSON('payload')" title="复制">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copied === 'payload' ? '已复制' : '复制' }}
            </button>
          </div>
          <div class="json-viewer" v-if="decodedPayload">
            <JsonViewer :data="decodedPayload" />
            <!-- Time fields -->
            <div v-if="timeFields.length" class="time-fields">
              <div v-for="f in timeFields" :key="f.key" class="time-row">
                <span class="time-key">{{ f.key }}</span>
                <div class="time-info">
                  <span class="time-ts">{{ f.value }}</span>
                  <span class="time-human" :class="f.expired ? 'expired' : 'valid'">
                    {{ f.human }}
                    <span v-if="f.key === 'exp'" class="exp-badge" :class="f.expired ? 'badge-expired' : 'badge-valid'">
                      {{ f.expired ? '已过期' : '未过期' }}
                    </span>
                  </span>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="json-empty"><span>等待输入...</span></div>
        </div>

        <!-- Signature Verify -->
        <div class="card decoded-card">
          <div class="card-header">
            <div class="part-tag tag-signature">SIGNATURE</div>
            <span class="card-sub">签名验证</span>
          </div>
          <div class="sig-body">
            <div class="sig-formula">
              <span class="formula-fn">{{ headerAlg }}(</span>
              <span class="part-header formula-part">base64UrlEncode(header)</span>
              <span class="formula-sep"> + "." + </span>
              <span class="part-payload formula-part">base64UrlEncode(payload)</span>
              <span class="formula-sep">,</span>
              <span class="part-signature formula-part"> secret</span>
              <span class="formula-fn">)</span>
            </div>

            <template v-if="isHMACAlg">
              <div class="secret-row">
                <label class="secret-label">
                  <input type="checkbox" v-model="secretIsBase64" @change="verifySignature" />
                  Secret Base64 编码
                </label>
                <div class="secret-input-wrap">
                  <input
                    v-model="secret"
                    class="secret-input"
                    :type="showSecret ? 'text' : 'password'"
                    placeholder="输入密钥进行验证..."
                    @input="verifySignature"
                  />
                  <button class="eye-btn" @click="showSecret = !showSecret">
                    <svg v-if="showSecret" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94" />
                      <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19" />
                      <line x1="1" y1="1" x2="23" y2="23" />
                    </svg>
                    <svg v-else width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                         stroke-width="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                      <circle cx="12" cy="12" r="3" />
                    </svg>
                  </button>
                </div>
              </div>
              <div class="verify-result" :class="verifyClass" v-if="verifyStatus !== 'idle'">
                <svg v-if="verifyStatus === 'valid'" width="15" height="15" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2.5">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <svg v-else-if="verifyStatus === 'invalid'" width="15" height="15" viewBox="0 0 24 24" fill="none"
                     stroke="currentColor" stroke-width="2.5">
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                <svg v-else width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                     stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                {{ verifyMessage }}
              </div>
              <div v-else class="verify-result verify-idle">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                输入 Secret 以验证签名
              </div>
            </template>
            <template v-else-if="decodedHeader">
              <div class="verify-result verify-info">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                {{ headerAlg }} 算法需要非对称密钥，暂不支持浏览器端验证
              </div>
            </template>
            <template v-else>
              <div class="verify-result verify-idle">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                等待输入...
              </div>
            </template>
          </div>
        </div>

      </div>
    </div>

    <!-- Error bar -->
    <Transition name="err-slide">
      <div class="error-bar" v-if="parseError">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
        {{ parseError }}
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, defineComponent, h } from 'vue'

/* ============================================================
   JSON Viewer 子组件（递归渲染，无外部依赖）
   ============================================================ */
const JsonViewer = defineComponent({
  name: 'JsonViewer',
  props: {
    data: { type: [Object, Array, String, Number, Boolean], default: null },
    depth: { type: Number, default: 0 }
  },
  setup(props) {
    const indent = computed(() => props.depth * 16)

    function renderValue(val, depth) {
      if (val === null) return h('span', { class: 'jv-null' }, 'null')
      if (val === undefined) return h('span', { class: 'jv-null' }, 'undefined')
      const t = typeof val
      if (t === 'boolean') return h('span', { class: 'jv-bool' }, String(val))
      if (t === 'number') return h('span', { class: 'jv-num' }, String(val))
      if (t === 'string') return h('span', { class: 'jv-str' }, `"${val}"`)
      if (Array.isArray(val)) {
        if (val.length === 0) return h('span', { class: 'jv-bracket' }, '[]')
        return h('span', { class: 'jv-block' }, [
          h('span', { class: 'jv-bracket' }, '['),
          h('div', { style: { paddingLeft: '16px' } },
            val.map((item, i) => h('div', { class: 'jv-line' }, [
              renderValue(item, depth + 1),
              i < val.length - 1 ? h('span', { class: 'jv-comma' }, ',') : null
            ]))
          ),
          h('span', { class: 'jv-bracket' }, ']')
        ])
      }
      if (t === 'object') {
        const entries = Object.entries(val)
        if (entries.length === 0) return h('span', { class: 'jv-bracket' }, '{}')
        return h('span', { class: 'jv-block' }, [
          h('span', { class: 'jv-bracket' }, '{'),
          h('div', { style: { paddingLeft: '16px' } },
            entries.map(([k, v], i) => h('div', { class: 'jv-line' }, [
              h('span', { class: 'jv-key' }, `"${k}"`),
              h('span', { class: 'jv-colon' }, ': '),
              renderValue(v, depth + 1),
              i < entries.length - 1 ? h('span', { class: 'jv-comma' }, ',') : null
            ]))
          ),
          h('span', { class: 'jv-bracket' }, '}')
        ])
      }
      return h('span', {}, String(val))
    }

    return () => h('div', { class: 'json-viewer-root' }, [renderValue(props.data, 0)])
  }
})

/* ============================================================
   State
   ============================================================ */
const rawToken = ref('')
const decodedHeader = ref(null)
const decodedPayload = ref(null)
const parseError = ref('')
const secret = ref('')
const secretIsBase64 = ref(false)
const showSecret = ref(false)
const verifyStatus = ref('idle')  // idle | valid | invalid | error
const verifyMessage = ref('')
const copied = ref('')

/* ============================================================
   Computed
   ============================================================ */
const parts = computed(() => {
  const t = rawToken.value.trim()
  if (!t) return ['', '', '']
  const p = t.split('.')
  return [p[0] || '', p[1] || '', p[2] || '']
})

const headerAlg = computed(() => decodedHeader.value?.alg || 'HS256')

const isHMACAlg = computed(() => {
  const alg = headerAlg.value
  return alg.startsWith('HS')
})

const timeFields = computed(() => {
  if (!decodedPayload.value) return []
  const fields = ['iat', 'exp', 'nbf']
  const now = Math.floor(Date.now() / 1000)
  return fields
    .filter(k => decodedPayload.value[k] != null)
    .map(k => {
      const v = decodedPayload.value[k]
      const date = new Date(v * 1000)
      const expired = k === 'exp' ? v < now : false
      return {
        key: k,
        value: v,
        human: date.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }),
        expired
      }
    })
})

const statusClass = computed(() => {
  if (!rawToken.value.trim()) return 'status-neutral'
  if (parseError.value) return 'status-error'
  if (decodedPayload.value) return 'status-valid'
  return 'status-neutral'
})

const statusText = computed(() => {
  if (!rawToken.value.trim()) return '等待输入'
  if (parseError.value) return 'Token 无效'
  const exp = decodedPayload.value?.exp
  if (exp && exp < Math.floor(Date.now() / 1000)) return 'Token 已过期'
  if (decodedPayload.value) return 'Token 有效'
  return '等待输入'
})

const verifyClass = computed(() => {
  if (verifyStatus.value === 'valid') return 'verify-valid'
  if (verifyStatus.value === 'invalid') return 'verify-invalid'
  if (verifyStatus.value === 'error') return 'verify-warn'
  return 'verify-idle'
})

/* ============================================================
   Base64URL 工具
   ============================================================ */
function base64UrlDecode(str) {
  // 补全 padding
  str = str.replace(/-/g, '+').replace(/_/g, '/')
  while (str.length % 4) str += '='
  const bin = atob(str)
  const bytes = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i++) bytes[i] = bin.charCodeAt(i)
  return bytes
}

function base64UrlDecodeStr(str) {
  const bytes = base64UrlDecode(str)
  return new TextDecoder().decode(bytes)
}

/* ============================================================
   Token 解析
   ============================================================ */
function onTokenInput() {
  parseToken()
  if (secret.value) verifySignature()
}

function parseToken() {
  const token = rawToken.value.trim()
  parseError.value = ''
  decodedHeader.value = null
  decodedPayload.value = null
  verifyStatus.value = 'idle'

  if (!token) return

  const p = token.split('.')
  if (p.length !== 3) {
    parseError.value = '格式错误：JWT 必须包含三个由 "." 分隔的部分'
    return
  }

  try {
    decodedHeader.value = JSON.parse(base64UrlDecodeStr(p[0]))
  } catch {
    parseError.value = 'Header 解码失败：不是有效的 Base64URL JSON'
    return
  }

  try {
    decodedPayload.value = JSON.parse(base64UrlDecodeStr(p[1]))
  } catch {
    parseError.value = 'Payload 解码失败：不是有效的 Base64URL JSON'
    return
  }
}

/* ============================================================
   签名验证（HMAC-SHA256/384/512）
   使用 Web Crypto API，纯浏览器端
   ============================================================ */
async function verifySignature() {
  if (!secret.value || !rawToken.value.trim() || !decodedHeader.value) {
    verifyStatus.value = 'idle'
    return
  }
  if (!isHMACAlg.value) return

  const alg = headerAlg.value  // HS256 / HS384 / HS512
  const hashMap = { HS256: 'SHA-256', HS384: 'SHA-384', HS512: 'SHA-512' }
  const hashAlg = hashMap[alg]
  if (!hashAlg) {
    verifyStatus.value = 'error'
    verifyMessage.value = `不支持的算法: ${alg}`
    return
  }

  try {
    const [headerB64, payloadB64, sigB64] = rawToken.value.trim().split('.')
    const signingInput = `${headerB64}.${payloadB64}`

    // 密钥字节
    let keyBytes
    if (secretIsBase64.value) {
      keyBytes = base64UrlDecode(secret.value)
    } else {
      keyBytes = new TextEncoder().encode(secret.value)
    }

    const cryptoKey = await crypto.subtle.importKey(
      'raw', keyBytes,
      { name: 'HMAC', hash: hashAlg },
      false, ['sign']
    )

    const signature = await crypto.subtle.sign(
      'HMAC',
      cryptoKey,
      new TextEncoder().encode(signingInput)
    )

    // 将计算出的签名转为 Base64URL
    const sigArr = Array.from(new Uint8Array(signature))
    const sigBase64 = btoa(String.fromCharCode(...sigArr))
      .replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '')

    if (sigBase64 === sigB64) {
      verifyStatus.value = 'valid'
      verifyMessage.value = '签名验证通过 ✓'
    } else {
      verifyStatus.value = 'invalid'
      verifyMessage.value = '签名验证失败：Secret 不匹配'
    }
  } catch (e) {
    verifyStatus.value = 'error'
    verifyMessage.value = `验证出错: ${e.message}`
  }
}

/* ============================================================
   辅助操作
   ============================================================ */
async function pasteToken() {
  try {
    const text = await navigator.clipboard.readText()
    rawToken.value = text.trim()
    parseToken()
  } catch {
    tokenInput.value?.focus()
  }
}

function clearAll() {
  rawToken.value = ''
  decodedHeader.value = null
  decodedPayload.value = null
  parseError.value = ''
  verifyStatus.value = 'idle'
  secret.value = ''
}

function loadSample() {
  rawToken.value = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IuW8gOWPkeeUqCIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoyNTE2MjM5MDIyfQ.eMGMlJJ23kgYvE6NTbVUNiWKRNXhbzPFvLUnlS_QZBI'
  secret.value = 'your-secret-key'
  parseToken()
  verifySignature()
}

async function copyJSON(part) {
  const data = part === 'header' ? decodedHeader.value : decodedPayload.value
  if (!data) return
  try {
    await navigator.clipboard.writeText(JSON.stringify(data, null, 2))
    copied.value = part
    setTimeout(() => {
      copied.value = ''
    }, 2000)
  } catch {
  }
}

const tokenInput = ref(null)
</script>

<style lang="scss" scoped>
/* ── 整体布局 ── */
.jwt-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--color-bg-page);
  overflow: hidden;
}

/* ── Header ── */
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
  background: var(--brand-primary);
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
  color: var(--color-text-primary);
}

.header-sub {
  margin: 2px 0 0;
  font-size: 12px;
  color: var(--color-text-secondary);
}

/* ── Status Badge ── */
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
    background: var(--color-bg-hover);
    border-color: var(--color-border);
    color: var(--color-text-secondary);
  }

  &.status-valid {
    background: var(--color-success-soft);
    border-color: var(--color-success);
    color: var(--color-success);
  }

  &.status-error {
    background: var(--color-danger-soft);
    border-color: var(--color-danger);
    color: var(--color-danger);
  }
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;

  .status-neutral & {
    background: var(--color-text-placeholder);
  }

  .status-valid & {
    background: var(--color-success);
    box-shadow: 0 0 0 2px var(--color-success);
  }

  .status-error & {
    background: var(--color-danger);
    box-shadow: 0 0 0 2px var(--color-danger);
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
  overflow: hidden;

  &.col-left {
    width: 380px;
    flex-shrink: 0;
  }

  &.col-right {
    flex: 1;
    min-width: 0;
    overflow-y: auto;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, .1);
      border-radius: 2px;
    }
  }
}

/* ── Card ── */
.card {
  background: var(--color-bg-card);
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.col-left .card {
  flex: 1;
  min-height: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border-light);
  flex-shrink: 0;
}

.card-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .08em;
  color: var(--color-text-secondary);
  text-transform: uppercase;
}

.card-sub {
  font-size: 11px;
  color: var(--color-text-placeholder);
  flex: 1;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-left: auto;
}

/* ── Part Tags ── */
.part-tag {
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .08em;
  padding: 2px 8px;
  border-radius: 4px;
}

.tag-header {
  background: var(--color-warning-soft);
  color: var(--color-warning);
}

.tag-payload {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.tag-signature {
  background: #fce7f3;
  color: #db2777;
}

/* ── Token Textarea ── */
.token-textarea-wrap {
  position: relative;
  flex: 1;
  min-height: 0;
}

.token-textarea {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  padding: 14px;
  border: none;
  outline: none;
  resize: none;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12.5px;
  line-height: 1.7;
  color: transparent;
  caret-color: var(--color-text-primary);
  background: transparent;
  z-index: 2;
  word-break: break-all;
  overflow-y: auto;

  &::placeholder {
    color: var(--color-text-placeholder);
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .1);
    border-radius: 2px;
  }
}

.token-highlight {
  position: absolute;
  inset: 0;
  padding: 14px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12.5px;
  line-height: 1.7;
  word-break: break-all;
  pointer-events: none;
  z-index: 1;
  overflow: hidden;
  white-space: pre-wrap;
}

/* Part Colors — 与 jwt.io 一致 */
.part-header {
  color: var(--color-warning);
  font-weight: 600;
}

.part-payload {
  color: var(--color-success);
  font-weight: 600;
}

.part-signature {
  color: #db2777;
  font-weight: 600;
}

.part-dot {
  color: var(--color-text-placeholder);
}

/* ── Buttons ── */
.icon-btn {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 4px 10px;
  border-radius: 6px;
  border: 1px solid var(--color-border);
  background: transparent;
  font-size: 11px;
  color: var(--color-text-regular);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--brand-primary);
    color: var(--brand-primary);
  }

  &.accent {
    background: var(--brand-primary-soft);
    border-color: var(--brand-primary);
    color: var(--brand-primary);
    font-weight: 600;
  }
}

.copy-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: auto;
  padding: 3px 8px;
  border-radius: 5px;
  border: 1px solid var(--color-border);
  background: transparent;
  font-size: 11px;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    color: var(--brand-primary);
    border-color: var(--brand-primary);
  }
}

/* ── JSON Viewer ── */
.decoded-card {
  flex-shrink: 0;
}

.json-viewer {
  padding: 14px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.8;
}

.json-empty {
  padding: 18px 14px;
  font-size: 12px;
  color: var(--color-text-placeholder);
  font-style: italic;
}

:deep(.json-viewer-root) {
  .jv-bracket {
    color: var(--color-text-secondary);
  }

  .jv-key {
    color: var(--brand-primary);
  }

  .jv-str {
    color: var(--color-success);
  }

  .jv-num {
    color: var(--color-warning);
  }

  .jv-bool {
    color: #db2777;
    font-weight: 600;
  }

  .jv-null {
    color: var(--color-text-placeholder);
    font-style: italic;
  }

  .jv-colon {
    color: var(--color-text-secondary);
  }

  .jv-comma {
    color: var(--color-text-secondary);
  }

  .jv-line {
    display: block;
  }

  .jv-block {
    display: inline;
  }
}

/* ── Time Fields ── */
.time-fields {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed var(--color-border-light);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.time-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 11px;
}

.time-key {
  font-family: 'Menlo', 'Monaco', monospace;
  font-weight: 700;
  color: var(--brand-primary);
  min-width: 32px;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 1px;
}

.time-ts {
  font-family: monospace;
  color: var(--color-text-secondary);
}

.time-human {
  color: var(--color-text-regular);

  &.expired {
    color: var(--color-danger);
  }

  &.valid {
    color: var(--color-success);
  }
}

.exp-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 700;

  &.badge-expired {
    background: var(--color-danger-soft);
    color: var(--color-danger);
  }

  &.badge-valid {
    background: var(--color-success-soft);
    color: var(--color-success);
  }
}

/* ── Signature Section ── */
.sig-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sig-formula {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 11.5px;
  line-height: 1.8;
  background: var(--color-bg-hover);
  border-radius: 8px;
  padding: 10px 14px;
  word-break: break-all;
}

.formula-fn {
  color: var(--color-text-secondary);
}

.formula-sep {
  color: var(--color-text-placeholder);
}

.formula-part {
  font-weight: 600;
}

.secret-row {
  display: flex;
  flex-direction: column;
  gap: 7px;
}

.secret-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  color: var(--color-text-secondary);
  cursor: pointer;

  input[type="checkbox"] {
    accent-color: var(--brand-primary);
    cursor: pointer;
  }
}

.secret-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.secret-input {
  width: 100%;
  padding: 8px 36px 8px 10px;
  box-sizing: border-box;
  border: 1.5px solid var(--color-border);
  border-radius: 7px;
  font-size: 12.5px;
  font-family: 'Menlo', 'Monaco', monospace;
  background: var(--color-bg-card);
  color: var(--color-text-primary);
  outline: none;
  transition: border-color .18s;

  &:focus {
    border-color: var(--brand-primary);
  }

  &::placeholder {
    font-family: inherit;
    color: var(--color-text-placeholder);
  }
}

.eye-btn {
  position: absolute;
  right: 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  color: var(--color-text-placeholder);
  padding: 2px;
  display: flex;
  align-items: center;

  &:hover {
    color: var(--brand-primary);
  }
}

/* ── Verify Result ── */
.verify-result {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 12px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  border: 1.5px solid;
  transition: all .2s;

  &.verify-valid {
    background: var(--color-success-soft);
    border-color: var(--color-success);
    color: var(--color-success);
  }

  &.verify-invalid {
    background: var(--color-danger-soft);
    border-color: var(--color-danger);
    color: var(--color-danger);
  }

  &.verify-warn {
    background: var(--color-warning-soft);
    border-color: var(--color-warning);
    color: var(--color-warning);
  }

  &.verify-idle {
    background: var(--color-bg-hover);
    border-color: var(--color-border-light);
    color: var(--color-text-placeholder);
    font-weight: 400;
  }

  &.verify-info {
    background: var(--brand-primary-soft);
    border-color: var(--brand-primary);
    color: var(--brand-primary);
    font-weight: 400;
  }
}

/* ── Error Bar ── */
.error-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px 14px;
  border-radius: 8px;
  background: var(--color-danger-soft);
  border: 1.5px solid var(--color-danger);
  color: var(--color-danger);
  font-size: 12px;
  flex-shrink: 0;
}

.err-slide-enter-active, .err-slide-leave-active {
  transition: all .2s ease;
}

.err-slide-enter-from, .err-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

/* ══ 移动端适配 ══ */
@media screen and (max-width: 768px) {
  .jwt-page {
    padding: 12px;
    gap: 12px;
    overflow-y: auto;
    height: auto;
    min-height: 100%;
  }

  .header-sub {
    display: none;
  }

  .header-icon {
    width: 34px;
    height: 34px;
    border-radius: 8px;
  }

  .header-title {
    font-size: 15px;
  }

  .main-layout {
    flex-direction: column;
    gap: 12px;
    overflow: visible;
  }

  .col {
    &.col-left {
      width: 100%;
      flex-shrink: 0;
      min-height: 220px;
    }

    &.col-right {
      width: 100%;
      flex: none;
      overflow: visible;
    }
  }

  .col-left .card {
    min-height: 220px;
  }

  .token-textarea {
    min-height: 160px;
  }
}
</style>