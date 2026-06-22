<template>
  <div class="panel-wrap">
    <div class="stress-layout">

      <!-- ══ 左侧：请求配置 + 并发设置 ══ -->
      <div class="stress-left">

        <!-- URL Bar -->
        <div class="card req-card">
          <div class="card-header">
            <span class="card-label">请求配置</span>
          </div>
          <div class="url-bar">
            <select class="method-select" v-model="method" :class="'method-' + method.toLowerCase()">
              <option v-for="m in methods" :key="m" :value="m">{{ m }}</option>
            </select>
            <input
              class="url-input mono"
              v-model="url"
              placeholder="https://api.example.com/v1/orders"
            />
          </div>

          <!-- 请求配置 Tabs -->
          <div class="inner-tabs">
            <button v-for="t in reqTabs" :key="t.key" class="inner-tab" :class="{ active: reqTab === t.key }"
                    @click="reqTab = t.key">
              {{ t.label }}
              <span v-if="t.key === 'headers' && activeHeaders.length" class="badge">{{ activeHeaders.length }}</span>
              <span v-if="t.key === 'params'  && activeParams.length" class="badge">{{ activeParams.length }}</span>
            </button>
          </div>

          <!-- Query Params -->
          <div v-if="reqTab === 'params'" class="kv-panel">
            <div class="kv-head"><span>Key</span><span>Value</span><span></span></div>
            <div class="kv-row" v-for="(p, i) in params" :key="i">
              <input class="kv-input" v-model="p.key" placeholder="参数名" />
              <input class="kv-input" v-model="p.value" placeholder="参数值 / {{动态变量}}" />
              <button class="kv-del" @click="params.splice(i,1)">✕</button>
            </div>
            <button class="kv-add" @click="params.push({key:'',value:''})">+ 添加参数</button>
          </div>

          <!-- Headers -->
          <div v-if="reqTab === 'headers'" class="kv-panel">
            <div class="kv-head"><span>Header</span><span>Value</span><span></span></div>
            <div class="kv-row" v-for="(h, i) in headers" :key="i">
              <input class="kv-input" v-model="h.key" placeholder="Header 名" list="stress-headers" />
              <input class="kv-input" v-model="h.value" placeholder="值" />
              <button class="kv-del" @click="headers.splice(i,1)">✕</button>
            </div>
            <button class="kv-add" @click="headers.push({key:'',value:''})">+ 添加 Header</button>
            <datalist id="stress-headers">
              <option value="Content-Type" />
              <option value="Authorization" />
              <option value="Accept" />
              <option value="X-Request-ID" />
              <option value="X-API-Key" />
            </datalist>
          </div>

          <!-- Body with dynamic fields -->
          <div v-if="reqTab === 'body'" class="body-panel">
            <div class="body-type-row">
              <label v-for="bt in bodyTypes" :key="bt" class="radio-label">
                <input type="radio" v-model="bodyType" :value="bt" />{{ bt }}
              </label>
            </div>
            <!-- Dynamic KV body -->
            <template v-if="bodyType === 'JSON-KV'">
              <div class="dyn-tip">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                支持动态变量：<code v-pre>{{uuid}}</code> <code v-pre>{{rand_int:1,9999}}</code> <code v-pre>{{rand_str:8}}</code>
                <code v-pre>{{now_ms}}</code> <code v-pre>{{now_iso}}</code> <code v-pre>{{seq}}</code>
              </div>
              <div class="kv-head" style="grid-template-columns:120px 1fr 50px 22px">
                <span>字段名</span><span>值 / 变量</span><span>动态</span><span></span></div>
              <div class="kv-row dyn-row" v-for="(f, i) in dynFields" :key="i"
                   style="grid-template-columns:120px 1fr 50px 22px">
                <input class="kv-input" v-model="f.key" placeholder="字段名" />
                <div class="kv-val-wrap">
                  <input class="kv-input" v-model="f.value" :placeholder="f.isDyn ? '{{uuid}}' : '固定值'" />
                  <span v-if="hasDynVar(f.value)" class="dyn-badge">动态</span>
                </div>
                <div class="kv-toggle" :class="{ on: f.isDyn }" @click="f.isDyn = !f.isDyn">
                  <div class="kv-toggle-dot" />
                </div>
                <button class="kv-del" @click="dynFields.splice(i,1)">✕</button>
              </div>
              <button class="kv-add" @click="dynFields.push({key:'',value:'',isDyn:false})">+ 添加字段</button>
            </template>
            <!-- Raw JSON body -->
            <textarea
              v-if="bodyType === 'JSON'"
              class="body-textarea mono"
              v-model="bodyRaw"
              placeholder='{"key": "value"}'
              spellcheck="false"
            />
            <textarea
              v-if="bodyType === 'Raw'"
              class="body-textarea mono"
              v-model="bodyRaw"
              placeholder="原始请求体"
              spellcheck="false"
            />
            <div v-if="bodyType === 'None'" class="body-none">该请求方法无请求体</div>
          </div>

          <!-- Auth -->
          <div v-if="reqTab === 'auth'" class="auth-panel">
            <div class="field-row">
              <label class="field-label">认证类型</label>
              <div class="seg-ctrl">
                <button v-for="a in authTypes" :key="a" :class="{ active: authType === a }" @click="authType = a">{{ a
                  }}
                </button>
              </div>
            </div>
            <template v-if="authType === 'Bearer'">
              <div class="field-row mt8">
                <label class="field-label">Token</label>
                <input class="field-input mono flex1" v-model="authToken" placeholder="eyJhbGciOiJIUzI1NiJ9..." />
              </div>
            </template>
            <template v-if="authType === 'Basic'">
              <div class="field-row mt8">
                <label class="field-label">Username</label>
                <input class="field-input flex1" v-model="authUser" placeholder="用户名" />
              </div>
              <div class="field-row mt8">
                <label class="field-label">Password</label>
                <input class="field-input flex1" v-model="authPass" type="password" placeholder="密码" />
              </div>
            </template>
            <template v-if="authType === 'API Key'">
              <div class="field-row mt8">
                <label class="field-label">Key</label>
                <input class="field-input flex1" v-model="apiKeyName" placeholder="X-API-Key" />
              </div>
              <div class="field-row mt8">
                <label class="field-label">Value</label>
                <input class="field-input mono flex1" v-model="apiKeyValue" placeholder="your-api-key" />
              </div>
            </template>
            <div v-if="authType === 'None'" class="body-none">不使用认证</div>
          </div>

          <!-- Pre-request Script -->
          <div v-if="reqTab === 'script'" class="script-panel">
            <div class="script-toolbar">
              <div class="script-info">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10" />
                  <line x1="12" y1="8" x2="12" y2="12" />
                  <line x1="12" y1="16" x2="12.01" y2="16" />
                </svg>
                用 <code class="inline-code">pm.environment.set("key", value)</code> 注册变量，在 Body/Params 中用 <span
                class="inline-code" v-pre>{{变量名}}</span> 引用
              </div>
              <div class="script-actions">
                <button class="icon-btn sm" @click="runPreScript" :class="{ running: scriptRunning }">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                  {{ scriptRunning ? '执行中...' : '测试执行' }}
                </button>
                <button class="icon-btn sm" @click="loadScriptSample">示例脚本</button>
                <button class="icon-btn sm danger" @click="preScript = ''; scriptEnvVars = {}; scriptError = ''">清空
                </button>
              </div>
            </div>

            <!-- 已注入变量预览 -->
            <div class="script-vars" v-if="Object.keys(scriptEnvVars).length">
              <div class="vars-title">
                <span>已注入变量</span>
                <span class="vars-count">{{ Object.keys(scriptEnvVars).length }} 个</span>
              </div>
              <div class="vars-grid">
                <div class="var-item" v-for="(val, key) in scriptEnvVars" :key="key">
                  <span class="var-key mono">{{ wrapBraces(key) }}</span>
                  <span class="var-val mono">{{ String(val).length > 40 ? String(val).slice(0, 40) + '...' : val
                    }}</span>
                </div>
              </div>
            </div>

            <!-- 错误提示 -->
            <div class="script-error" v-if="scriptError">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <line x1="15" y1="9" x2="9" y2="15" />
                <line x1="9" y1="9" x2="15" y2="15" />
              </svg>
              {{ scriptError }}
            </div>

            <!-- 代码编辑区 -->
            <textarea
              class="script-editor mono"
              v-model="preScript"
              spellcheck="false"
            />
          </div>

        </div>

        <!-- 并发设置 -->
        <div class="card">
          <div class="card-header"><span class="card-label">并发设置</span></div>
          <div class="card-body gap10">
            <div class="concur-grid">
              <div class="concur-item">
                <label class="concur-label">并发数</label>
                <div class="concur-input-wrap">
                  <button class="concur-stepper" @click="concurrency = Math.max(1, concurrency-1)">−</button>
                  <input class="concur-input" type="number" v-model.number="concurrency" min="1" max="200" />
                  <button class="concur-stepper" @click="concurrency = Math.min(200, concurrency+1)">+</button>
                </div>
                <span class="concur-unit">线程</span>
              </div>
              <div class="concur-item">
                <label class="concur-label">总请求数</label>
                <div class="concur-input-wrap">
                  <button class="concur-stepper" @click="totalRequests = Math.max(1, totalRequests-10)">−</button>
                  <input class="concur-input" type="number" v-model.number="totalRequests" min="1" max="10000" />
                  <button class="concur-stepper" @click="totalRequests = Math.min(10000, totalRequests+10)">+</button>
                </div>
                <span class="concur-unit">次</span>
              </div>
              <div class="concur-item">
                <label class="concur-label">超时时间</label>
                <div class="concur-input-wrap">
                  <button class="concur-stepper" @click="timeout = Math.max(1, timeout-1)">−</button>
                  <input class="concur-input" type="number" v-model.number="timeout" min="1" max="120" />
                  <button class="concur-stepper" @click="timeout = Math.min(120, timeout+1)">+</button>
                </div>
                <span class="concur-unit">秒</span>
              </div>
            </div>

            <div class="field-row">
              <label class="field-label">请求间隔</label>
              <div class="seg-ctrl">
                <button :class="{ active: delayMode === 'none' }" @click="delayMode = 'none'">无间隔</button>
                <button :class="{ active: delayMode === 'fixed' }" @click="delayMode = 'fixed'">固定</button>
                <button :class="{ active: delayMode === 'random' }" @click="delayMode = 'random'">随机</button>
              </div>
              <template v-if="delayMode === 'fixed'">
                <input class="field-input mono" style="width:64px" type="number" v-model.number="delayFixed" min="0" />
                ms
              </template>
              <template v-if="delayMode === 'random'">
                <input class="field-input mono" style="width:54px" type="number" v-model.number="delayMin" min="0" />
                <span style="font-size:11px;color:var(--el-text-color-secondary)">~</span>
                <input class="field-input mono" style="width:54px" type="number" v-model.number="delayMax" min="0" /> ms
              </template>
            </div>

            <div class="field-row">
              <label class="field-label">失败重试</label>
              <input class="field-input mono" style="width:60px;text-align:center" type="number"
                     v-model.number="retryCount" min="0" max="5" />
              <span style="font-size:11px;color:var(--el-text-color-secondary)">次</span>
            </div>
          </div>
        </div>

        <!-- 进度 + 控制 -->
        <div class="card">
          <div class="card-header"><span class="card-label">执行控制</span></div>
          <div class="card-body">
            <!-- 进度条 -->
            <div class="progress-wrap">
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: progressPercent + '%' }" />
              </div>
              <div class="progress-row">
                <span>已完成 {{ completedCount }} / {{ totalRequests }}</span>
                <span class="run-status" :class="runStatusClass">{{ runStatusText }}</span>
              </div>
            </div>

            <!-- 按钮 -->
            <div class="ctrl-btns">
              <template v-if="running">
                <button class="ctrl-btn pause" @click="togglePause" v-if="!paused">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="6" y="4" width="4" height="16" />
                    <rect x="14" y="4" width="4" height="16" />
                  </svg>
                  暂停
                </button>
                <button class="ctrl-btn resume" @click="togglePause" v-else>
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polygon points="5 3 19 12 5 21 5 3" />
                  </svg>
                  继续
                </button>
                <button class="ctrl-btn stop" @click="stopTest">
                  <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="3" width="18" height="18" rx="2" />
                  </svg>
                  中止
                </button>
              </template>
              <button v-else class="ctrl-btn start" @click="startTest" :disabled="!url">
                <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                  <polygon points="5 3 19 12 5 21 5 3" />
                </svg>
                开始压测
              </button>
            </div>
          </div>
        </div>

      </div>

      <!-- ══ 右侧：结果 ══ -->
      <div class="stress-right">

        <!-- 统计卡片 -->
        <div class="stats-bar">
          <div class="stat-card">
            <span class="stat-num">{{ completedCount }}</span>
            <span class="stat-lbl">总请求</span>
          </div>
          <div class="stat-card success">
            <span class="stat-num">{{ successCount }}</span>
            <span class="stat-lbl">成功</span>
          </div>
          <div class="stat-card fail">
            <span class="stat-num">{{ failCount }}</span>
            <span class="stat-lbl">失败</span>
          </div>
          <div class="stat-card time">
            <span class="stat-num">{{ avgTimeText }}</span>
            <span class="stat-lbl">平均耗时</span>
          </div>
          <div class="stat-card time">
            <span class="stat-num">{{ p95TimeText }}</span>
            <span class="stat-lbl">P95 耗时</span>
          </div>
          <div class="stat-card">
            <span class="stat-num">{{ tpsText }}</span>
            <span class="stat-lbl">TPS</span>
          </div>
        </div>

        <!-- 延迟分布 -->
        <div class="dist-chart" v-if="completedCount > 0">
          <div class="dist-title">响应时间分布</div>
          <div class="dist-bars">
            <div class="dist-row" v-for="b in timeBuckets" :key="b.label">
              <span class="dist-label">{{ b.label }}</span>
              <div class="dist-bar-wrap">
                <div class="dist-bar-fill" :class="b.cls" :style="{ width: b.pct + '%' }" />
              </div>
              <span class="dist-count">{{ b.count }}</span>
            </div>
          </div>
        </div>

        <!-- 结果列表 Tabs -->
        <div class="inner-tabs" style="padding:0 12px">
          <button class="inner-tab" :class="{ active: resultTab === 'all' }" @click="resultTab = 'all'">全部
            ({{ results.length }})
          </button>
          <button class="inner-tab" :class="{ active: resultTab === 'success' }" @click="resultTab = 'success'">成功
            ({{ successCount }})
          </button>
          <button class="inner-tab" :class="{ active: resultTab === 'fail' }" @click="resultTab = 'fail'">失败
            ({{ failCount }})
          </button>
          <div style="flex:1" />
          <button class="icon-btn sm" @click="clearResults" :disabled="running">
            <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6" />
              <path d="M19 6l-1 14H6L5 6" />
            </svg>
            清空
          </button>
        </div>

        <!-- 空状态 -->
        <div v-if="filteredResults.length === 0" class="result-empty">
          <svg width="44" height="44" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
               opacity="0.2">
            <line x1="22" y1="2" x2="11" y2="13" />
            <polygon points="22 2 15 22 11 13 2 9 22 2" />
          </svg>
          <p>{{ results.length === 0 ? '点击「开始压测」发起请求' : '暂无符合条件的结果' }}</p>
        </div>

        <!-- 结果列表 -->
        <div v-else class="result-list">
          <div
            v-for="r in filteredResults"
            :key="r.id"
            class="result-item"
            :class="r.success ? 'ok' : 'fail'"
            @click="r.expanded = !r.expanded"
          >
            <div class="ri-row">
              <div class="ri-idx" :class="r.success ? 'ok' : 'fail'">#{{ r.seq }}</div>
              <span class="ri-status" :class="r.success ? 'ok' : 'fail'">{{ r.status }}</span>
              <span class="ri-thread">T-{{ String(r.thread).padStart(2, '0') }}</span>
              <span class="ri-url mono">{{ r.urlShort }}</span>
              <span class="ri-time" :class="r.elapsed > 1000 ? 'slow' : ''">{{ r.elapsed }}ms</span>
              <svg class="ri-chevron" :class="{ open: r.expanded }" width="10" height="10" viewBox="0 0 24 24"
                   fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9" />
              </svg>
            </div>
            <!-- 展开详情 -->
            <template v-if="r.expanded">
              <div class="ri-detail">
                <div class="ri-detail-row">
                  <span class="ri-detail-key">请求体</span>
                  <pre class="ri-detail-val mono">{{ r.reqBody }}</pre>
                </div>
                <div class="ri-detail-row" v-if="r.respBody">
                  <span class="ri-detail-key">响应体</span>
                  <pre class="ri-detail-val mono">{{ r.respBody }}</pre>
                </div>
                <div class="ri-detail-row" v-if="r.error">
                  <span class="ri-detail-key">错误</span>
                  <pre class="ri-detail-val mono err">{{ r.error }}</pre>
                </div>
              </div>
            </template>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

