<template>
  <div class="panel-wrap">
    <div class="ws-layout">
      <div class="ws-left">
        <div class="card">
          <div class="card-header">
            <span class="card-label">WebSocket 连接</span>
            <div class="conn-status-dot" :class="wsStatus" />
            <span class="conn-status-text" :class="wsStatus">{{
                statusLabel
              }}</span>
          </div>
          <div class="card-body gap10">
            <div class="field-row">
              <label class="field-label">地址</label>
              <input
                class="field-input mono flex1"
                v-model="wsUrl"
                placeholder="ws://localhost:8080/ws"
                :disabled="connected"
              />
            </div>

            <div class="field-row">
              <label class="field-label">子协议</label>
              <input
                class="field-input flex1"
                v-model="wsProtocol"
                placeholder="可选，如 mqtt、chat"
                :disabled="connected"
              />
            </div>

            <div class="tip-sm">
              WebSocket 握手头由浏览器控制，无法自定义 Authorization 等头部
            </div>

            <div class="conn-btns">
              <button
                v-if="!connected"
                class="conn-btn connect"
                @click="connect"
                :disabled="!wsUrl || connecting"
              >
              <svg
                width="13"
                height="13"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <path d="M5 12h14M12 5l7 7-7 7" />
              </svg>
              {{ connecting ? '连接中...' : '连接' }}
              </button>
              <button v-else class="conn-btn disconnect" @click="disconnect">
                <svg
                  width="13"
                  height="13"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <line x1="18" y1="6" x2="6" y2="18" />
                  <line x1="6" y1="6" x2="18" y2="18" />
                </svg>
                断开
              </button>
              <button class="conn-btn clear" @click="messages = []">
                清空消息
              </button>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-label">发送消息</span>
          </div>
          <div class="card-body gap10">
            <div class="field-row">
              <label class="field-label">格式</label>
              <div class="seg-ctrl">
                <button
                  :class="{ active: sendFormat === 'text' }"
                  @click="sendFormat = 'text'"
                >
                  Text
                </button>
                <button
                  :class="{ active: sendFormat === 'json' }"
                  @click="sendFormat = 'json'"
                >
                  JSON
                </button>
                <button
                  :class="{ active: sendFormat === 'binary' }"
                  @click="sendFormat = 'binary'"
                >
                  Binary
                </button>
              </div>
            </div>
            <textarea
              class="send-textarea mono"
              v-model="sendMsg"
              :placeholder="sendPlaceholder"
              spellcheck="false"
              @keydown.ctrl.enter="send"
            />
            <div class="send-row">
              <span class="send-hint">Ctrl+Enter 发送</span>
              <div class="send-actions">
                <label class="radio-label">
                  <input type="checkbox" v-model="autoSend" />
                  <span>自动发送</span>
                </label>
                <input
                  v-if="autoSend"
                  class="field-input"
                  style="width: 70px"
                  type="number"
                  v-model.number="autoInterval"
                  min="500"
                />
                <span
                  v-if="autoSend"
                  style="font-size: 11px; color: var(--el-text-color-secondary)"
                >ms</span
                >
                <button
                  class="send-btn-ws"
                  @click="send"
                  :disabled="!connected"
                >
                  <svg
                    width="12"
                    height="12"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2.5"
                  >
                    <line x1="22" y1="2" x2="11" y2="13" />
                    <polygon points="22 2 15 22 11 13 2 9 22 2" />
                  </svg>
                  发送
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-label">消息模板</span>
          </div>
          <div class="tpl-grid">
            <button
              class="tpl-chip"
              v-for="t in msgTemplates"
              :key="t.label"
              @click="sendMsg = t.msg; sendFormat = t.format"
            >
              {{ t.label }}
            </button>
          </div>
        </div>
      </div>

      <div class="ws-right">
        <div class="card msg-card">
          <div class="card-header">
            <span class="card-label">消息记录</span>
            <span class="msg-count">{{ messages.length }} 条</span>
            <div class="card-actions">
              <label class="radio-label" style="font-size: 11px">
                <input type="checkbox" v-model="autoScroll" />自动滚动
              </label>
              <button class="icon-btn sm" @click="exportMessages">
                <svg
                  width="11"
                  height="11"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                导出
              </button>
              <button class="icon-btn sm danger" @click="messages = []">
                清空
              </button>
            </div>
          </div>

          <div class="msg-list" ref="msgListRef">
            <div v-if="messages.length === 0" class="msg-empty">
              <svg
                width="40"
                height="40"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="0.8"
                opacity="0.2"
              >
                <path
                  d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"
                />
              </svg>
              <p>{{ connected ? '等待消息...' : '连接后开始接收消息' }}</p>
            </div>
            <div
              v-for="(msg, i) in messages"
              :key="i"
              class="msg-item"
              :class="msg.direction"
            >
              <div class="msg-meta">
                <span class="msg-dir-icon">{{
                    msg.direction === 'sent' ? '↑' : '↓'
                  }}</span>
                <span class="msg-time">{{ msg.time }}</span>
                <span class="msg-type-badge" :class="msg.direction">{{
                    msg.direction === 'sent' ? '发送' : '接收'
                  }}</span>
                <span class="msg-size">{{ msg.size }}</span>
                <button class="copy-mini" @click="copyText(msg.content)">
                  <svg
                    width="10"
                    height="10"
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
                </button>
              </div>
              <pre class="msg-content mono">{{
                  formatMsgContent(msg.content)
                }}</pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'

