<template>
  <div class="panel-wrap">
    <div class="card">
      <div class="card-header">
        <span class="card-label">URL 解析</span>
        <button class="icon-btn sm"
                @click="urlInput = 'https://api.example.com:8080/v1/users?page=1&size=20&keyword=hello+world&filter=active#section-2'; parseUrl()">
          示例
        </button>
      </div>
      <div class="card-body">
        <div class="field-row">
          <label class="field-label">URL</label>
          <input class="field-input mono flex1" v-model="urlInput" placeholder="https://example.com/path?key=value#hash"
                 @input="parseUrl" />
        </div>
      </div>
    </div>

    <div v-if="urlResult && !urlError" class="url-result-wrap mt12">
      <!-- 结构分解 -->
      <div class="card url-parts-card">
        <div class="card-header"><span class="card-label">结构分解</span></div>
        <div class="url-parts">
          <div class="up-item" v-for="p in urlParts" :key="p.key">
            <span class="up-key">{{ p.label }}</span>
            <div class="up-val-wrap">
              <span class="up-tag" :style="{ background: p.color + '22', color: p.color }">{{ p.key }}</span>
              <span class="up-val mono">{{ p.value || '—' }}</span>
              <button v-if="p.value" class="copy-mini" @click="copyText(p.value)">
                <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="9" y="9" width="13" height="13" rx="2" />
                  <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Query 参数 -->
      <div class="card mt12" v-if="urlParams.length">
        <div class="card-header">
          <span class="card-label">Query 参数</span>
          <span class="count-badge">{{ urlParams.length }} 个</span>
        </div>
        <div class="param-table">
          <div class="pt-head">
            <span>参数名</span><span>原始值</span><span>解码值</span>
          </div>
          <div class="pt-row" v-for="p in urlParams" :key="p.key">
            <span class="mono pt-key">{{ p.key }}</span>
            <span class="mono pt-raw">{{ p.raw }}</span>
            <span class="mono pt-decoded">{{ p.decoded }}</span>
          </div>
        </div>
      </div>
    </div>
    <div v-if="urlError" class="result-err mt12">{{ urlError }}</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const urlInput = ref('')
const urlResult = ref(null)
const urlError = ref('')
const urlParts = ref([])
const urlParams = ref([])

const URL_COLORS = {
  protocol: '#409eff', hostname: '#67c23a', port: '#e6a23c',
  pathname: '#9b59b6', hash: '#f56c6c', username: '#1abc9c'
}

function parseUrl() {
  urlError.value = ''
  urlResult.value = null
  urlParts.value = []
  urlParams.value = []
  const raw = urlInput.value.trim()
  if (!raw) return

  let u
  try {
    u = new URL(raw)
  } catch {
    try {
      u = new URL('https://' + raw)
    } catch {
      urlError.value = '无效的 URL 格式'
      return
    }
  }

  urlResult.value = u
  urlParts.value = [
    { key: 'protocol', label: '协议', value: u.protocol.replace(':', ''), color: URL_COLORS.protocol },
    { key: 'username', label: '用户名', value: u.username, color: URL_COLORS.username },
    { key: 'password', label: '密码', value: u.password, color: URL_COLORS.username },
    { key: 'hostname', label: '主机名', value: u.hostname, color: URL_COLORS.hostname },
    { key: 'port', label: '端口', value: u.port, color: URL_COLORS.port },
    { key: 'pathname', label: '路径', value: u.pathname, color: URL_COLORS.pathname },
    { key: 'search', label: '查询串', value: u.search, color: URL_COLORS.hash },
    { key: 'hash', label: 'Hash', value: u.hash, color: URL_COLORS.hash },
    { key: 'href', label: '完整URL', value: u.href, color: URL_COLORS.protocol }
  ].filter(p => p.value)

  u.searchParams.forEach((val, key) => {
    urlParams.value.push({ key, raw: encodeURIComponent(val), decoded: decodeURIComponent(val) })
  })
}

</script>

<style lang="scss">
@import '../shared.scss';
</style>