<template>
  <div class="b64pdf-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
            <line x1="9" y1="13" x2="15" y2="13" />
            <line x1="9" y1="17" x2="13" y2="17" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">Base64 转 PDF</h2>
          <p class="header-sub">将 Base64 编码字符串解码并预览 / 下载 PDF 文件</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge" :class="statusClass">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
    </div>

    <!-- ===== Main Layout ===== -->
    <div class="main-layout">

      <!-- ── 左列：输入区 ── -->
      <div class="col col-left">
        <div class="card input-card">

          <div class="card-header">
            <span class="card-label">Base64 输入</span>
            <div class="card-actions">
              <button class="icon-btn" @click="pasteInput">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2" />
                  <rect x="8" y="2" width="8" height="4" rx="1" />
                </svg>
                粘贴
              </button>
              <button class="icon-btn" @click="loadSample">示例</button>
              <button class="icon-btn danger" @click="clearAll">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6l-1 14H6L5 6" />
                  <path d="M10 11v6M14 11v6M9 6V4h6v2" />
                </svg>
                清空
              </button>
            </div>
          </div>

          <textarea
            v-model="rawInput"
            class="b64-textarea"
            placeholder="将 Base64 字符串粘贴到此处...&#10;&#10;支持带 data URI 前缀，例如：&#10;data:application/pdf;base64,JVBERi0x..."
            spellcheck="false"
            @input="onInput"
          />

          <div class="card-foot">
            <span v-if="parseError" class="foot-msg err">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle
                cx="12" cy="12" r="10" /><line x1="15" y1="9" x2="9" y2="15" /><line x1="9" y1="9" x2="15"
                                                                                     y2="15" /></svg>
              {{ parseError }}
            </span>
            <span v-else-if="pdfDataUrl" class="foot-msg ok">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline
                points="20 6 9 17 4 12" /></svg>
              解码成功 · {{ fileSizeText }}
            </span>
            <span v-else class="foot-msg hint">支持粘贴原始 Base64 或 data URI 格式</span>
            <span class="char-count">{{ rawInput.length.toLocaleString() }} 字符</span>
          </div>
        </div>

        <!-- 操作按钮卡片 -->
        <div class="card action-card">
          <div class="card-header">
            <span class="card-label">操作</span>
          </div>
          <div class="action-body">
            <button class="action-btn primary" :disabled="!pdfDataUrl" @click="previewPDF">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              在线预览
            </button>
            <button class="action-btn success" :disabled="!pdfDataUrl" @click="downloadPDF">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="7 10 12 15 17 10" />
                <line x1="12" y1="15" x2="12" y2="3" />
              </svg>
              下载 PDF
            </button>
            <button class="action-btn neutral" :disabled="!pdfDataUrl" @click="copyBase64">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              {{ copied ? '已复制 ✓' : '复制 Base64' }}
            </button>
          </div>
          <!-- 文件信息 -->
          <div class="file-info" v-if="pdfDataUrl">
            <div class="info-row">
              <span class="info-key">文件大小</span>
              <span class="info-val">{{ fileSizeText }}</span>
            </div>
            <div class="info-row">
              <span class="info-key">字符长度</span>
              <span class="info-val">{{ cleanBase64.length.toLocaleString() }} 字符</span>
            </div>
            <div class="info-row">
              <span class="info-key">MIME 类型</span>
              <span class="info-val">{{ mimeType }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ── 右列：预览区 ── -->
      <div class="col col-right">
        <div class="card preview-card">
          <div class="card-header">
            <span class="card-label">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path
                d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" /><circle cx="12" cy="12" r="3" /></svg>
              PDF 预览
            </span>
            <div class="card-actions" v-if="pdfDataUrl">
              <button class="icon-btn accent" @click="downloadPDF">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                下载 PDF
              </button>
            </div>
          </div>

          <!-- 空状态 -->
          <div v-if="!pdfDataUrl && !parseError" class="preview-empty">
            <div class="empty-icon-wrap">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="9" y1="13" x2="15" y2="13" />
                <line x1="9" y1="17" x2="13" y2="17" />
              </svg>
            </div>
            <p class="empty-title">在左侧粘贴 Base64 字符串</p>
            <p class="empty-sub">解码成功后 PDF 将在此处预览</p>
          </div>

          <!-- 错误状态 -->
          <div v-else-if="parseError && !pdfDataUrl" class="preview-error">
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
              <circle cx="12" cy="12" r="10" />
              <line x1="15" y1="9" x2="9" y2="15" />
              <line x1="9" y1="9" x2="15" y2="15" />
            </svg>
            <p>{{ parseError }}</p>
          </div>

          <!-- PDF iframe 预览 -->
          <div v-else-if="pdfDataUrl" class="preview-iframe-wrap">
            <iframe
              :src="pdfDataUrl"
              class="pdf-iframe"
              type="application/pdf"
              title="PDF 预览"
            />
          </div>

        </div>
      </div>

    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

/* ============================================================
   State
   ============================================================ */
const rawInput = ref('')
const pdfDataUrl = ref('')
const cleanBase64 = ref('')
const mimeType = ref('application/pdf')
const parseError = ref('')
const copied = ref(false)

/* ============================================================
   Computed
   ============================================================ */
const fileSizeText = computed(() => {
  if (!cleanBase64.value) return ''
  const bytes = Math.floor(cleanBase64.value.length * 3 / 4)
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
})

const statusClass = computed(() => {
  if (!rawInput.value.trim()) return 'status-neutral'
  if (parseError.value) return 'status-error'
  if (pdfDataUrl.value) return 'status-valid'
  return 'status-neutral'
})

const statusText = computed(() => {
  if (!rawInput.value.trim()) return '等待输入'
  if (parseError.value) return '解码失败'
  if (pdfDataUrl.value) return `解码成功 · ${fileSizeText.value}`
  return '等待输入'
})

/* ============================================================
   Core: parse Base64 → PDF data URL
   ============================================================ */
function onInput() {
  const val = rawInput.value.trim()
  parseError.value = ''
  pdfDataUrl.value = ''
  cleanBase64.value = ''

  if (!val) return

  try {
    let b64 = val
    let mime = 'application/pdf'

    // 处理 data URI 前缀
    if (val.startsWith('data:')) {
      const semicolon = val.indexOf(';')
      const comma = val.indexOf(',')
      if (semicolon === -1 || comma === -1) {
        parseError.value = '无效的 data URI 格式'
        return
      }
      mime = val.slice(5, semicolon)  // e.g. application/pdf
      b64 = val.slice(comma + 1)
    }

    // 移除所有空白字符（粘贴时可能含换行/空格）
    b64 = b64.replace(/\s/g, '')

    // 验证是合法 Base64
    if (!/^[A-Za-z0-9+/=_-]+$/.test(b64)) {
      parseError.value = '包含非法字符，请检查是否为有效的 Base64 字符串'
      return
    }

    // 尝试解码验证
    const binary = atob(b64.replace(/-/g, '+').replace(/_/g, '/'))
    if (!binary) {
      parseError.value = 'Base64 解码失败'
      return
    }

    // 检查 PDF 魔数 %PDF
    if (binary.slice(0, 4) !== '%PDF') {
      // 如果不是 PDF 魔数但用户输入了 data URI 且不是 pdf mime，也给提示
      if (mime !== 'application/pdf') {
        parseError.value = `MIME 类型为 "${mime}"，不是 PDF 文件`
        return
      }
      parseError.value = '解码内容不是有效的 PDF 文件（缺少 %PDF 魔数）'
      return
    }

    mimeType.value = mime
    cleanBase64.value = b64
    pdfDataUrl.value = `data:${mime};base64,${b64}`
  } catch (e) {
    parseError.value = `解码出错：${e.message}`
  }
}

/* ============================================================
   Actions
   ============================================================ */
function previewPDF() {
  if (!pdfDataUrl.value) return
  // 在新标签页打开
  const w = window.open()
  if (w) {
    w.document.write(`
      <html><head><title>PDF 预览</title>
      <style>body{margin:0;background:#525659;}iframe{width:100%;height:100vh;border:none;}</style>
      </head><body>
      <iframe src="${pdfDataUrl.value}" type="application/pdf"></iframe>
      </body></html>
    `)
    w.document.close()
  }
}

function downloadPDF() {
  if (!pdfDataUrl.value) return
  const a = document.createElement('a')
  a.href = pdfDataUrl.value
  a.download = `document_${Date.now()}.pdf`
  a.click()
}

async function copyBase64() {
  if (!cleanBase64.value) return
  try {
    await navigator.clipboard.writeText(cleanBase64.value)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch {
    // fallback
    const ta = document.createElement('textarea')
    ta.value = cleanBase64.value
    document.body.appendChild(ta)
    ta.select()
    document.execCommand('copy')
    document.body.removeChild(ta)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}

async function pasteInput() {
  try {
    const text = await navigator.clipboard.readText()
    rawInput.value = text.trim()
    onInput()
  } catch {
    // 无权限时聚焦 textarea 让用户手动粘贴
  }
}

function clearAll() {
  rawInput.value = ''
  pdfDataUrl.value = ''
  cleanBase64.value = ''
  parseError.value = ''
  mimeType.value = 'application/pdf'
}

// 内置一个极小的合法 PDF Base64 作为示例
function loadSample() {
  // 这是一个最小合法 PDF（"Hello PDF"单页）的 Base64
  rawInput.value = `JVBERi0xLjQKMSAwIG9iago8PAovVHlwZSAvQ2F0YWxvZwovUGFnZXMgMiAwIFIKPj4KZW5kb2Jq
CjIgMCBvYmoKPDwKL1R5cGUgL1BhZ2VzCi9LaWRzIFszIDAgUl0KL0NvdW50IDEKL01lZGlhQm94
IFswIDAgMzAwIDE0NF0KPj4KZW5kb2JqCgozIDAgb2JqCjw8Ci9UeXBlIC9QYWdlCi9QYXJlbnQg
MiAwIFIKL1Jlc291cmNlcwo8PAovRm9udAo8PAovRjEKPDwKL1R5cGUgL0ZvbnQKL1N1YnR5cGUg
L1R5cGUxCi9CYXNlRm9udCAvVGltZXMtUm9tYW4KPj4KPj4KPj4KPj4KL0NvbnRlbnRzIDQgMCBS
Cj4+CmVuZG9iagoKNCAwIG9iago8PC9MZW5ndGggNDQ+PgpzdHJlYW0KQlQKL0YxIDE4IFRmCjMw
IDExMCBUZAooSGVsbG8gUERGISkgVGoKRVQKZW5kc3RyZWFtCmVuZG9iagoKeHJlZgowIDUKMDAw
MDAwMDAwMCA2NTUzNSBmIAowMDAwMDAwMDA5IDAwMDAwIG4gCjAwMDAwMDAwNTggMDAwMDAgbiAK
MDAwMDAwMDE1NSAwMDAwMCBuIAowMDAwMDAwMzIyIDAwMDAwIG4gCgp0cmFpbGVyCjw8Ci9TaXpl
IDUKLy9Sb290IDEgMCBSCj4+CnN0YXJ0eHJlZgo0MTQKJSVFT0YK`
  onInput()
}
</script>

<style lang="scss" scoped>
/* ── 整体容器 ── */
.b64pdf-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--color-bg-page);
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

.header-right {
  display: flex;
  align-items: center;
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
  min-height: 0;

  &.col-left {
    width: 360px;
    flex-shrink: 0;
  }

  &.col-right {
    flex: 1;
    min-width: 0;
  }
}

/* ── Card Base ── */
.card {
  background: var(--color-bg-card);
  border-radius: 12px;
  border: 1px solid var(--color-border-light);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.input-card {
  flex: 1;
  min-height: 0;
}

.preview-card {
  flex: 1;
  min-height: 0;
}

.action-card {
  flex-shrink: 0;
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
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
  color: var(--color-text-secondary);
  flex: 1;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ── Icon Buttons ── */
.icon-btn {
  display: inline-flex;
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
    background: var(--brand-primary-soft);
  }

  &.danger:hover {
    border-color: var(--color-danger);
    color: var(--color-danger);
    background: var(--color-danger-soft);
  }

  &.accent {
    background: var(--brand-primary-soft);
    border-color: var(--brand-primary);
    color: var(--brand-primary);
    font-weight: 600;
  }

  &.accent:hover {
    background: var(--brand-primary-soft);
  }
}

/* ── Textarea ── */
.b64-textarea {
  flex: 1;
  padding: 14px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.75;
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
  border: none;
  resize: none;
  outline: none;
  overflow-y: auto;
  word-break: break-all;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }

  &::placeholder {
    color: var(--color-text-placeholder);
    font-family: inherit;
    line-height: 1.8;
  }
}

/* ── Card Footer ── */
.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 14px;
  border-top: 1px solid var(--color-border-light);
  flex-shrink: 0;
  background: var(--color-bg-card);
}

.foot-msg {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;

  &.err {
    color: var(--color-danger);
  }

  &.ok {
    color: var(--color-success);
  }

  &.hint {
    color: var(--color-text-placeholder);
  }
}

.char-count {
  font-size: 11px;
  font-family: monospace;
  color: var(--color-text-placeholder);
}

/* ── Action Card ── */
.action-body {
  display: flex;
  gap: 8px;
  padding: 14px;
  flex-shrink: 0;
}

.action-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 9px 0;
  border-radius: 8px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all .18s;

  &:disabled {
    opacity: .4;
    cursor: not-allowed;
  }

  &.primary {
    background: var(--brand-primary);
    color: #fff;

    &:hover:not(:disabled) {
      background: var(--brand-primary-hover);
      box-shadow: 0 3px 10px rgba(64, 158, 255, .35);
      transform: translateY(-1px);
    }
  }

  &.success {
    background: var(--color-success);
    color: #fff;

    &:hover:not(:disabled) {
      background: #15803d;
      box-shadow: 0 3px 10px rgba(22, 163, 74, .35);
      transform: translateY(-1px);
    }
  }

  &.neutral {
    background: var(--color-bg-hover);
    border: 1.5px solid var(--color-border);
    color: var(--color-text-regular);

    &:hover:not(:disabled) {
      border-color: var(--brand-primary);
      color: var(--brand-primary);
      background: var(--brand-primary-soft);
    }
  }
}

