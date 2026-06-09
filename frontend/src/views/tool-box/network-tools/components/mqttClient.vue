<template>
  <div class="panel-wrap">
    <div class="mqtt-layout">
      <!-- ══ 左侧：连接 + 订阅 + 发布 ══ -->
      <div class="mqtt-left">
        <!-- 连接配置 -->
        <div class="card">
          <div class="card-header">
            <span class="card-label">MQTT 连接</span>
            <div class="conn-status-dot" :class="mqttStatus" />
            <span class="conn-status-text" :class="mqttStatus">{{
              statusLabel
            }}</span>
          </div>
          <div class="card-body gap10">
            <div class="field-row">
              <label class="field-label">协议</label>
              <div class="seg-ctrl">
                <button
                  v-for="p in protocols"
                  :key="p"
                  :class="{ active: protocol === p }"
                  @click="protocol = p"
                  :disabled="connected"
                >
                  {{ p }}
                </button>
              </div>
            </div>
            <div class="field-row">
              <label class="field-label">地址</label>
              <input
                class="field-input mono flex1"
                v-model="brokerHost"
                placeholder="broker.emqx.io"
                :disabled="connected"
              />
            </div>
            <div class="field-row">
              <label class="field-label">端口</label>
              <input
                class="field-input mono"
                style="width: 80px"
                v-model.number="brokerPort"
                :disabled="connected"
              />
              <label class="field-label" style="margin-left: 12px">Path</label>
              <input
                class="field-input mono flex1"
                v-model="brokerPath"
                placeholder="/mqtt"
                :disabled="connected"
              />
            </div>
            <div class="field-row">
              <label class="field-label">Client ID</label>
              <input
                class="field-input mono flex1"
                v-model="clientId"
                :disabled="connected"
              />
              <button
                class="icon-btn sm"
                @click="genClientId"
                :disabled="connected"
                title="随机生成"
              >
                <svg
                  width="11"
                  height="11"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <polyline points="23 4 23 10 17 10" />
                  <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
                </svg>
              </button>
            </div>
            <div class="field-row">
              <label class="field-label">用户名</label>
              <input
                class="field-input flex1"
                v-model="mqttUser"
                placeholder="（可选）"
                :disabled="connected"
              />
            </div>
            <div class="field-row">
              <label class="field-label">密码</label>
              <input
                class="field-input flex1"
                v-model="mqttPass"
                type="password"
                placeholder="（可选）"
                :disabled="connected"
              />
            </div>

            <div class="collapse-trigger" @click="showAdvanced = !showAdvanced">
              <svg
                width="11"
                height="11"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                :style="{ transform: showAdvanced ? 'rotate(90deg)' : '' }"
              >
                <polyline points="9 18 15 12 9 6" />
              </svg>
              高级选项
            </div>

            <template v-if="showAdvanced">
              <div class="field-row">
                <label class="field-label">Keep Alive</label>
                <input
                  class="field-input"
                  style="width: 70px"
                  type="number"
                  v-model.number="keepAlive"
                  :disabled="connected"
                />
                <span
                  style="
                    font-size: 11px;
                    color: var(--el-text-color-secondary);
                    margin-left: 4px;
                  "
                  >秒</span
                >
              </div>
              <div class="field-row">
                <label class="field-label">Clean Session</label>
                <div class="seg-ctrl">
                  <button
                    :class="{ active: cleanSession === true }"
                    @click="cleanSession = true"
                    :disabled="connected"
                  >
                    是
                  </button>
                  <button
                    :class="{ active: cleanSession === false }"
                    @click="cleanSession = false"
                    :disabled="connected"
                  >
                    否
                  </button>
                </div>
              </div>
              <div class="field-row">
                <label class="field-label">MQTT 版本</label>
                <div class="seg-ctrl">
                  <button
                    v-for="v in mqttVersions"
                    :key="v"
                    :class="{ active: mqttVersion === v }"
                    @click="mqttVersion = v"
                    :disabled="connected"
                  >
                    {{ v }}
                  </button>
                </div>
              </div>
              <div class="field-row">
                <label class="field-label">遗嘱 Topic</label>
                <input
                  class="field-input mono flex1"
                  v-model="willTopic"
                  placeholder="（可选）"
                  :disabled="connected"
                />
              </div>
              <div class="field-row">
                <label class="field-label">遗嘱消息</label>
                <input
                  class="field-input flex1"
                  v-model="willMsg"
                  placeholder="offline"
                  :disabled="connected"
                />
              </div>
            </template>

            <div class="conn-btns">
              <button
                v-if="!connected"
                class="conn-btn connect"
                @click="connect"
                :disabled="!brokerHost || connecting"
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
            </div>
          </div>
        </div>

        <!-- 订阅管理 -->
        <div class="card">
          <div class="card-header">
            <span class="card-label">订阅管理</span>
            <span class="count-badge">{{ subscriptions.length }}</span>
          </div>
          <div class="card-body gap10">
            <div class="field-row">
              <input
                class="field-input mono flex1"
                v-model="subTopic"
                placeholder="test/# 或 sensors/+/temp"
                :disabled="!connected"
                @keydown.enter="subscribe"
              />
              <div class="seg-ctrl" style="margin-left: 6px">
                <button
                  v-for="q in [0, 1, 2]"
                  :key="q"
                  :class="{ active: subQos === q }"
                  @click="subQos = q"
                  :disabled="!connected"
                >
                  {{ q }}
                </button>
              </div>
              <button
                class="icon-btn sm accent"
                @click="subscribe"
                :disabled="!connected || !subTopic"
                style="margin-left: 6px"
              >
                订阅
              </button>
            </div>
            <div class="sub-list" v-if="subscriptions.length">
              <div class="sub-item" v-for="(s, i) in subscriptions" :key="i">
                <span class="sub-topic mono">{{ s.topic }}</span>
                <span class="qos-badge">QoS {{ s.qos }}</span>
                <button class="kv-del" @click="unsubscribe(i)">✕</button>
              </div>
            </div>
            <div v-else class="empty-hint-sm">尚未订阅任何 Topic</div>
          </div>
        </div>

        <!-- 发布消息 -->
        <div class="card">
          <div class="card-header">
            <span class="card-label">发布消息</span>
          </div>
          <div class="card-body gap10">
            <div class="field-row">
              <label class="field-label">Topic</label>
              <input
                class="field-input mono flex1"
                v-model="pubTopic"
                placeholder="test/message"
                :disabled="!connected"
              />
            </div>
            <div class="field-row">
              <label class="field-label">QoS</label>
              <div class="seg-ctrl">
                <button
                  v-for="q in [0, 1, 2]"
                  :key="q"
                  :class="{ active: pubQos === q }"
                  @click="pubQos = q"
                  :disabled="!connected"
                >
                  {{ q }}
                </button>
              </div>
              <label class="radio-label" style="margin-left: 12px">
                <input
                  type="checkbox"
                  v-model="retained"
                  :disabled="!connected"
                />Retain
              </label>
            </div>
            <textarea
              class="send-textarea mono"
              v-model="pubPayload"
              placeholder='{"temperature":25.5,"humidity":60}'
              :disabled="!connected"
              spellcheck="false"
              @keydown.ctrl.enter="publish"
            />
            <div class="send-row">
              <span class="send-hint">Ctrl+Enter 发布</span>
              <button
                class="send-btn-ws"
                @click="publish"
                :disabled="!connected || !pubTopic"
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
                发布
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- ══ 右侧：消息记录 ══ -->
      <div class="mqtt-right">
        <div class="card msg-card">
          <div class="card-header">
            <span class="card-label">消息记录</span>
            <span class="msg-count">{{ messages.length }} 条</span>
            <div class="card-actions">
              <input
                class="field-input"
                style="width: 140px; height: 26px; font-size: 11px"
                v-model="filterTopic"
                placeholder="过滤 Topic..."
              />
              <label class="radio-label" style="font-size: 11px">
                <input type="checkbox" v-model="autoScroll" />自动滚动
              </label>
              <button class="icon-btn sm" @click="exportMessages">导出</button>
              <button class="icon-btn sm danger" @click="messages = []">
                清空
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="filteredMessages.length === 0" class="msg-empty">
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
                d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07A19.5 19.5 0 0 1 4.69 13a19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 3.6 2h3a2 2 0 0 1 2 1.72c.127.96.361 1.903.7 2.81a2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0 1 22 16.92z"
              />
            </svg>
            <p>{{ connected ? '等待消息...' : '连接后开始接收消息' }}</p>
          </div>

          <!-- 消息列表 — 关键：这里是唯一的滚动容器 -->
          <div v-else class="msg-list" ref="msgListRef">
            <div
              v-for="(msg, i) in filteredMessages"
              :key="i"
              class="msg-item"
              :class="msg.direction"
            >
              <div class="msg-meta">
                <span class="msg-dir-icon">{{
                  msg.direction === 'published' ? '↑' : '↓'
                }}</span>
                <span class="msg-topic-tag mono">{{ msg.topic }}</span>
                <span class="qos-badge" style="margin-left: 4px"
                  >QoS {{ msg.qos }}</span
                >
                <span v-if="msg.retained" class="retain-badge">Retain</span>
                <span class="msg-time">{{ msg.time }}</span>
                <button class="copy-mini" @click="copyText(msg.payload)">
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
              <!-- payload 折叠：超过3行时折叠 -->
              <div class="msg-body">
                <pre
                  class="msg-content mono"
                  :class="{ collapsed: !msg.expanded && isLong(msg.payload) }"
                  >{{ formatPayload(msg.payload) }}</pre
                >
                <button
                  v-if="isLong(msg.payload)"
                  class="expand-btn"
                  @click="msg.expanded = !msg.expanded"
                >
                  {{ msg.expanded ? '收起 ▲' : '展开 ▼' }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick, onBeforeUnmount } from 'vue'

