<template>
  <div class="panel-wrap">
    <div class="http-layout">
      <!-- ══ 左侧：请求配置 ══ -->
      <div class="http-left">
        <!-- URL Bar -->
        <div class="card url-card">
          <div class="url-bar">
            <select
              class="method-select"
              v-model="method"
              :class="'method-' + method.toLowerCase()"
            >
              <option v-for="m in methods" :key="m" :value="m">{{ m }}</option>
            </select>
            <input
              class="url-input mono"
              v-model="url"
              placeholder="https://api.example.com/users"
              @keydown.enter="sendRequest"
              @input="parseUrlParams"
            />
            <button
              class="send-btn"
              @click="sendRequest"
              :disabled="loading || !url"
            >
              <span v-if="loading" class="spin-icon">↻</span>
              <svg
                v-else
                width="13"
                height="13"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2.5"
              >
                <line x1="22" y1="2" x2="11" y2="13" />
                <polygon points="22 2 15 22 11 13 2 9 22 2" />
              </svg>
              {{ loading ? '发送中...' : '发送' }}
            </button>
          </div>
        </div>

        <!-- 请求配置 Tabs -->
        <div class="card req-config-card">
          <div class="inner-tabs">
            <button
              v-for="t in reqTabs"
              :key="t.key"
              class="inner-tab"
              :class="{ active: reqTab === t.key }"
              @click="reqTab = t.key"
            >
              {{ t.label }}
              <span
                v-if="t.key === 'headers' && activeHeaders.length"
                class="badge"
                >{{ activeHeaders.length }}</span
              >
              <span
                v-if="t.key === 'params' && activeParams.length"
                class="badge"
                >{{ activeParams.length }}</span
              >
            </button>
          </div>

          <!-- Query Params -->
          <div v-if="reqTab === 'params'" class="kv-panel">
            <div class="kv-head">
              <span>Key</span><span>Value</span><span></span>
            </div>
            <div class="kv-row" v-for="(p, i) in params" :key="i">
              <input
                class="kv-input"
                v-model="p.key"
                placeholder="参数名"
                @input="syncUrl"
              />
              <input
                class="kv-input"
                v-model="p.value"
                placeholder="参数值"
                @input="syncUrl"
              />
              <button class="kv-del" @click="removeParam(i)">✕</button>
            </div>
            <button class="kv-add" @click="addParam">+ 添加参数</button>
          </div>

          <!-- Headers -->
          <div v-if="reqTab === 'headers'" class="kv-panel">
            <div class="kv-head">
              <span>Header</span><span>Value</span><span></span>
            </div>
            <div class="kv-row" v-for="(h, i) in headers" :key="i">
              <input
                class="kv-input"
                v-model="h.key"
                placeholder="Header 名"
                list="common-headers"
              />
              <input class="kv-input" v-model="h.value" placeholder="值" />
              <button class="kv-del" @click="removeHeader(i)">✕</button>
            </div>
            <button class="kv-add" @click="addHeader">+ 添加 Header</button>
            <datalist id="common-headers">
              <option value="Content-Type" />
              <option value="Authorization" />
              <option value="Accept" />
              <option value="Cache-Control" />
              <option value="X-Request-ID" />
              <option value="X-API-Key" />
            </datalist>
          </div>

          <!-- Body -->
          <div v-if="reqTab === 'body'" class="body-panel">
            <div class="body-type-row">
              <label v-for="bt in bodyTypes" :key="bt" class="radio-label">
                <input type="radio" v-model="bodyType" :value="bt" />{{ bt }}
              </label>
            </div>
            <textarea
              v-if="bodyType === 'JSON' || bodyType === 'Raw'"
              class="body-textarea mono"
              v-model="bodyRaw"
              :placeholder="bodyPlaceholder"
              spellcheck="false"
            />
            <div v-if="bodyType === 'Form'" class="kv-panel">
              <div class="kv-head">
                <span>Key</span><span>Value</span><span></span>
              </div>
              <div class="kv-row" v-for="(f, i) in formData" :key="i">
                <input class="kv-input" v-model="f.key" placeholder="字段名" />
                <input class="kv-input" v-model="f.value" placeholder="值" />
                <button class="kv-del" @click="formData.splice(i, 1)">✕</button>
              </div>
              <button
                class="kv-add"
                @click="formData.push({ key: '', value: '' })"
              >
                + 添加字段
              </button>
            </div>
            <div v-if="bodyType === 'None'" class="body-none">
              该请求方法无请求体
            </div>
          </div>

          <!-- Auth -->
          <div v-if="reqTab === 'auth'" class="auth-panel">
            <div class="field-row">
              <label class="field-label">认证类型</label>
              <div class="seg-ctrl">
                <button
                  v-for="a in authTypes"
                  :key="a"
                  :class="{ active: authType === a }"
                  @click="authType = a"
                >
                  {{ a }}
                </button>
              </div>
            </div>
            <template v-if="authType === 'Bearer'">
              <div class="field-row mt8">
                <label class="field-label">Token</label>
                <input
                  class="field-input mono flex1"
                  v-model="authToken"
                  placeholder="eyJhbGciOiJIUzI1NiJ9..."
                />
              </div>
            </template>
            <template v-if="authType === 'Basic'">
              <div class="field-row mt8">
                <label class="field-label">Username</label>
                <input
                  class="field-input flex1"
                  v-model="authUser"
                  placeholder="用户名"
                />
              </div>
              <div class="field-row mt8">
                <label class="field-label">Password</label>
                <input
                  class="field-input flex1"
                  v-model="authPass"
                  placeholder="密码"
                  type="password"
                />
              </div>
            </template>
            <template v-if="authType === 'API Key'">
              <div class="field-row mt8">
                <label class="field-label">Key</label>
                <input
                  class="field-input flex1"
                  v-model="apiKeyName"
                  placeholder="X-API-Key"
                />
              </div>
              <div class="field-row mt8">
                <label class="field-label">Value</label>
                <input
                  class="field-input mono flex1"
                  v-model="apiKeyValue"
                  placeholder="your-api-key"
                />
              </div>
              <div class="field-row mt8">
                <label class="field-label">加入位置</label>
                <div class="seg-ctrl">
                  <button
                    :class="{ active: apiKeyIn === 'header' }"
                    @click="apiKeyIn = 'header'"
                  >
                    Header
                  </button>
                  <button
                    :class="{ active: apiKeyIn === 'query' }"
                    @click="apiKeyIn = 'query'"
                  >
                    Query
                  </button>
                </div>
              </div>
            </template>
            <div v-if="authType === 'None'" class="body-none">不使用认证</div>
          </div>
        </div>

        <!-- 历史记录 -->
        <div class="card history-card" v-if="history.length">
          <div class="card-header">
            <span class="card-label">历史记录</span>
            <button class="icon-btn sm danger" @click="history = []">
              清空
            </button>
          </div>
          <div class="history-list">
            <div
              class="history-item"
              v-for="(h, i) in history.slice(0, 10)"
              :key="i"
              @click="loadHistory(h)"
            >
              <span
                class="method-tag"
                :class="'method-' + h.method.toLowerCase()"
                >{{ h.method }}</span
              >
              <span class="history-url mono">{{ h.url }}</span>
              <span
                class="history-status"
                :class="h.status >= 400 ? 'err' : 'ok'"
                >{{ h.status }}</span
              >
            </div>
          </div>
        </div>
      </div>

      <!-- ══ 右侧：响应区 ══ -->
      <div class="http-right">
        <div class="card response-card">
          <!-- 空状态 -->
          <div v-if="!response && !error && !loading" class="resp-empty">
            <svg
              width="48"
              height="48"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="0.8"
              opacity="0.2"
            >
              <line x1="22" y1="2" x2="11" y2="13" />
              <polygon points="22 2 15 22 11 13 2 9 22 2" />
            </svg>
            <p>配置请求后点击发送</p>
          </div>

          <!-- 加载中 -->
          <div v-else-if="loading" class="resp-loading">
            <div class="loading-spinner" />
            <p>请求中...</p>
          </div>

          <!-- 错误 -->
          <div v-else-if="error" class="resp-error">
            <svg
              width="32"
              height="32"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="1.5"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="15" y1="9" x2="9" y2="15" />
              <line x1="9" y1="9" x2="15" y2="15" />
            </svg>
            <p>{{ error }}</p>
          </div>

          <!-- 响应内容 -->
          <template v-else-if="response">
            <!-- 响应状态栏 -->
            <div class="resp-status-bar">
              <span class="status-code" :class="statusClass"
                >{{ response.status }} {{ response.statusText }}</span
              >
              <span class="resp-meta">{{ response.time }}ms</span>
              <span class="resp-meta">{{ response.size }}</span>
              <div class="resp-actions">
                <button class="icon-btn sm" @click="copyResponse">
                  <svg
                    width="11"
                    height="11"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="9" y="9" width="13" height="13" rx="2" />
                    <path
                      d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"
                    />
                  </svg>
                  {{ copiedResp ? '已复制' : '复制' }}
                </button>
                <button class="icon-btn sm" @click="formatResponse">
                  格式化
                </button>
              </div>
            </div>

            <!-- 响应 Tabs -->
            <div class="inner-tabs">
              <button
                v-for="t in respTabs"
                :key="t.key"
                class="inner-tab"
                :class="{ active: respTab === t.key }"
                @click="respTab = t.key"
              >
                {{ t.label }}
              </button>
            </div>

            <!-- 响应体 -->
            <div v-if="respTab === 'body'" class="resp-body-wrap">
              <pre class="resp-body mono" v-html="responseBodyHtml" />
            </div>

            <!-- 响应头 -->
            <div v-if="respTab === 'headers'" class="resp-headers">
              <div
                class="rh-row"
                v-for="(val, key) in response.headers"
                :key="key"
              >
                <span class="rh-key">{{ key }}</span>
                <span class="rh-val mono">{{ val }}</span>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/* ── 请求配置 ── */