/* ============================================================
   请求配置（与 HttpClient 相同）
   ============================================================ */
const method = ref('POST')
const url = ref('')
const reqTab = ref('body')
const methods = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS']
const reqTabs = [
  { key: 'params', label: 'Params' },
  { key: 'headers', label: 'Headers' },
  { key: 'body', label: 'Body' },
  { key: 'auth', label: 'Auth' },
  { key: 'script', label: 'Pre-Script' }
]

const params = ref([{ key: '', value: '' }])
const headers = ref([
  { key: 'Content-Type', value: 'application/json' },
  { key: 'Accept', value: 'application/json' }
])
const activeParams = computed(() => params.value.filter(p => p.key))
const activeHeaders = computed(() => headers.value.filter(h => h.key))

const bodyTypes = ['JSON-KV', 'JSON', 'Raw', 'None']
const bodyType = ref('JSON-KV')
const bodyRaw = ref('')

// 动态字段
const dynFields = ref([
  { key: 'orderId', value: '{{uuid}}', isDyn: true },
  { key: 'amount', value: '{{rand_int:100,9999}}', isDyn: true },
  { key: 'timestamp', value: '{{now_ms}}', isDyn: true }
])

// 预处理脚本
const preScript = ref('')
const scriptEnvVars = ref({})   // pm.environment 注入的变量
const scriptError = ref('')
const scriptRunning = ref(false)