/* ── File Info ── */
.file-info {
  padding: 0 14px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  border-top: 1px dashed var(--color-border-light);
  margin-top: -2px;
  padding-top: 12px;
}

.info-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
}

.info-key {
  color: var(--color-text-secondary);
}

.info-val {
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 11px;
  color: var(--color-text-primary);
  background: var(--color-bg-hover);
  padding: 1px 8px;
  border-radius: 4px;
  border: 1px solid var(--color-border-light);
}

/* ── Preview Area ── */
.preview-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.empty-icon-wrap {
  color: var(--color-border);
  opacity: .6;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text-secondary);
}

.empty-sub {
  margin: 0;
  font-size: 12px;
  color: var(--color-text-placeholder);
}

.preview-error {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--color-danger);
  padding: 24px;
  text-align: center;
  font-size: 13px;
}

.preview-iframe-wrap {
  flex: 1;
  min-height: 0;
  background: #525659;
}

.pdf-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/* ══ 移动端适配 ══ */
@media screen and (max-width: 768px) {
  .b64pdf-page {
    padding: 12px;
    gap: 12px;
    overflow-y: auto;
    height: auto;
    min-height: 100%;
    padding-bottom: 24px;
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

  /* 状态标签在窄屏收窄，避免挤压标题 */
  .status-badge {
    padding: 4px 10px;
    font-size: 11px;
    gap: 5px;
  }

  .main-layout {
    flex-direction: column;
    gap: 12px;
    overflow: visible;
  }

  .col {
    gap: 12px;

    &.col-left {
      width: 100%;
      flex-shrink: 0;
    }

    &.col-right {
      width: 100%;
      flex: none;
    }
  }

  /* 输入框：移动端给固定高度，避免 flex 塌陷 */
  .input-card {
    flex: none;
  }

  .b64-textarea {
    min-height: 200px;
  }

  /* 操作按钮：窄屏改为纵向排列，避免文字挤压溢出 */
  .action-body {
    flex-wrap: wrap;
  }

  .action-btn {
    flex: 1 1 100%;
    padding: 11px 0;
  }

  /* 预览区：移动端给固定高度 */
  .preview-card {
    flex: none;
    min-height: 300px;
  }

  .preview-iframe-wrap {
    min-height: 400px;
  }

  .preview-empty {
    padding: 32px 16px;
    min-height: 260px;
  }

  /* 输入区底部：提示与字数统计在窄屏换行，避免挤在一起 */
  .card-foot {
    flex-wrap: wrap;
    gap: 4px;
    padding: 8px 14px;
  }

  .foot-msg {
    flex: 1 1 auto;
    min-width: 0;
  }

  .char-count {
    flex-shrink: 0;
  }
}
</style>