<template>
  <div class="json-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="20">
            <Document />
          </el-icon>
        </div>
        <div>
          <h2 class="header-title">JSON 工具</h2>
          <p class="header-sub">格式化、压缩、转换 JSON 数据</p>
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

      <!-- 左侧：输入区 -->
      <div class="col col-left">
        <div class="card input-card">
          <div class="card-header">
            <span class="card-label">
              <el-icon :size="11"><DocumentCopy /></el-icon>JSON 输入
            </span>
            <div class="card-actions">
              <button class="icon-btn" @click="pasteFromClipboard">
                <el-icon :size="11">
                  <DocumentCopy />
                </el-icon>
                粘贴
              </button>
              <button class="icon-btn danger" @click="clearInput">
                <el-icon :size="11">
                  <Delete />
                </el-icon>
                清空
              </button>
            </div>
          </div>
          <textarea
            ref="inputRef"
            v-model="inputText"
            class="json-textarea"
            placeholder='在此粘贴或输入 JSON，例如：{"name":"张三","age":18}'
            spellcheck="false"
            @input="onInput"
          />
          <div class="card-foot">
            <span v-if="inputError" class="foot-msg err">
              <el-icon :size="11"><CircleClose /></el-icon>{{ inputError }}
            </span>
            <span v-else-if="inputText" class="foot-msg ok">
              <el-icon :size="11"><CircleCheck /></el-icon>JSON 合法
            </span>
            <span v-else class="foot-msg hint">支持粘贴或手动输入</span>
            <span class="char-count">{{ inputText.length }} 字符</span>
          </div>
        </div>
      </div>

      <!-- 中间：工具栏 -->
      <div class="col col-toolbar">
        <div class="card toolbar-card">

          <div class="tb-section">
            <div class="tb-section-label">格式化</div>
            <button class="tb-btn" :class="{ active: activeTab === 'format' }" @click="doFormat">
              <el-icon>
                <Grid />
              </el-icon>
              <span>格式化</span>
            </button>
            <button class="tb-btn" :class="{ active: activeTab === 'compress' }" @click="doCompress">
              <el-icon>
                <Minus />
              </el-icon>
              <span>压缩</span>
            </button>
            <button class="tb-btn" :class="{ active: activeTab === 'escape' }" @click="doEscape">
              <el-icon>
                <Edit />
              </el-icon>
              <span>转义</span>
            </button>
            <button class="tb-btn" :class="{ active: activeTab === 'unescape' }" @click="doUnescape">
              <el-icon>
                <EditPen />
              </el-icon>
              <span>去转义</span>
            </button>
          </div>

          <div class="tb-divider" />

          <div class="tb-section">
            <div class="tb-section-label">视图</div>
            <button class="tb-btn" :class="{ active: activeTab === 'tree' }" @click="doTree">
              <el-icon>
                <Share />
              </el-icon>
              <span>树视图</span>
            </button>
          </div>

          <div class="tb-divider" />

          <div class="tb-section">
            <div class="tb-section-label">转换</div>
            <button class="tb-btn" :class="{ active: activeTab === 'java' }" @click="doJava">
              <el-icon>
                <Cpu />
              </el-icon>
              <span>转 Java</span>
            </button>
            <button class="tb-btn" :class="{ active: activeTab === 'xml' }" @click="doXml">
              <el-icon>
                <Document />
              </el-icon>
              <span>转 XML</span>
            </button>
          </div>

          <div class="tb-divider" />

          <div class="tb-section tb-section-bottom">
            <button class="tb-btn accent" @click="copyResult">
              <el-icon>
                <CopyDocument />
              </el-icon>
              <span>复制结果</span>
            </button>
            <button class="tb-btn danger" @click="clearAll">
              <el-icon>
                <Delete />
              </el-icon>
              <span>全部清空</span>
            </button>
          </div>

        </div>
      </div>

      <!-- 右侧：结果区 -->
      <div class="col col-right">
        <div class="card result-card">
          <div class="card-header">
            <span class="card-label">
              <el-icon :size="11"><Aim /></el-icon>
              {{ tabTitles[activeTab] || '结果' }}
            </span>
            <button class="icon-btn accent" @click="copyResult">
              <el-icon :size="11">
                <CopyDocument />
              </el-icon>
              复制结果
            </button>
          </div>

          <!-- 格式化 / 压缩 / 转义 / 去转义 → 代码文本 -->
          <div v-if="['format','compress','escape','unescape'].includes(activeTab)" class="result-scroll">
            <pre v-if="resultText" class="code-block" v-html="highlightedResult" />
            <div v-else class="result-placeholder">点击左侧按钮执行操作</div>
          </div>

          <!-- 树视图 -->
          <div v-else-if="activeTab === 'tree'" class="result-scroll">
            <JsonTree v-if="treeData" :data="treeData" />
            <div v-else class="result-placeholder">点击「树视图」解析 JSON 结构</div>
          </div>

          <!-- Java 实体 -->
          <div v-else-if="activeTab === 'java'" class="result-scroll">
            <pre v-if="resultText" class="code-block java-code">{{ resultText }}</pre>
            <div v-else class="result-placeholder">点击「转 Java」生成实体类</div>
          </div>

          <!-- XML -->
          <div v-else-if="activeTab === 'xml'" class="result-scroll">
            <pre v-if="resultText" class="code-block" v-html="highlightXml(resultText)" />
            <div v-else class="result-placeholder">点击「转 XML」生成 XML</div>
          </div>

          <!-- 初始空状态 -->
          <div v-else class="result-empty">
            <el-icon :size="44" class="empty-icon">
              <Document />
            </el-icon>
            <p>在左侧输入 JSON，选择操作查看结果</p>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import {
  DocumentCopy, Delete, Grid, Minus, Edit, EditPen,
  Share, Cpu, Document, CopyDocument, CircleCheck,
  CircleClose, Aim
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import JsonTree from './components/jsonTree.vue'

/* ---- 状态 ---- */
const inputText = ref('')
const inputError = ref('')
const resultText = ref('')
const activeTab = ref('')
const treeData = ref(null)
const inputRef = ref(null)

const tabTitles = {
  format: '格式化结果',
  compress: '压缩结果',
  escape: '转义结果',
  unescape: '去转义结果',
  tree: '树视图',
  java: 'Java 实体类',
  xml: 'XML 结果'
}

/* ---- 状态徽章 ---- */
const statusClass = computed(() => {
  if (!inputText.value.trim()) return 'status-neutral'
  if (inputError.value) return 'status-error'
  return 'status-valid'
})
const statusText = computed(() => {
  if (!inputText.value.trim()) return '等待输入'
  if (inputError.value) return 'JSON 格式错误'
  return 'JSON 合法'
})

/* ---- 解析 JSON ---- */
function parseJson() {
  if (!inputText.value.trim()) {
    ElMessage.warning('请先输入 JSON 内容')
    return null
  }
  try {
    return JSON.parse(inputText.value)
  } catch (e) {
    ElMessage.error('JSON 格式错误：' + e.message)
    return null
  }
}

/* ---- 实时校验 ---- */
function onInput() {
  if (!inputText.value.trim()) {
    inputError.value = ''
    return
  }
  try {
    JSON.parse(inputText.value)
    inputError.value = ''
  } catch (e) {
    inputError.value = e.message
  }
}

/* ---- 格式化 ---- */
function doFormat() {
  const obj = parseJson()
  if (obj === null) return
  resultText.value = JSON.stringify(obj, null, 2)
  activeTab.value = 'format'
}

/* ---- 压缩 ---- */
function doCompress() {
  const obj = parseJson()
  if (obj === null) return
  resultText.value = JSON.stringify(obj)
  activeTab.value = 'compress'
}

/* ---- 转义 ---- */
function doEscape() {
  if (!inputText.value.trim()) {
    ElMessage.warning('请先输入 JSON 内容')
    return
  }
  resultText.value = inputText.value
    .replace(/\\/g, '\\\\').replace(/"/g, '\\"')
    .replace(/\n/g, '\\n').replace(/\r/g, '\\r').replace(/\t/g, '\\t')
  activeTab.value = 'escape'
}

/* ---- 去转义 ---- */
function doUnescape() {
  if (!inputText.value.trim()) {
    ElMessage.warning('请先输入内容')
    return
  }
  try {
    resultText.value = inputText.value
      .replace(/\\"/g, '"').replace(/\\n/g, '\n')
      .replace(/\\r/g, '\r').replace(/\\t/g, '\t').replace(/\\\\/g, '\\')
    activeTab.value = 'unescape'
  } catch (e) {
    ElMessage.error('去转义失败：' + e.message)
  }
}

/* ---- 树视图 ---- */
function doTree() {
  const obj = parseJson()
  if (obj === null) return
  treeData.value = obj
  resultText.value = ''
  activeTab.value = 'tree'
}

/* ---- 转 Java 实体 ---- */
function doJava() {
  const obj = parseJson()
  if (obj === null) return
  if (typeof obj !== 'object' || Array.isArray(obj)) {
    ElMessage.warning('请输入 JSON 对象（非数组）来生成 Java 实体')
    return
  }
  resultText.value = jsonToJava(obj, 'Root')
  activeTab.value = 'java'
}

function jsonToJava(obj, className) {
  const classes = []
  buildJavaClass(obj, className, classes)
  return classes.join('\n\n')
}

function buildJavaClass(obj, className, classes) {
  const fields = []
  const nested = []
  for (const [key, val] of Object.entries(obj)) {
    const javaType = getJavaType(val, key, nested, classes)
    const fieldName = toCamelCase(key)
    fields.push(`    private ${javaType} ${fieldName};`)
  }
  const cls = [`import lombok.Data;`, ``, `@Data`, `public class ${capitalize(className)} {`, ...fields, `}`].join('\n')
  classes.push(cls)
  nested.forEach(({ obj: o, name }) => buildJavaClass(o, name, classes))
}

function getJavaType(val, key, nested, classes) {
  if (val === null) return 'Object'
  if (typeof val === 'boolean') return 'Boolean'
  if (typeof val === 'number') return Number.isInteger(val) ? 'Integer' : 'Double'
  if (typeof val === 'string') return 'String'
  if (Array.isArray(val)) {
    if (val.length > 0 && typeof val[0] === 'object' && val[0] !== null) {
      const innerName = capitalize(key) + 'Item'
      nested.push({ obj: val[0], name: innerName })
      return `List<${innerName}>`
    }
    const elType = val.length > 0 ? getJavaType(val[0], key, nested, classes) : 'Object'
    return `List<${elType}>`
  }
  if (typeof val === 'object') {
    const innerName = capitalize(key)
    nested.push({ obj: val, name: innerName })
    return innerName
  }
  return 'Object'
}

function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
}

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1)
}