// 内置变量（优先级低于脚本变量）
const BUILT_IN_VARS = {
  uuid: () => crypto.randomUUID ? crypto.randomUUID() : randHex(32),
  now_ms: () => String(Date.now()),
  now_iso: () => new Date().toISOString(),
  seq: () => String(++seqCounter)
}

function hasDynVar(val) {
  return /\{\{.+?\}\}/.test(val)
}

// 在模板里安全显示 {{key}} 格式，避免 Vue 编译器误解析
function wrapBraces(key) {
  return '\u007B\u007B' + key + '\u007D\u007D'
}

const authTypes = ['None', 'Bearer', 'Basic', 'API Key']
const authType = ref('None')
const authToken = ref('')
const authUser = ref('')
const authPass = ref('')
const apiKeyName = ref('X-API-Key')
const apiKeyValue = ref('')

/* ============================================================
   并发设置
   ============================================================ */
const concurrency = ref(10)
const totalRequests = ref(100)
const timeout = ref(30)
const delayMode = ref('none')    // none | fixed | random
const delayFixed = ref(100)
const delayMin = ref(100)
const delayMax = ref(500)
const retryCount = ref(0)

/* ============================================================
   运行状态
   ============================================================ */
const running = ref(false)
const paused = ref(false)
const completedCount = ref(0)
const results = ref([])
const resultTab = ref('all')
const startTime = ref(0)