/* ── 连接配置 ── */
const protocols = ['ws', 'wss', 'mqtt', 'mqtts']
const protocol = ref('ws')
const brokerHost = ref('broker.emqx.io')
const brokerPort = ref(8083)
const brokerPath = ref('/mqtt')
const clientId = ref('mqtt-tool-' + Math.random().toString(36).slice(2, 8))
const mqttUser = ref('')
const mqttPass = ref('')
const keepAlive = ref(60)
const cleanSession = ref(true)
const mqttVersions = ['3.1.1', '5.0']
const mqttVersion = ref('3.1.1')
const willTopic = ref('')
const willMsg = ref('')
const showAdvanced = ref(false)

/* ── 连接状态 ── */
const connected = ref(false)
const connecting = ref(false)
const mqttStatus = computed(() =>
  connecting.value
    ? 'connecting'
    : connected.value
      ? 'connected'
      : 'disconnected',
)
const statusLabel = computed(() =>
  connecting.value ? '连接中' : connected.value ? '已连接' : '未连接',
)

/* ── 订阅 ── */
const subTopic = ref('test/#')
const subQos = ref(0)
const subscriptions = ref([])

/* ── 发布 ── */
const pubTopic = ref('test/message')
const pubQos = ref(0)
const pubPayload = ref('{"temperature":25.5,"humidity":60}')
const retained = ref(false)