const wsUrl = ref('wss://echo.websocket.org')
const wsProtocol = ref('')
const sendMsg = ref('')
const sendFormat = ref('text')
const sendPlaceholder = computed(() =>
  sendFormat.value === 'json'
    ? '{"type":"ping","data":{}}'
    : '输入要发送的消息...',
)
const autoSend = ref(false)
const autoInterval = ref(1000)
const autoScroll = ref(true)
const messages = ref([])
const msgListRef = ref(null)

let ws = null
let autoTimer = null

/* ── 状态 ── */
const connected = ref(false)
const connecting = ref(false)
const wsStatus = computed(() => {
  if (connecting.value) return 'connecting'
  if (connected.value) return 'connected'
  return 'disconnected'
})
const statusLabel = computed(() => {
  if (connecting.value) return '连接中'
  if (connected.value) return '已连接'
  return '未连接'
})

/* ── 消息模板 ── */
const msgTemplates = [
  { label: 'Ping', format: 'json', msg: '{"type":"ping"}' },
  {
    label: 'Heartbeat',
    format: 'json',
    msg: '{"type":"heartbeat","timestamp":0}',
  },
  {
    label: 'Subscribe',
    format: 'json',
    msg: '{"action":"subscribe","channel":"test"}',
  },
  { label: 'Hello Text', format: 'text', msg: 'Hello, WebSocket!' },
]

/* ── 连接 ── */
function connect() {
  if (!wsUrl.value || connected.value) return
  connecting.value = true
  try {
    const protocols = wsProtocol.value ? [wsProtocol.value] : undefined
    ws = new WebSocket(wsUrl.value, protocols)

    ws.onopen = () => {
      connected.value = true
      connecting.value = false
      addSystemMsg(`✓ 已连接到 ${wsUrl.value}`)
    }

    ws.onmessage = (e) => {
      addMessage(
        'received',
        typeof e.data === 'string' ? e.data : '[Binary Data]',
      )
    }

    ws.onclose = (e) => {
      connected.value = false
      connecting.value = false
      addSystemMsg(`✗ 连接关闭 (code: ${e.code})`)
      stopAutoSend()
    }

    ws.onerror = () => {
      connecting.value = false
      addSystemMsg('✗ 连接错误')
    }
  } catch (e) {
    connecting.value = false
    addSystemMsg(`✗ ${e.message}`)
  }
}

function disconnect() {
  ws?.close()
  stopAutoSend()
}

function send() {
  if (!connected.value || !sendMsg.value) return
  let content = sendMsg.value
  if (sendFormat.value === 'json') {
    try {
      content = JSON.stringify(JSON.parse(content))
    } catch {}
  }
  ws.send(content)
  addMessage('sent', content)
}

/* ── 自动发送 ── */
watch(autoSend, (val) => {
  if (val) startAutoSend()
  else stopAutoSend()
})

function startAutoSend() {
  autoTimer = setInterval(() => {
    if (connected.value) send()
  }, autoInterval.value)
}
function stopAutoSend() {
  clearInterval(autoTimer)
  autoTimer = null
}

/* ── 消息处理与触底控制 ── */
function scrollToBottom() {
  if (!autoScroll.value || !msgListRef.value) return
  nextTick(() => {
    // 使用 scrollTop 直接赋值比 'smooth' 平滑滚动在超高频并发下性能更强，不易掉帧卡顿
    msgListRef.value.scrollTop = msgListRef.value.scrollHeight
  })
}

function addMessage(direction, content) {
  const now = new Date()
  const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}.${String(now.getMilliseconds()).padStart(3, '0')}`
  const bytes = new TextEncoder().encode(content).length
  const size = bytes < 1024 ? `${bytes}B` : `${(bytes / 1024).toFixed(1)}KB`

  messages.value.push({ direction, content, time, size })

  // 限制最大渲染数，防止海量堆积撑爆浏览器内存
  if (messages.value.length > 400) {
    messages.value.shift()
  }

  scrollToBottom()
}

function addSystemMsg(text) {
  addMessage('system', text)
}

function formatMsgContent(content) {
  try {
    const obj = JSON.parse(content)
    return JSON.stringify(obj, null, 2)
  } catch {
    return content
  }
}

async function copyText(text) {
  await navigator.clipboard.writeText(text).catch(() => {})
}

function exportMessages() {
  const lines = messages.value.map(
    (m) => `[${m.time}] [${m.direction}] ${m.content}`,
  )
  const blob = new Blob([lines.join('\n')], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `ws-messages-${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(url)
}