let abortFlag = false
let pauseFlag = false

const successCount = computed(() => results.value.filter(r => r.success).length)
const failCount = computed(() => results.value.filter(r => !r.success).length)

const progressPercent = computed(() =>
  totalRequests.value > 0 ? Math.round(completedCount.value / totalRequests.value * 100) : 0
)

const runStatusClass = computed(() => {
  if (running.value && paused.value) return 'status-paused'
  if (running.value) return 'status-running'
  if (completedCount.value > 0) return 'status-done'
  return ''
})
const runStatusText = computed(() => {
  if (running.value && paused.value) return '已暂停'
  if (running.value) return '运行中…'
  if (completedCount.value > 0 && completedCount.value >= totalRequests.value) return '已完成'
  if (completedCount.value > 0) return '已中止'
  return '就绪'
})

// 平均耗时
const avgTimeText = computed(() => {
  const done = results.value.filter(r => r.elapsed > 0)
  if (!done.length) return '—'
  const avg = done.reduce((s, r) => s + r.elapsed, 0) / done.length
  return avg < 1000 ? `${Math.round(avg)}ms` : `${(avg / 1000).toFixed(2)}s`
})

// P95
const p95TimeText = computed(() => {
  const times = results.value.map(r => r.elapsed).filter(t => t > 0).sort((a, b) => a - b)
  if (!times.length) return '—'
  const idx = Math.floor(times.length * 0.95)
  const p95 = times[Math.min(idx, times.length - 1)]
  return p95 < 1000 ? `${p95}ms` : `${(p95 / 1000).toFixed(2)}s`
})

// TPS
const tpsText = computed(() => {
  if (!startTime.value || completedCount.value === 0) return '—'
  const elapsed = (Date.now() - startTime.value) / 1000
  if (elapsed < 0.1) return '—'
  return `${(completedCount.value / elapsed).toFixed(1)}`
})

// 响应时间分布
const timeBuckets = computed(() => {
  const times = results.value.map(r => r.elapsed).filter(t => t >= 0)
  const total = times.length || 1
  const buckets = [
    { label: '<100ms', cls: 'b-green', min: 0, max: 100, count: 0 },
    { label: '100-300', cls: 'b-blue', min: 100, max: 300, count: 0 },
    { label: '300-1000', cls: 'b-amber', min: 300, max: 1000, count: 0 },
    { label: '>1000ms', cls: 'b-red', min: 1000, max: Infinity, count: 0 }
  ]
  for (const t of times) {
    for (const b of buckets) {
      if (t >= b.min && t < b.max) {
        b.count++
        break
      }
    }
  }
  const maxCount = Math.max(...buckets.map(b => b.count), 1)
  return buckets.map(b => ({ ...b, pct: Math.round(b.count / maxCount * 100) }))
})

const filteredResults = computed(() => {
  const list = resultTab.value === 'success' ? results.value.filter(r => r.success)
    : resultTab.value === 'fail' ? results.value.filter(r => !r.success)
      : results.value
  // 最新的在前，最多显示 500 条
  return [...list].reverse().slice(0, 500)
})

/* ============================================================
   动态变量解析
   ============================================================ */
let seqCounter = 0