/* ── 消息 ── */
const messages = ref([])
const filterTopic = ref('')
const autoScroll = ref(true)
const msgListRef = ref(null)

// 超过 3 行的 payload 默认折叠
const LONG_THRESHOLD = 3
function isLong(payload) {
  try {
    const formatted = JSON.stringify(JSON.parse(payload), null, 2)
    return formatted.split('\n').length > LONG_THRESHOLD
  } catch {
    return payload.split('\n').length > LONG_THRESHOLD
  }
}

const filteredMessages = computed(() => {
  if (!filterTopic.value) return messages.value
  return messages.value.filter((m) => m.topic?.includes(filterTopic.value))
})

/* ── MQTT 客户端 ── */
let client = null

function genClientId() {
  clientId.value = 'mqtt-tool-' + Math.random().toString(36).slice(2, 10)
}

async function loadMqttLib() {
  if (window.mqtt) return window.mqtt
  return new Promise((resolve, reject) => {
    const s = document.createElement('script')
    s.src = 'https://cdnjs.cloudflare.com/ajax/libs/mqtt/5.10.1/mqtt.min.js'
    s.onload = () => resolve(window.mqtt)
    s.onerror = reject
    document.head.appendChild(s)
  })
}

async function connect() {
  if (connected.value || connecting.value) return
  connecting.value = true
  addSystemMsg(
    `正在连接 ${protocol.value}://${brokerHost.value}:${brokerPort.value}${brokerPath.value}...`,
  )
  try {
    const mqtt = await loadMqttLib()
    const wsProto =
      protocol.value === 'mqtt'
        ? 'ws'
        : protocol.value === 'mqtts'
          ? 'wss'
          : protocol.value
    const url = `${wsProto}://${brokerHost.value}:${brokerPort.value}${brokerPath.value}`
    const opts = {
      clientId: clientId.value,
      keepalive: keepAlive.value,
      clean: cleanSession.value,
      reconnectPeriod: 0,
      connectTimeout: 10000,
      protocolVersion: mqttVersion.value === '5.0' ? 5 : 4,
    }
    if (mqttUser.value) opts.username = mqttUser.value
    if (mqttPass.value) opts.password = mqttPass.value
    if (willTopic.value)
      opts.will = {
        topic: willTopic.value,
        payload: willMsg.value,
        qos: 0,
        retain: false,
      }

    client = mqtt.connect(url, opts)
    client.on('connect', () => {
      connected.value = true
      connecting.value = false
      addSystemMsg(`✓ 已连接到 ${brokerHost.value}:${brokerPort.value}`)
    })
    client.on('message', (topic, payload, packet) => {
      addMessage(
        'received',
        topic,
        payload.toString(),
        packet.qos,
        packet.retain,
      )
    })
    client.on('error', (err) => {
      connecting.value = false
      addSystemMsg(`✗ 错误: ${err.message}`)
    })
    client.on('close', () => {
      connected.value = false
      connecting.value = false
      addSystemMsg('✗ 连接已关闭')
    })
    client.on('offline', () => {
      addSystemMsg('⚠ 离线')
    })
  } catch (e) {
    connecting.value = false
    addSystemMsg(`✗ 加载 MQTT 库失败: ${e.message}`)
  }
}