onBeforeUnmount(() => {
  ws?.close()
  stopAutoSend()
})
</script>

<style lang="scss">
@import '../shared.scss';
</style>

<style lang="scss" scoped>
.ws-layout {
  display: flex;
  gap: 12px;
  flex: 1;
  height: 100%; /* 确保布局占满整个容器高度 */
  min-height: 0;
  overflow: hidden;
}
.ws-left {
  width: 480px; /* 同步加宽，给长链接提供足够的视觉纵深 */
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
.ws-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  height: 100%; /* 强行铺满高度 */
  overflow: hidden; /* 切断外部任何形式的溢出 */
}

/* ── Connection Status ── */
.conn-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  &.connected {
    background: #16a34a;
    box-shadow: 0 0 0 2px #86efac;
  }
  &.connecting {
    background: #d97706;
    box-shadow: 0 0 0 2px #fcd34d;
    animation: pulse 0.8s infinite;
  }
  &.disconnected {
    background: var(--el-text-color-placeholder, #c0c4cc);
  }
}
@keyframes pulse {
  0%,
  100% {
    opacity: 1;
  }
  50% {
    opacity: 0.4;
  }
}

.conn-status-text {
  font-size: 11px;
  font-weight: 600;
  &.connected {
    color: #16a34a;
  }
  &.connecting {
    color: #d97706;
  }
  &.disconnected {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

.conn-btns {
  display: flex;
  gap: 8px;
}
.conn-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 7px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.15s;
  &.connect {
    background: var(--el-color-primary, #409eff);
    color: #fff;
    &:hover:not(:disabled) {
      background: #337ecc;
    }
    &:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
  }
  &.disconnect {
    background: #dc2626;
    color: #fff;
    &:hover {
      background: #b91c1c;
    }
  }
  &.clear {
    background: var(--el-fill-color, #f0f2f5);
    border: 1px solid var(--el-border-color, #dcdfe6);
    color: var(--el-text-color-regular, #606266);
    &:hover {
      border-color: var(--el-color-primary, #409eff);
      color: var(--el-color-primary, #409eff);
    }
  }
}

.tip-sm {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  padding: 4px 8px;
  background: var(--el-fill-color-lighter, #fafafa);
  border-radius: 5px;
  line-height: 1.5;
}

/* ── Send ── */
.send-textarea {
  width: 100%;
  min-height: 90px;
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
.send-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.send-hint {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
.send-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
.send-btn-ws {
  display: flex;
  align-items: center;
  gap: 5px;
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  background: var(--el-color-primary, #409eff);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  cursor: pointer;
  &:hover:not(:disabled) {
    background: #337ecc;
  }
  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

/* ── Templates ── */
.tpl-grid {
  padding: 10px 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.tpl-chip {
  padding: 4px 12px;
  border-radius: 6px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: var(--el-fill-color-lighter, #fafafa);
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

/* ── Messages ── */
.msg-card {
  flex: 1;
  min-height: 0; /* flex容器在空间不足时向下缩小的必备法则 */
  display: flex;
  flex-direction: column;
  height: 100%;
}
.msg-count {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
}
.msg-list {
  flex: 1;
  overflow-y: auto; /* 必须激活独立的局部纵向滚动条 */
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  background: var(--el-bg-color, #fff);
  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.08);
    border-radius: 3px;
  }
}
.msg-empty {
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
.msg-item {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0; /* 防止子项自己被压缩变形 */
  &.sent {
    border-color: #bfdbfe;
  }
  &.received {
    border-color: #bbf7d0;
  }
  &.system {
    border-color: var(--el-border-color-lighter, #ebeef5);
    opacity: 0.8;
  }
}
.msg-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 10px;
  background: var(--el-fill-color-lighter, #fafafa);
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  .sent & {
    background: #eff6ff;
  }
  .received & {
    background: #f0fdf4;
  }
}
.msg-dir-icon {
  font-size: 12px;
  font-weight: 700;
  .sent & {
    color: #2563eb;
  }
  .received & {
    color: #16a34a;
  }
}
.msg-time {
  font-size: 10px;
  font-family: monospace;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
.msg-type-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 3px;
  &.sent {
    background: #dbeafe;
    color: #2563eb;
  }
  &.received {
    background: #dcfce7;
    color: #16a34a;
  }
  &.system {
    background: var(--el-fill-color, #f0f2f5);
    color: var(--el-text-color-secondary, #909399);
  }
}
.msg-size {
  font-size: 10px;
  font-family: monospace;
  color: var(--el-text-color-placeholder, #c0c4cc);
}
.msg-content {
  margin: 0;
  padding: 8px 10px;
  font-size: 11px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--el-text-color-primary, #303133);
  max-height: 260px; /* 适当调高内部独立滚动的阈值，给海量JSON提供空间 */
  overflow-y: auto;
}
.copy-mini {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: transparent;
  cursor: pointer;
  color: var(--el-text-color-placeholder, #c0c4cc);
  &:hover {
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
  }
}
</style>