function resolveVar(val, envSnapshot = null) {
  const env = envSnapshot || scriptEnvVars.value
  return val.replace(/\{\{(.+?)\}\}/g, (orig, expr) => {
    const e = expr.trim()
    // 1. 优先查脚本注入的变量（每次请求重新执行脚本后的快照）
    if (Object.prototype.hasOwnProperty.call(env, e)) {
      return String(env[e])
    }
    // 2. 内置变量
    if (e === 'uuid') return crypto.randomUUID ? crypto.randomUUID() : randHex(32)
    if (e === 'now_ms') return String(Date.now())
    if (e === 'now_iso') return new Date().toISOString()
    if (e === 'seq') return String(++seqCounter)
    if (e.startsWith('rand_int:')) {
      const [min, max] = e.slice(9).split(',').map(Number)
      return String(Math.floor(Math.random() * (max - min + 1)) + min)
    }
    if (e.startsWith('rand_str:')) {
      const len = parseInt(e.slice(9)) || 8
      return randStr(len)
    }
    return orig  // 未识别则原样保留
  })
}

/* ── 执行预处理脚本，返回当次的环境变量快照 ── */
function execPreScript() {
  if (!preScript.value.trim()) return {}
  const envStore = {}
  // 构造 pm 对象（与 Postman API 兼容）
  const pm = {
    environment: {
      set(key, value) {
        envStore[key] = value
      },
      get(key) {
        return envStore[key]
      },
      has(key) {
        return key in envStore
      },
      unset(key) {
        delete envStore[key]
      }
    },
    variables: {
      set(key, value) {
        envStore[key] = value
      },
      get(key) {
        return envStore[key]
      }
    }
  }
  try {
    // eslint-disable-next-line no-new-func
    new Function('pm', preScript.value)(pm)
    return envStore
  } catch (e) {
    throw new Error('脚本执行错误：' + e.message)
  }
}

/* ── 测试执行（不发请求，只跑脚本看变量） ── */
async function runPreScript() {
  if (scriptRunning.value) return
  scriptRunning.value = true
  scriptError.value = ''
  try {
    const env = execPreScript()
    scriptEnvVars.value = env
  } catch (e) {
    scriptError.value = e.message
  } finally {
    scriptRunning.value = false
  }
}

/* ── 示例脚本 ── */
function loadScriptSample() {
  preScript.value = `// 随机字符串
function randomString(length) {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
}

// 随机数字串
function randomNumber(length) {
  let result = '';
  for (let i = 0; i < length; i++) result += Math.floor(Math.random() * 10);
  return result;
}

// 日期工具
function formatDate(date) {
  return date.toISOString().substring(0, 10);
}

const arrival = new Date();
arrival.setDate(arrival.getDate() + Math.floor(Math.random() * 30));
const departure = new Date(arrival);
departure.setDate(departure.getDate() + 1);

const signatureTypes = ['RC','FOLIO','PGS','IDDOCUMENT','PAYMENT','PAIDOUT','BI','RO','EXCHANGE'];

// 注册环境变量
pm.environment.set('reservationId', randomString(11));
pm.environment.set('crsNo', randomString(9));
pm.environment.set('signatureType', signatureTypes[Math.floor(Math.random() * signatureTypes.length)]);
pm.environment.set('arrival', formatDate(arrival));
pm.environment.set('departure', formatDate(departure));
pm.environment.set('roomId', randomNumber(8));
pm.environment.set('email', randomString(8).toLowerCase() + '@qq.com');
pm.environment.set('timestamp', Date.now());`
  // 执行一次让变量预览出来
  runPreScript()
}

function randHex(len) {
  let s = ''
  for (let i = 0; i < len; i++) s += '0123456789abcdef'[Math.floor(Math.random() * 16)]
  return s
}

function randStr(len) {
  const chars = 'abcdefghijklmnopqrstuvwxyz0123456789'
  let s = ''
  for (let i = 0; i < len; i++) s += chars[Math.floor(Math.random() * chars.length)]
  return s
}

function buildRequestBody(seq, envSnapshot = {}) {
  if (bodyType.value === 'None') return undefined

  if (bodyType.value === 'JSON-KV') {
    const obj = {}
    for (const f of dynFields.value) {
      if (!f.key) continue
      obj[f.key] = f.isDyn || hasDynVar(f.value) ? resolveVar(f.value, envSnapshot) : f.value
    }
    return JSON.stringify(obj)
  }

  if (bodyType.value === 'JSON' || bodyType.value === 'Raw') {
    return resolveVar(bodyRaw.value, envSnapshot)
  }

  return undefined
}