function disconnect() {
  client?.end(true)
  client = null
  connected.value = false
  connecting.value = false
}

function subscribe() {
  if (!connected.value || !subTopic.value || !client) return
  client.subscribe(subTopic.value, { qos: subQos.value }, (err) => {
    if (err) {
      addSystemMsg(`✗ 订阅失败: ${err.message}`)
      return
    }
    subscriptions.value.push({ topic: subTopic.value, qos: subQos.value })
    addSystemMsg(`✓ 已订阅 ${subTopic.value} (QoS ${subQos.value})`)
    subTopic.value = ''
  })
}

function unsubscribe(i) {
  const { topic } = subscriptions.value[i]
  client?.unsubscribe(topic, () => {
    subscriptions.value.splice(i, 1)
    addSystemMsg(`已取消订阅 ${topic}`)
  })
}

function publish() {
  if (!connected.value || !pubTopic.value || !client) return
  client.publish(
    pubTopic.value,
    pubPayload.value,
    { qos: pubQos.value, retain: retained.value },
    (err) => {
      if (err) {
        addSystemMsg(`✗ 发布失败: ${err.message}`)
        return
      }
      addMessage(
        'published',
        pubTopic.value,
        pubPayload.value,
        pubQos.value,
        retained.value,
      )
    },
  )
}

/* ── 消息记录 ── */
function addMessage(direction, topic, payload, qos = 0, isRetained = false) {
  const now = new Date()
  const time = `${String(now.getHours()).padStart(2, '0')}:${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`
  messages.value.push({
    direction,
    topic,
    payload,
    qos,
    retained: isRetained,
    time,
    expanded: false,
  })
  if (messages.value.length > 500) messages.value.shift()
  if (autoScroll.value)
    nextTick(() => {
      if (msgListRef.value)
        msgListRef.value.scrollTop = msgListRef.value.scrollHeight
    })
}

function addSystemMsg(text) {
  addMessage('system', 'system', text, 0, false)
}

function formatPayload(payload) {
  try {
    return JSON.stringify(JSON.parse(payload), null, 2)
  } catch {
    return payload
  }
}

async function copyText(text) {
  await navigator.clipboard.writeText(text).catch(() => {})
}

function exportMessages() {
  const lines = filteredMessages.value.map(
    (m) =>
      `[${m.time}] [${m.direction}] ${m.topic} (QoS${m.qos}${m.retained ? ' Retain' : ''}): ${m.payload}`,
  )
  const blob = new Blob([lines.join('\n')], { type: 'text/plain' })
  const a = document.createElement('a')
  a.href = URL.createObjectURL(blob)
  a.download = `mqtt-messages-${Date.now()}.txt`
  a.click()
  URL.revokeObjectURL(a.href)
}