const method = ref('GET')
const url = ref('')
const reqTab = ref('params')
const methods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS']
const reqTabs = [
  { key: 'params', label: 'Params' },
  { key: 'headers', label: 'Headers' },
  { key: 'body', label: 'Body' },
  { key: 'auth', label: 'Auth' },
]

/* ── Params ── */
const params = ref([{ key: '', value: '' }])
const activeParams = computed(() => params.value.filter((p) => p.key))
function addParam() {
  params.value.push({ key: '', value: '' })
}
function removeParam(i) {
  params.value.splice(i, 1)
  syncUrl()
}
function syncUrl() {
  try {
    const base = url.value.split('?')[0]
    const qs = params.value
      .filter((p) => p.key)
      .map((p) => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`)
      .join('&')
    url.value = qs ? `${base}?${qs}` : base
  } catch {}
}

function parseUrlParams() {
  if (!url.value) {
    params.value = [{ key: '', value: '' }]
    return
  }

  try {
    // 判断是否包含问号
    const searchIndex = url.value.indexOf('?')
    if (searchIndex === -1) {
      // 没有参数时，保持列表为空（或留一个空行供输入）
      params.value = [{ key: '', value: '' }]
      return
    }

    // 截取问号后面的查询字符串
    const queryString = url.value.substring(searchIndex + 1)
    if (!queryString) {
      params.value = [{ key: '', value: '' }]
      return
    }

    // 使用 URLSearchParams 原生 API 解析
    const searchParams = new URLSearchParams(queryString)
    const newParams = []

    searchParams.forEach((value, key) => {
      newParams.push({ key, value })
    })

    // 吐给响应式变量，如果解析出了参数则赋值，否则留空行
    if (newParams.length > 0) {
      params.value = newParams
    } else {
      params.value = [{ key: '', value: '' }]
    }
  } catch (e) {
    console.error('URL 参数解析失败:', e)
  }
}

/* ── Headers ── */
const headers = ref([
  { key: 'Content-Type', value: 'application/json' },
  { key: 'Accept', value: 'application/json' },
])
const activeHeaders = computed(() => headers.value.filter((h) => h.key))
function addHeader() {
  headers.value.push({ key: '', value: '' })
}
function removeHeader(i) {
  headers.value.splice(i, 1)
}

/* ── Body ── */
const bodyTypes = ['JSON', 'Form', 'Raw', 'None']
const bodyType = ref('JSON')
const bodyRaw = ref('')
const bodyPlaceholder = computed(() =>
  bodyType.value === 'JSON' ? '{"key": "value"}' : '原始请求体',
)
const formData = ref([{ key: '', value: '' }])

/* ── Auth ── */
const authTypes = ['None', 'Bearer', 'Basic', 'API Key']
const authType = ref('None')
const authToken = ref('')
const authUser = ref('')
const authPass = ref('')
const apiKeyName = ref('X-API-Key')
const apiKeyValue = ref('')
const apiKeyIn = ref('header')

/* ── 响应 ── */
const loading = ref(false)
const response = ref(null)
const error = ref('')
const copiedResp = ref(false)
const respTab = ref('body')
const respTabs = [
  { key: 'body', label: '响应体' },
  { key: 'headers', label: '响应头' },
]
const history = ref([])

const statusClass = computed(() => {
  if (!response.value) return ''
  const s = response.value.status
  if (s < 300) return 'status-2xx'
  if (s < 400) return 'status-3xx'
  if (s < 500) return 'status-4xx'
  return 'status-5xx'
})

const responseBodyHtml = computed(() => {
  if (!response.value?.body) return ''
  const raw = response.value.body
  try {
    const obj = JSON.parse(raw)
    const json = JSON.stringify(obj, null, 2)
    return json
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"([^"]+)":/g, '<span class="json-key">"$1"</span>:')
      .replace(/: "([^"]*)"/g, ': <span class="json-str">"$1"</span>')
      .replace(/: (\d+\.?\d*)/g, ': <span class="json-num">$1</span>')
      .replace(/: (true|false)/g, ': <span class="json-bool">$1</span>')
      .replace(/: (null)/g, ': <span class="json-null">$1</span>')
  } catch {
    return raw
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
  }
})

/* ── 发送请求 ── */
async function sendRequest() {
  if (!url.value.trim() || loading.value) return
  loading.value = true
  error.value = ''
  response.value = null
  const start = Date.now()

  try {
    // 构建请求头
    const reqHeaders = {}
    headers.value
      .filter((h) => h.key)
      .forEach((h) => {
        reqHeaders[h.key] = h.value
      })

    // Auth
    if (authType.value === 'Bearer' && authToken.value)
      reqHeaders['Authorization'] = `Bearer ${authToken.value}`
    if (authType.value === 'Basic' && authUser.value)
      reqHeaders['Authorization'] =
        `Basic ${btoa(authUser.value + ':' + authPass.value)}`
    if (
      authType.value === 'API Key' &&
      apiKeyName.value &&
      apiKeyIn.value === 'header'
    )
      reqHeaders[apiKeyName.value] = apiKeyValue.value

    // 构建请求 URL（API Key in query）
    let reqUrl = url.value
    if (
      authType.value === 'API Key' &&
      apiKeyName.value &&
      apiKeyIn.value === 'query'
    ) {
      const sep = reqUrl.includes('?') ? '&' : '?'
      reqUrl += `${sep}${apiKeyName.value}=${apiKeyValue.value}`
    }

    // 构建请求体
    let body = undefined
    if (!['GET', 'HEAD'].includes(method.value)) {
      if (bodyType.value === 'JSON' && bodyRaw.value) {
        body = bodyRaw.value
        if (!reqHeaders['Content-Type'])
          reqHeaders['Content-Type'] = 'application/json'
      } else if (bodyType.value === 'Raw' && bodyRaw.value) {
        body = bodyRaw.value
      } else if (bodyType.value === 'Form') {
        const fd = new FormData()
        formData.value
          .filter((f) => f.key)
          .forEach((f) => fd.append(f.key, f.value))
        body = fd
        delete reqHeaders['Content-Type'] // FormData 自动设置
      }
    }

    const resp = await fetch(reqUrl, {
      method: method.value,
      headers: reqHeaders,
      body,
      signal: AbortSignal.timeout(30000),
    })

    const elapsed = Date.now() - start
    const text = await resp.text()
    const respHeaders = {}
    resp.headers.forEach((v, k) => {
      respHeaders[k] = v
    })

    // 计算大小
    const bytes = new TextEncoder().encode(text).length
    const size = bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`

    response.value = {
      status: resp.status,
      statusText: resp.statusText,
      headers: respHeaders,
      body: text,
      time: elapsed,
      size,
    }

    // 加入历史
    history.value.unshift({
      method: method.value,
      url: url.value,
      status: resp.status,
    })
    if (history.value.length > 20) history.value.pop()
  } catch (e) {
    if (e.name === 'TimeoutError') error.value = '请求超时（30s）'
    else if (
      e.message.includes('Failed to fetch') ||
      e.message.includes('NetworkError')
    )
      error.value =
        '网络错误 — 请检查 URL 是否正确，或服务器是否允许跨域（CORS）'
    else error.value = e.message
  } finally {
    loading.value = false
  }
}

function loadHistory(h) {
  method.value = h.method
  url.value = h.url
}

async function copyResponse() {
  if (!response.value?.body) return
  await navigator.clipboard.writeText(response.value.body).catch(() => {})
  copiedResp.value = true
  setTimeout(() => {
    copiedResp.value = false
  }, 2000)
}

function formatResponse() {
  if (!response.value?.body) return
  try {
    response.value.body = JSON.stringify(
      JSON.parse(response.value.body),
      null,
      2,
    )
  } catch {}
}
</script>

<style lang="scss">
@import '../shared.scss';
</style>

<style lang="scss" scoped>
.http-layout {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.http-left {
  width: 500px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.08);
    border-radius: 2px;
  }
}
.http-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