function buildRequestUrl(envSnapshot = {}) {
  let reqUrl = url.value.trim()
  const activeP = params.value.filter(p => p.key)
  if (activeP.length) {
    const qs = activeP.map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(resolveVar(p.value, envSnapshot))}`).join('&')
    reqUrl += (reqUrl.includes('?') ? '&' : '?') + qs
  }
  // API Key in query
  if (authType.value === 'API Key' && apiKeyName.value) {
    const sep = reqUrl.includes('?') ? '&' : '?'
    reqUrl += `${sep}${apiKeyName.value}=${apiKeyValue.value}`
  }
  return reqUrl
}

function buildHeaders() {
  const h = {}
  headers.value.filter(hh => hh.key).forEach(hh => {
    h[hh.key] = hh.value
  })
  if (authType.value === 'Bearer' && authToken.value)
    h['Authorization'] = `Bearer ${authToken.value}`
  if (authType.value === 'Basic' && authUser.value)
    h['Authorization'] = `Basic ${btoa(authUser.value + ':' + authPass.value)}`
  if (authType.value === 'API Key' && apiKeyName.value && true) // header mode default
    h[apiKeyName.value] = apiKeyValue.value
  return h
}

/* ============================================================
   压测执行
   ============================================================ */
async function startTest() {
  if (!url.value.trim() || running.value) return

  abortFlag = false
  pauseFlag = false
  running.value = true
  paused.value = false
  completedCount.value = 0
  results.value = []
  seqCounter = 0
  startTime.value = Date.now()
  scriptError.value = ''
  // 首次执行脚本，检查语法
  if (preScript.value.trim()) {
    try {
      execPreScript()
    } catch (e) {
      scriptError.value = e.message
      running.value = false
      return
    }
  }

  const total = totalRequests.value
  const conc = Math.min(concurrency.value, total)

  // 使用信号量控制并发数
  let seq = 0
  let active = 0
  let done = 0

  await new Promise(resolve => {
    function tryLaunch() {
      while (!abortFlag && !pauseFlag && active < conc && seq < total) {
        active++
        const curSeq = ++seq
        runOne(curSeq).then(() => {
          active--
          done++
          if (done >= total || abortFlag) {
            running.value = false
            resolve()
          } else if (!pauseFlag) {
            tryLaunch()
          }
        })
      }

      if (abortFlag) {
        running.value = false
        resolve()
      }
    }

    // 监听 pause 恢复
    const resumeWatcher = watch(paused, (isPaused) => {
      if (!isPaused && running.value) {
        tryLaunch()
      }
    })

    tryLaunch()
  })

  running.value = false
  paused.value = false
}

async function runOne(seq) {
  const t0 = Date.now()
  let elapsed = 0
  let success = false
  let status = 0
  let respBody = ''
  let reqBody = ''
  let error = ''

  // 每次请求独立执行脚本，获取当次变量快照（保证动态性）
  let envSnapshot = {}
  if (preScript.value.trim()) {
    try {
      envSnapshot = execPreScript()
    } catch (e) { /* 已在 startTest 检查过 */
    }
  }

  const retries = retryCount.value + 1

  // 间隔
  if (delayMode.value === 'fixed' && delayFixed.value > 0) {
    await sleep(delayFixed.value)
  } else if (delayMode.value === 'random') {
    await sleep(delayMin.value + Math.random() * (delayMax.value - delayMin.value))
  }

  for (let attempt = 0; attempt < retries; attempt++) {
    try {
      reqBody = bodyType.value !== 'None' ? buildRequestBody(seq, envSnapshot) : ''
      const reqHeaders = buildHeaders()
      if ((bodyType.value === 'JSON-KV' || bodyType.value === 'JSON') && !reqHeaders['Content-Type'])
        reqHeaders['Content-Type'] = 'application/json'

      const reqUrl = buildRequestUrl(envSnapshot)

      const resp = await fetch(reqUrl, {
        method: method.value,
        headers: reqHeaders,
        body: ['GET', 'HEAD'].includes(method.value) ? undefined : reqBody,
        signal: AbortSignal.timeout(timeout.value * 1000)
      })

      elapsed = Date.now() - t0
      status = resp.status
      success = resp.ok

      try {
        const text = await resp.text()
        // 截断长响应
        respBody = text.length > 300 ? text.slice(0, 300) + '…' : text
      } catch {
      }

      if (success || attempt === retries - 1) break

    } catch (e) {
      elapsed = Date.now() - t0
      error = e.name === 'TimeoutError' ? `请求超时 (${timeout.value}s)` : e.message
      status = 0
      success = false
    }
  }

  const urlObj = (() => {
    try {
      return new URL(url.value)
    } catch {
      return null
    }
  })()
  const urlShort = urlObj ? urlObj.pathname + (urlObj.search || '') : url.value

  results.value.push({
    id: `${seq}-${Date.now()}`,
    seq,
    thread: seq % concurrency.value + 1,
    success,
    status: status || (error ? 'ERR' : 0),
    elapsed,
    urlShort,
    reqBody: reqBody ? (reqBody.length > 200 ? reqBody.slice(0, 200) + '…' : reqBody) : '—',
    respBody,
    error,
    expanded: false
  })

  completedCount.value++
}

function sleep(ms) {
  return new Promise(r => setTimeout(r, Math.max(0, Math.round(ms))))
}

function togglePause() {
  paused.value = !paused.value
  pauseFlag = paused.value
}

function stopTest() {
  abortFlag = true
  pauseFlag = false
  running.value = false
  paused.value = false
}

function clearResults() {
  if (running.value) return
  results.value = []
  completedCount.value = 0
  startTime.value = 0
}
</script>

<style lang="scss">
@import '../shared.scss';

/* ── Pre-request Script ── */
.script-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 10px;
}

.script-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.script-info {
  font-size: 10px;
  color: var(--el-text-color-secondary, #909399);
  line-height: 1.7;
  flex: 1;

  .inline-code {
    font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
    background: var(--el-fill-color, #f0f2f5);
    padding: 0 4px;
    border-radius: 3px;
    font-size: 10px;
    color: var(--el-color-primary, #409eff);
  }
}

.script-actions {
  display: flex;
  gap: 5px;
  flex-shrink: 0;
}

.script-editor {
  width: 100%;
  min-height: 260px;
  max-height: 400px;
  resize: vertical;
  box-sizing: border-box;
  padding: 10px 12px;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 11px;
  line-height: 1.65;
  background: #1e1e1e;
  color: #d4d4d4;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 8px;
  outline: none;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }

  &::placeholder {
    color: #6a9955;
  }
}

.script-vars {
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 7px;
  overflow: hidden;
}

.vars-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 10px;
  font-size: 10px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  text-transform: uppercase;
  letter-spacing: .06em;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-bg-color, #fff);
}

.vars-count {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  font-weight: 700;
}

.vars-grid {
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.var-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 6px;
  border-radius: 5px;
  font-size: 11px;

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

.var-key {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
}

.var-val {
  color: var(--el-text-color-secondary, #909399);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.script-error {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  padding: 8px 10px;
  background: #fff1f2;
  border: 1px solid #fca5a5;
  border-radius: 7px;
  font-size: 11px;
  color: #dc2626;
  line-height: 1.5;
}

.icon-btn.running {
  opacity: .7;
  cursor: not-allowed;
}

</style>

<style lang="scss" scoped>
/* ══ 整体布局 ══ */
.stress-layout {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ── 左侧 ── */
.stress-left {
  width: 440px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto;
  overflow-x: hidden;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

/* ── 右侧 ── */
.stress-right {
  flex: 1;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 请求配置卡片撑满左侧剩余高度 */
.req-card {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* URL Bar */
.url-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  flex-shrink: 0;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.method-select {
  height: 32px;
  padding: 0 7px;
  flex-shrink: 0;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  background: var(--el-bg-color, #fff);
  cursor: pointer;
  min-width: 80px;

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

  &.method-head, &.method-options {
    color: var(--el-text-color-secondary, #909399);
  }
}

.url-input {
  flex: 1;
  height: 32px;
  padding: 0 9px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 6px;
  font-size: 12px;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  outline: none;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }
}

/* Inner Tabs */
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
  padding: 7px 11px;
  border: none;
  background: transparent;
  font-size: 11px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  transition: all .15s;
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
  width: 15px;
  height: 15px;
  border-radius: 50%;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* KV Panel */
.kv-panel {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.kv-head {
  display: grid;
  grid-template-columns: 1fr 1fr 22px;
  padding: 0 3px;
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

.kv-row {
  display: grid;
  grid-template-columns: 1fr 1fr 22px;
  gap: 5px;
}

.kv-input {
  height: 26px;
  padding: 0 7px;
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
  width: 22px;
  height: 26px;
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
  padding: 3px 9px;
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

/* Dynamic fields */
.dyn-row {
  grid-template-columns: 120px 1fr 50px 22px !important;
}

.kv-val-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.kv-val-wrap .kv-input {
  width: 100%;
  padding-right: 38px;
}

.dyn-badge {
  position: absolute;
  right: 5px;
  font-size: 9px;
  padding: 1px 4px;
  border-radius: 3px;
  background: #fef3c7;
  color: #b45309;
  font-weight: 600;
  pointer-events: none;
}

.kv-toggle {
  width: 34px;
  height: 18px;
  border-radius: 9px;
  background: var(--el-border-color, #dcdfe6);
  position: relative;
  cursor: pointer;
  transition: background .18s;
  flex-shrink: 0;

  &.on {
    background: var(--el-color-primary, #409eff);
  }
}

.kv-toggle-dot {
  position: absolute;
  top: 2px;
  left: 2px;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 1px 2px rgba(0, 0, 0, .2);
  transition: transform .18s;

  .kv-toggle.on & {
    transform: translateX(16px);
  }
}

.dyn-tip {
  font-size: 10px;
  color: #92400e;
  background: #fef9ec;
  border: 0.5px solid #fcd34d;
  border-radius: 6px;
  padding: 6px 8px;
  line-height: 1.7;

  code {
    font-family: monospace;
    background: #fef3c7;
    padding: 0 3px;
    border-radius: 3px;
  }
}

/* Body */
.body-panel {
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

.body-type-row {
  display: flex;
  gap: 14px;
}

.radio-label {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;

  input[type="radio"] {
    accent-color: var(--el-color-primary, #409eff);
  }
}

.body-textarea {
  width: 100%;
  flex: 1;
  min-height: 120px;
  resize: vertical;
  padding: 10px 12px;
  box-sizing: border-box;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 8px;
  font-size: 12px;
  line-height: 1.75;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  outline: none;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
    box-shadow: 0 0 0 2px rgba(64, 158, 255, .15);
  }

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

.body-none {
  padding: 10px;
  font-size: 12px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  text-align: center;
}

/* Auth */
.auth-panel {
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 0;
}

.mt8 {
  margin-top: 8px;
}

/* Concur settings */
.concur-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 8px;
}

.concur-item {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.concur-label {
  font-size: 10px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
}

.concur-input-wrap {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;
}

.concur-stepper {
  width: 26px;
  height: 32px;
  border: none;
  background: var(--el-fill-color-light, #f5f7fa);
  font-size: 15px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
  }
}

.concur-input {
  flex: 1;
  height: 32px;
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

.concur-unit {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  text-align: center;
}

/* Progress */
.progress-wrap {
  margin-bottom: 10px;
}

.progress-bar {
  height: 6px;
  background: var(--el-fill-color, #f0f2f5);
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 5px;
}

.progress-fill {
  height: 100%;
  border-radius: 3px;
  background: var(--el-color-primary, #409eff);
  transition: width .3s;
}

.progress-row {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
}

.run-status {
  font-weight: 600;

  &.status-running {
    color: var(--el-color-primary, #409eff);
  }

  &.status-paused {
    color: #d97706;
  }

  &.status-done {
    color: #16a34a;
  }
}

/* Control buttons */
.ctrl-btns {
  display: flex;
  gap: 7px;
}

.ctrl-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 0;
  border-radius: 7px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;

  &.start {
    background: var(--el-color-primary, #409eff);
    color: #fff;

    &:hover:not(:disabled) {
      background: #337ecc;
    }

    &:disabled {
      opacity: .4;
      cursor: not-allowed;
    }
  }

  &.pause {
    background: #fef3c7;
    color: #b45309;
    border: 1px solid #fcd34d;

    &:hover {
      background: #fde68a;
    }
  }

  &.resume {
    background: #dcfce7;
    color: #166534;
    border: 1px solid #86efac;

    &:hover {
      background: #bbf7d0;
    }
  }

  &.stop {
    background: #fee2e2;
    color: #991b1b;
    border: 1px solid #fca5a5;

    &:hover {
      background: #fecaca;
    }
  }
}

/* ── 右侧 ── */
.stats-bar {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 8px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
  background: var(--el-bg-color, #fff);
}

.stat-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 3px;
  padding: 8px 6px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter, #fafafa);
}

.stat-num {
  font-size: 17px;
  font-weight: 700;
  font-family: monospace;
  color: var(--el-text-color-primary, #303133);

  .success & {
    color: #16a34a;
  }

  .fail & {
    color: #dc2626;
  }

  .time & {
    color: #d97706;
  }
}

.stat-lbl {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

/* 分布图 */
.dist-chart {
  padding: 8px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
}

.dist-title {
  font-size: 10px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: .06em;
}

.dist-bars {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.dist-row {
  display: flex;
  align-items: center;
  gap: 7px;
}

.dist-label {
  font-size: 10px;
  color: var(--el-text-color-secondary, #909399);
  width: 56px;
  text-align: right;
  flex-shrink: 0;
}

.dist-bar-wrap {
  flex: 1;
  height: 8px;
  background: var(--el-fill-color, #f0f2f5);
  border-radius: 3px;
  overflow: hidden;
}

.dist-bar-fill {
  height: 100%;
  border-radius: 3px;
  transition: width .4s;

  &.b-green {
    background: #16a34a;
  }

  &.b-blue {
    background: var(--el-color-primary, #409eff);
  }

  &.b-amber {
    background: #d97706;
  }

  &.b-red {
    background: #dc2626;
  }
}

.dist-count {
  font-size: 10px;
  color: var(--el-text-color-secondary, #909399);
  font-family: monospace;
  width: 24px;
  flex-shrink: 0;
}

/* 结果列表 */
.result-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

.result-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-size: 12px;

  p {
    margin: 0;
  }
}

.result-item {
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  overflow: hidden;
  flex-shrink: 0;
  cursor: pointer;
  transition: border-color .12s;

  &.ok {
    border-color: #bbf7d0;

    &:hover {
      border-color: #86efac;
    }
  }

  &.fail {
    border-color: #fca5a5;

    &:hover {
      border-color: #f87171;
    }
  }
}

.ri-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;

  .ok & {
    background: #f0fdf4;
  }

  .fail & {
    background: #fff5f5;
  }
}

.ri-idx {
  width: 26px;
  height: 18px;
  border-radius: 4px;
  font-size: 9px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.ok {
    background: #dcfce7;
    color: #166534;
  }

  &.fail {
    background: #fee2e2;
    color: #991b1b;
  }
}

.ri-status {
  font-size: 12px;
  font-weight: 700;
  font-family: monospace;
  flex-shrink: 0;
  min-width: 30px;

  &.ok {
    color: #16a34a;
  }

  &.fail {
    color: #dc2626;
  }
}

.ri-thread {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-text-color-secondary, #909399);
  flex-shrink: 0;
}

.ri-url {
  flex: 1;
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ri-time {
  font-size: 11px;
  font-family: monospace;
  color: var(--el-text-color-secondary, #909399);
  flex-shrink: 0;

  &.slow {
    color: #d97706;
    font-weight: 600;
  }
}

.ri-chevron {
  flex-shrink: 0;
  color: var(--el-text-color-placeholder, #c0c4cc);
  transition: transform .18s;

  &.open {
    transform: rotate(180deg);
  }
}

.ri-detail {
  padding: 8px 10px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: var(--el-bg-color, #fff);
}

.ri-detail-row {
  display: flex;
  gap: 8px;
  align-items: flex-start;
}

.ri-detail-key {
  font-size: 10px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  min-width: 36px;
  padding-top: 2px;
  flex-shrink: 0;
}

.ri-detail-val {
  flex: 1;
  margin: 0;
  font-family: monospace;
  font-size: 11px;
  line-height: 1.6;
  color: var(--el-text-color-primary, #303133);
  white-space: pre-wrap;
  word-break: break-all;
  background: var(--el-fill-color-lighter, #fafafa);
  border-radius: 5px;
  padding: 4px 7px;
  border: 0.5px solid var(--el-border-color-lighter, #ebeef5);
  max-height: 120px;
  overflow-y: auto;

  &.err {
    color: #dc2626;
    background: #fff5f5;
  }
}

/* icon-btn */
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

  &:disabled {
    opacity: .4;
    cursor: not-allowed;
  }

  &.sm {
    padding: 3px 8px;
    font-size: 10px;
  }
}

/* ── Pre-request Script ── */
.script-panel {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 8px 10px;
}

.script-toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 8px;
}

.script-info {
  font-size: 10px;
  color: var(--el-text-color-secondary, #909399);
  line-height: 1.7;
  flex: 1;

  .inline-code {
    font-family: 'Menlo', 'Monaco', 'Consolas', monospace;
    background: var(--el-fill-color, #f0f2f5);
    padding: 0 4px;
    border-radius: 3px;
    font-size: 10px;
    color: var(--el-color-primary, #409eff);
  }
}

.script-actions {
  display: flex;
  gap: 5px;
  flex-shrink: 0;
}

.script-editor {
  width: 100%;
  min-height: 260px;
  max-height: 400px;
  resize: vertical;
  box-sizing: border-box;
  padding: 10px 12px;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 11px;
  line-height: 1.65;
  background: #1e1e1e;
  color: #d4d4d4;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 8px;
  outline: none;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }

  &::placeholder {
    color: #6a9955;
  }
}

.script-vars {
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 7px;
  overflow: hidden;
}

.vars-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 10px;
  font-size: 10px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  text-transform: uppercase;
  letter-spacing: .06em;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-bg-color, #fff);
}

.vars-count {
  font-size: 10px;
  padding: 1px 6px;
  border-radius: 8px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  font-weight: 700;
}

.vars-grid {
  padding: 6px 8px;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.var-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 3px 6px;
  border-radius: 5px;
  font-size: 11px;

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

.var-key {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
}

.var-val {
  color: var(--el-text-color-secondary, #909399);
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.script-error {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  padding: 8px 10px;
  background: #fff1f2;
  border: 1px solid #fca5a5;
  border-radius: 7px;
  font-size: 11px;
  color: #dc2626;
  line-height: 1.5;
}

.icon-btn.running {
  opacity: .7;
  cursor: not-allowed;
}

</style>