onBeforeUnmount(() => {
  client?.end(true)
})
</script>

<style lang="scss">
@import '../shared.scss';
</style>

<style lang="scss" scoped>
/* ══ 整体布局 ══ */
.mqtt-layout {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;
  overflow: hidden; /* 关键：让子列的 overflow 生效 */
}

/* ── 左侧：固定宽度，内容超出时自身滚动 ── */
.mqtt-left {
  width: 360px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow-y: auto; /* 左侧配置区自己滚 */
  overflow-x: hidden;
  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.08);
    border-radius: 2px;
  }
}

/* ── 右侧：占满剩余高度，不超出 ── */
.mqtt-right {
  flex: 1;
  min-width: 0;
  min-height: 0; /* 关键：配合父级 flex 限高 */
  display: flex;
  flex-direction: column;
  overflow: hidden; /* 关键：裁剪超出内容 */
}

/* ── 消息卡片：撑满右侧 ── */
.msg-card {
  flex: 1;
  min-height: 0; /* 关键：flex 子项必须设 min-height:0 才能被父级裁剪 */
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ── 空状态 ── */
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

/* ── 消息列表：唯一的滚动容器，占满卡片剩余空间 ── */
.msg-list {
  flex: 1;
  min-height: 0; /* 关键 */
  overflow-y: auto; /* 只在这里滚动 */
  overflow-x: hidden;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  &::-webkit-scrollbar {
    width: 5px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 3px;
  }
}

/* ── 消息条目：高度由内容决定，但通过折叠控制 ── */
.msg-item {
  border-radius: 8px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  overflow: hidden;
  flex-shrink: 0; /* 每条消息不被压缩 */
  &.published {
    border-color: #bfdbfe;
  }
  &.received {
    border-color: #bbf7d0;
  }
  &.system {
    border-color: var(--el-border-color-lighter, #ebeef5);
    opacity: 0.65;
  }
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  min-height: 28px;
  .published & {
    background: #eff6ff;
  }
  .received & {
    background: #f0fdf4;
  }
  .system & {
    background: var(--el-fill-color-lighter, #fafafa);
  }
}

.msg-dir-icon {
  font-size: 11px;
  font-weight: 700;
  flex-shrink: 0;
  .published & {
    color: #2563eb;
  }
  .received & {
    color: #16a34a;
  }
  .system & {
    color: var(--el-text-color-secondary, #909399);
  }
}
.msg-topic-tag {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-color-primary, #409eff);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}
.msg-time {
  font-size: 10px;
  font-family: monospace;
  color: var(--el-text-color-placeholder, #c0c4cc);
  flex-shrink: 0;
}

/* ── 消息体：折叠控制，防止单条撑破布局 ── */
.msg-body {
  position: relative;
}

.msg-content {
  margin: 0;
  padding: 6px 10px;
  font-size: 11px;
  line-height: 1.55;
  white-space: pre-wrap;
  word-break: break-all;
  color: var(--el-text-color-primary, #303133);
  background: var(--el-bg-color, #fff);
  overflow: hidden;

  /* 默认折叠：最多显示 3 行 */
  &.collapsed {
    display: -webkit-box;
    -webkit-line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.expand-btn {
  display: block;
  width: 100%;
  padding: 3px 10px;
  border: none;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-fill-color-lighter, #fafafa);
  font-size: 10px;
  color: var(--el-color-primary, #409eff);
  cursor: pointer;
  text-align: center;
  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

/* ── 状态指示 ── */
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
}

.collapse-trigger {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  user-select: none;
  svg {
    transition: transform 0.2s;
  }
  &:hover {
    color: var(--el-color-primary, #409eff);
  }
}

.sub-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.sub-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 5px 8px;
  border-radius: 6px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
}
.sub-topic {
  flex: 1;
  font-size: 11px;
  color: var(--el-text-color-primary, #303133);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.qos-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  flex-shrink: 0;
}
.retain-badge {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  background: #fef3c7;
  color: #d97706;
  flex-shrink: 0;
}

.send-textarea {
  width: 100%;
  min-height: 80px;
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
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
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

.msg-count {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
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
  flex-shrink: 0;
  &:hover {
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
  }
}
.kv-del {
  width: 24px;
  height: 24px;
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
</style>