/* ── URL Bar ── */
.url-card {
  padding: 0 !important;
  overflow: visible;
}
.url-bar {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 8px;
  gap: 6px;
}

.method-select {
  height: 34px;
  padding: 0 8px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  font-size: 12px;
  font-weight: 700;
  background: var(--el-bg-color, #fff);
  cursor: pointer;
  flex-shrink: 0;
  min-width: 90px;
  &.method-get {
    color: #16a34a;
    border-color: #86efac;
    background: #f0fdf4;
  }
  &.method-post {
    color: #d97706;
    border-color: #fcd34d;
    background: #fffbeb;
  }
  &.method-put {
    color: #2563eb;
    border-color: #93c5fd;
    background: #eff6ff;
  }
  &.method-patch {
    color: #7c3aed;
    border-color: #c4b5fd;
    background: #f5f3ff;
  }
  &.method-delete {
    color: #dc2626;
    border-color: #fca5a5;
    background: #fff1f2;
  }
  &.method-head,
  &.method-options {
    color: var(--el-text-color-secondary, #909399);
  }
}

.url-input {
  flex: 1;
  height: 34px;
  padding: 0 10px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
  background: var(--el-bg-color, #fff);
  outline: none;
  transition: border-color 0.15s;
  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }
}

.send-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 34px;
  padding: 0 16px;
  border: none;
  border-radius: 7px;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  flex-shrink: 0;
  &:hover:not(:disabled) {
    background: var(--el-color-primary-dark-2, #337ecc);
  }
  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
  }
}
.spin-icon {
  display: inline-block;
  animation: spin 0.6s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* ── Req Config Card ── */
.req-config-card {
  flex-shrink: 0;
}

/* ── Inner Tabs ── */
.inner-tabs {
  display: flex;
  gap: 0;
  padding: 0 8px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
}
.inner-tab {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  font-size: 11px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  transition: all 0.15s;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  &:hover {
    color: var(--el-color-primary, #409eff);
  }
  &.active {
    color: var(--el-color-primary, #409eff);
    border-bottom-color: var(--el-color-primary, #409eff);
    font-weight: 600;
  }
}
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
}

/* ── KV Panel ── */
.kv-panel {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}
.kv-head {
  display: grid;
  grid-template-columns: 1fr 1fr 24px;
  padding: 0 4px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
.kv-row {
  display: grid;
  grid-template-columns: 1fr 1fr 24px;
  gap: 6px;
}
.kv-input {
  height: 28px;
  padding: 0 8px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 5px;
  font-size: 11px;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  outline: none;
  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }
}
.kv-del {
  width: 24px;
  height: 28px;
  border: none;
  background: transparent;
  color: var(--el-text-color-placeholder, #c0c4cc);
  cursor: pointer;
  font-size: 11px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  &:hover {
    background: #fff1f2;
    color: #dc2626;
  }
}
.kv-add {
  align-self: flex-start;
  margin-top: 2px;
  padding: 4px 10px;
  border-radius: 5px;
  border: 1px dashed var(--el-border-color, #dcdfe6);
  background: transparent;
  font-size: 11px;
  color: var(--el-color-primary, #409eff);
  cursor: pointer;
  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

/* ── Body ── */
.body-panel {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.body-type-row {
  display: flex;
  gap: 16px;
}
.radio-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  input[type='radio'] {
    accent-color: var(--el-color-primary, #409eff);
    cursor: pointer;
  }
}
.body-textarea {
  width: 100%;
  min-height: 120px;
  resize: vertical;
  padding: 8px 10px;
  box-sizing: border-box;
  border: 1px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  font-size: 11px;
  line-height: 1.6;
  background: var(--el-fill-color-lighter, #fafafa);
  color: var(--el-text-color-primary, #303133);
  outline: none;
  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }
}
.body-none {
  padding: 12px;
  font-size: 12px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  text-align: center;
}

/* ── Auth ── */
.auth-panel {
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 0;
}
.mt8 {
  margin-top: 8px;
}

/* ── History ── */
.history-card {
  flex-shrink: 0;
}
.history-list {
  display: flex;
  flex-direction: column;
  max-height: 160px;
  overflow-y: auto;
}
.history-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 12px;
  cursor: pointer;
  transition: background 0.12s;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  &:last-child {
    border-bottom: none;
  }
  &:hover {
    background: var(--el-fill-color-lighter, #fafafa);
  }
}
.history-url {
  flex: 1;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.history-status {
  font-size: 11px;
  font-weight: 700;
  font-family: monospace;
  &.ok {
    color: #16a34a;
  }
  &.err {
    color: #dc2626;
  }
}

/* ── Method Tag ── */
.method-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
  &.method-get {
    background: #f0fdf4;
    color: #16a34a;
  }
  &.method-post {
    background: #fffbeb;
    color: #d97706;
  }
  &.method-put {
    background: #eff6ff;
    color: #2563eb;
  }
  &.method-patch {
    background: #f5f3ff;
    color: #7c3aed;
  }
  &.method-delete {
    background: #fff1f2;
    color: #dc2626;
  }
  &.method-head,
  &.method-options {
    background: var(--el-fill-color, #f0f2f5);
    color: var(--el-text-color-secondary, #909399);
  }
}

/* ── Response ── */
.response-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
}
.resp-empty,
.resp-loading,
.resp-error {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-size: 12px;
}
.resp-error {
  color: #dc2626;
}
.loading-spinner {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  border: 3px solid var(--el-border-color-lighter, #ebeef5);
  border-top-color: var(--el-color-primary, #409eff);
  animation: spin 0.7s linear infinite;
}
.resp-status-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
}
.status-code {
  font-size: 13px;
  font-weight: 700;
  font-family: monospace;
  padding: 2px 10px;
  border-radius: 6px;
  &.status-2xx {
    background: #dcfce7;
    color: #16a34a;
  }
  &.status-3xx {
    background: #dbeafe;
    color: #2563eb;
  }
  &.status-4xx {
    background: #fff1f2;
    color: #dc2626;
  }
  &.status-5xx {
    background: #fef3c7;
    color: #d97706;
  }
}
.resp-meta {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
  font-family: monospace;
}
.resp-actions {
  display: flex;
  gap: 6px;
  margin-left: auto;
}
.resp-body-wrap {
  flex: 1;
  min-height: 0;
  overflow: auto;
  &::-webkit-scrollbar {
    width: 5px;
    height: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.08);
    border-radius: 3px;
  }
}
.resp-body {
  margin: 0;
  padding: 14px;
  font-size: 12px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--el-text-color-primary, #303133);
}
:deep(.json-key) {
  color: var(--el-color-primary, #409eff);
}
:deep(.json-str) {
  color: #16a34a;
}
:deep(.json-num) {
  color: #d97706;
}
:deep(.json-bool) {
  color: #db2777;
  font-weight: 600;
}
:deep(.json-null) {
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-style: italic;
}
.resp-headers {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.08);
    border-radius: 2px;
  }
}
.rh-row {
  display: grid;
  grid-template-columns: 200px 1fr;
  padding: 6px 14px;
  font-size: 11px;
  gap: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  &:last-child {
    border-bottom: none;
  }
}
.rh-key {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
  word-break: break-all;
}
.rh-val {
  color: var(--el-text-color-regular, #606266);
  word-break: break-all;
}
</style>