/* ---- 转 XML ---- */
function doXml() {
  const obj = parseJson()
  if (obj === null) return
  try {
    resultText.value = '<?xml version="1.0" encoding="UTF-8"?>\n' + jsonToXml(obj, 'root', 0)
    activeTab.value = 'xml'
  } catch (e) {
    ElMessage.error('转换失败：' + e.message)
  }
}

function jsonToXml(val, tag, depth) {
  const indent = '  '.repeat(depth)
  if (val === null || val === undefined) return `${indent}<${tag}/>`
  if (typeof val !== 'object') {
    const escaped = String(val).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    return `${indent}<${tag}>${escaped}</${tag}>`
  }
  if (Array.isArray(val)) return val.map(item => jsonToXml(item, tag, depth)).join('\n')
  const children = Object.entries(val).map(([k, v]) => jsonToXml(v, k, depth + 1)).join('\n')
  return `${indent}<${tag}>\n${children}\n${indent}</${tag}>`
}

/* ---- JSON 语法高亮 ---- */
const highlightedResult = computed(() => {
  if (!resultText.value) return ''
  return resultText.value
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(
      /("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
      match => {
        if (/^"/.test(match)) {
          if (/:$/.test(match)) return `<span class="hl-key">${match}</span>`
          return `<span class="hl-str">${match}</span>`
        }
        if (/true|false/.test(match)) return `<span class="hl-bool">${match}</span>`
        if (/null/.test(match)) return `<span class="hl-null">${match}</span>`
        return `<span class="hl-num">${match}</span>`
      }
    )
})

/* ---- XML 语法高亮 ---- */
function highlightXml(xml) {
  return xml
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/&lt;\?([^?]*)\?&gt;/g, '<span class="hl-xml-pi">&lt;?$1?&gt;</span>')
    .replace(/&lt;\/([^&]+)&gt;/g, '<span class="hl-xml-tag">&lt;/<span class="hl-xml-name">$1</span>&gt;</span>')
    .replace(/&lt;([^/&\s][^&\s>]*)([^&>]*)\/&gt;/g, '<span class="hl-xml-tag">&lt;<span class="hl-xml-name">$1</span>$2/&gt;</span>')
    .replace(/&lt;([^/&\s][^&\s>]*)([^&>]*)&gt;/g, '<span class="hl-xml-tag">&lt;<span class="hl-xml-name">$1</span>$2&gt;</span>')
}

/* ---- 复制结果 ---- */
async function copyResult() {
  const text = activeTab.value === 'tree' ? JSON.stringify(treeData.value, null, 2) : resultText.value
  if (!text) {
    ElMessage.warning('暂无结果可复制')
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

/* ---- 粘贴 ---- */
async function pasteFromClipboard() {
  try {
    const text = await navigator.clipboard.readText()
    inputText.value = text
    onInput()
  } catch {
    ElMessage.error('读取剪贴板失败，请手动粘贴')
  }
}

/* ---- 清空 ---- */
function clearInput() {
  inputText.value = ''
  inputError.value = ''
}

function clearAll() {
  clearInput()
  resultText.value = ''
  treeData.value = null
  activeTab.value = ''
}
</script>

<style lang="scss" scoped>
/* ═══════════════════════════════════════
   页面容器
   ═══════════════════════════════════════ */
.json-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
  gap: 16px;
  background: var(--el-bg-color-page, #f5f7fa);
  overflow: hidden;
}

/* ═══════════════════════════════════════
   Page Header（与 Token 页一致）
   ═══════════════════════════════════════ */
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
    background: var(--el-fill-color, #f0f2f5);
    border-color: var(--el-border-color, #dcdfe6);
    color: var(--el-text-color-secondary, #909399);
  }

  &.status-valid {
    background: #f0fdf4;
    border-color: #86efac;
    color: #16a34a;
  }

  &.status-error {
    background: #fff1f2;
    border-color: #fca5a5;
    color: #dc2626;
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

  .status-error & {
    background: #dc2626;
    box-shadow: 0 0 0 2px #fca5a5;
  }
}

/* ═══════════════════════════════════════
   Main Layout
   ═══════════════════════════════════════ */
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
  min-height: 0;

  &.col-left {
    width: 38%;
    flex-shrink: 0;
  }

  &.col-toolbar {
    width: 120px;
    flex-shrink: 0;
  }

  &.col-right {
    flex: 1;
    min-width: 0;
  }
}

/* ═══════════════════════════════════════
   Card 基础（与 Token 页一致）
   ═══════════════════════════════════════ */
.card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
  box-sizing: border-box;
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
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .06em;
  text-transform: uppercase;
  color: var(--el-text-color-secondary, #909399);
  flex: 1;
}

.card-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* ── Icon Button（与 Token 页一致）── */
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
    border-color: var(--el-color-danger, #f56c6c);
    color: var(--el-color-danger, #f56c6c);
    background: #fff1f2;
  }

  &.accent {
    background: var(--el-color-primary-light-9, #ecf5ff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
    color: var(--el-color-primary, #409eff);
    font-weight: 600;
  }

  &.accent:hover {
    background: var(--el-color-primary-light-8, #d9ecff);
  }
}

/* ═══════════════════════════════════════
   左侧：输入卡片
   ═══════════════════════════════════════ */
.input-card {
  flex: 1;
}

.json-textarea {
  flex: 1;
  padding: 14px;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.75;
  color: var(--el-text-color-primary, #303133);
  background: var(--el-fill-color-lighter, #fafafa);
  border: none;
  resize: none;
  outline: none;
  overflow-y: auto;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }

  &::placeholder {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

.card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 7px 14px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
  background: var(--el-bg-color, #fff);
}

.foot-msg {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;

  &.err {
    color: var(--el-color-danger, #f56c6c);
  }

  &.ok {
    color: var(--el-color-success, #67c23a);
  }

  &.hint {
    color: var(--el-text-color-placeholder, #c0c4cc);
  }
}

.char-count {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
}

/* ═══════════════════════════════════════
   中间：工具栏卡片
   ═══════════════════════════════════════ */
.toolbar-card {
  padding: 10px 8px;
  gap: 0;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 0;
  }
}

.tb-section {
  display: flex;
  flex-direction: column;
  gap: 3px;

  &.tb-section-bottom {
    margin-top: auto;
  }
}

.tb-section-label {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: .06em;
  text-transform: uppercase;
  color: var(--el-text-color-placeholder, #c0c4cc);
  padding: 4px 6px 2px;
}

.tb-divider {
  height: 1px;
  background: var(--el-border-color-lighter, #ebeef5);
  margin: 8px 0;
}

.tb-btn {
  width: 100%;
  height: 32px;
  border-radius: 7px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: transparent;
  font-size: 12px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  transition: all .15s;
  white-space: nowrap;
  box-sizing: border-box;

  .el-icon {
    font-size: 13px;
    flex-shrink: 0;
  }

  &:hover {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
  }

  &.active {
    background: var(--el-color-primary-light-9, #ecf5ff);
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
    font-weight: 600;
  }

  &.accent {
    background: var(--el-color-primary-light-9, #ecf5ff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
    color: var(--el-color-primary, #409eff);
    font-weight: 600;

    &:hover {
      background: var(--el-color-primary-light-8, #d9ecff);
    }
  }

  &.danger:hover {
    background: #fff1f2;
    color: var(--el-color-danger, #f56c6c);
    border-color: #fca5a5;
  }
}

/* ═══════════════════════════════════════
   右侧：结果卡片
   ═══════════════════════════════════════ */
.result-card {
  flex: 1;
}

.result-scroll {
  flex: 1;
  overflow: auto;
  padding: 14px 16px;
  min-height: 0;

  &::-webkit-scrollbar {
    width: 5px;
    height: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

.code-block {
  margin: 0;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.75;
  color: var(--el-text-color-primary, #303133);
  white-space: pre-wrap;
  word-break: break-all;
}

.java-code {
  color: var(--el-text-color-primary, #303133);
}

/* ── JSON 语法高亮 ── */
:deep(.hl-key) {
  color: #409eff;
}

:deep(.hl-str) {
  color: #16a34a;
}

:deep(.hl-num) {
  color: #d97706;
}

:deep(.hl-bool) {
  color: #db2777;
  font-weight: 600;
}

:deep(.hl-null) {
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-style: italic;
}

/* ── XML 高亮 ── */
:deep(.hl-xml-pi) {
  color: var(--el-text-color-secondary, #909399);
}

:deep(.hl-xml-tag) {
  color: var(--el-text-color-regular, #606266);
}

:deep(.hl-xml-name) {
  color: #409eff;
}

.result-placeholder {
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-size: 13px;
  padding: 24px 0;
  text-align: center;
}

.result-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);

  .empty-icon {
    color: var(--el-border-color, #dcdfe6);
  }

  p {
    font-size: 13px;
    margin: 0;
  }
}
</style>