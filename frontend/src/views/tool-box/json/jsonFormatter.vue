<template>
  <div class="json-tool-layout">

    <!-- ===== 左侧：输入区 ===== -->
    <div class="input-panel">
      <div class="panel-head">
        <span class="panel-title">
          <el-icon :size="13"><DocumentCopy /></el-icon>JSON 输入
        </span>
        <div class="head-actions">
          <el-button size="small" :icon="DocumentCopy" @click="pasteFromClipboard">粘贴</el-button>
          <el-button size="small" :icon="Delete" @click="clearInput">清空</el-button>
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
      <div class="panel-foot">
        <span v-if="inputError" class="err-msg">
          <el-icon :size="12"><CircleClose /></el-icon>{{ inputError }}
        </span>
        <span v-else-if="inputText" class="ok-msg">
          <el-icon :size="12"><CircleCheck /></el-icon>JSON 合法
        </span>
        <span v-else class="hint-msg">支持粘贴或手动输入</span>
        <span class="char-count">{{ inputText.length }} 字符</span>
      </div>
    </div>

    <!-- ===== 中间：操作按钮 ===== -->
    <div class="toolbar">
      <div class="tb-group-label">格式化</div>
      <div class="tb-btn" :class="{ active: activeTab === 'format' }" @click="doFormat">
        <el-icon>
          <Grid />
        </el-icon>
        <span>格式化</span>
      </div>
      <div class="tb-btn" :class="{ active: activeTab === 'compress' }" @click="doCompress">
        <el-icon>
          <Minus />
        </el-icon>
        <span>压缩</span>
      </div>
      <div class="tb-btn" :class="{ active: activeTab === 'escape' }" @click="doEscape">
        <el-icon>
          <Edit />
        </el-icon>
        <span>转义</span>
      </div>
      <div class="tb-btn" :class="{ active: activeTab === 'unescape' }" @click="doUnescape">
        <el-icon>
          <EditPen />
        </el-icon>
        <span>去转义</span>
      </div>

      <div class="tb-group-label">视图</div>
      <div class="tb-btn" :class="{ active: activeTab === 'tree' }" @click="doTree">
        <el-icon>
          <Share />
        </el-icon>
        <span>树视图</span>
      </div>

      <div class="tb-group-label">转换</div>
      <div class="tb-btn" :class="{ active: activeTab === 'java' }" @click="doJava">
        <el-icon>
          <Cpu />
        </el-icon>
        <span>转 Java</span>
      </div>
      <div class="tb-btn" :class="{ active: activeTab === 'xml' }" @click="doXml">
        <el-icon>
          <Document />
        </el-icon>
        <span>转 XML</span>
      </div>

      <div class="tb-group-label">其他</div>
      <div class="tb-btn" @click="copyResult">
        <el-icon>
          <CopyDocument />
        </el-icon>
        <span>复制结果</span>
      </div>
      <div class="tb-btn danger" @click="clearAll">
        <el-icon>
          <Delete />
        </el-icon>
        <span>全部清空</span>
      </div>
    </div>

    <!-- ===== 右侧：结果区 ===== -->
    <div class="result-panel">
      <div class="panel-head">
        <span class="panel-title">
          <el-icon :size="13"><Aim /></el-icon>
          {{ tabTitles[activeTab] || '结果' }}
        </span>
        <div class="head-actions">
          <el-button size="small" :icon="CopyDocument" @click="copyResult">复制结果</el-button>
        </div>
      </div>

      <!-- 格式化 / 压缩 / 转义 / 去转义 → 代码文本 -->
      <div
        v-if="['format','compress','escape','unescape'].includes(activeTab)"
        class="result-code"
      >
        <pre v-if="resultText" class="code-block" v-html="highlightedResult" />
        <div v-else class="result-placeholder">点击左侧按钮执行操作</div>
      </div>

      <!-- 树视图 -->
      <div v-else-if="activeTab === 'tree'" class="result-tree">
        <div v-if="treeData">
          <JsonTree :data="treeData" />
        </div>
        <div v-else class="result-placeholder">点击「树视图」解析 JSON 结构</div>
      </div>

      <!-- Java 实体 -->
      <div v-else-if="activeTab === 'java'" class="result-code">
        <pre v-if="resultText" class="code-block java-code">{{ resultText }}</pre>
        <div v-else class="result-placeholder">点击「转 Java」生成实体类</div>
      </div>

      <!-- XML -->
      <div v-else-if="activeTab === 'xml'" class="result-code">
        <pre v-if="resultText" class="code-block" v-html="highlightXml(resultText)" />
        <div v-else class="result-placeholder">点击「转 XML」生成 XML</div>
      </div>

      <!-- 初始空状态 -->
      <div v-else class="result-empty">
        <el-icon :size="40" class="empty-icon">
          <Document />
        </el-icon>
        <p>在左侧输入 JSON，选择操作查看结果</p>
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
    .replace(/\\/g, '\\\\')
    .replace(/"/g, '\\"')
    .replace(/\n/g, '\\n')
    .replace(/\r/g, '\\r')
    .replace(/\t/g, '\\t')
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
      .replace(/\\"/g, '"')
      .replace(/\\n/g, '\n')
      .replace(/\\r/g, '\r')
      .replace(/\\t/g, '\t')
      .replace(/\\\\/g, '\\')
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

  const cls = [
    `import lombok.Data;`,
    ``,
    `@Data`,
    `public class ${capitalize(className)} {`,
    ...fields,
    `}`
  ].join('\n')

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
  if (val === null || val === undefined) {
    return `${indent}<${tag}/>`
  }
  if (typeof val !== 'object') {
    const escaped = String(val)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
    return `${indent}<${tag}>${escaped}</${tag}>`
  }
  if (Array.isArray(val)) {
    return val.map(item => jsonToXml(item, tag, depth)).join('\n')
  }
  const children = Object.entries(val)
    .map(([k, v]) => jsonToXml(v, k, depth + 1))
    .join('\n')
  return `${indent}<${tag}>\n${children}\n${indent}</${tag}>`
}

/* ---- JSON 语法高亮 ---- */
const highlightedResult = computed(() => {
  if (!resultText.value) return ''
  return resultText.value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
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
  const text = activeTab.value === 'tree'
    ? JSON.stringify(treeData.value, null, 2)
    : resultText.value
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
/* ===== 整体布局 ===== */
.json-tool-layout {
  display: flex;
  height: calc(100vh - 56px);
  background: #f5f7fa;
  overflow: hidden;
}

/* ===== 公共头部 ===== */
.panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
  background: #fff;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  font-weight: 500;
  color: #606266;
}

.head-actions {
  display: flex;
  gap: 4px;
}

/* ===== 左侧输入区 ===== */
.input-panel {
  width: 38%;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  background: #fff;
}

.json-textarea {
  flex: 1;
  padding: 12px;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #303133;
  background: #fafafa;
  border: none;
  resize: none;
  outline: none;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ebeef5;
    border-radius: 2px;
  }

  &::placeholder {
    color: #c0c4cc;
  }
}

.panel-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 5px 12px;
  border-top: 1px solid #f0f0f0;
  font-size: 11px;
  flex-shrink: 0;
  background: #fff;
}

.err-msg {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f56c6c;
}

.ok-msg {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #67c23a;
}

.hint-msg {
  color: #c0c4cc;
}

.char-count {
  color: #c0c4cc;
}

/* ===== 中间工具栏 ===== */
.toolbar {
  width: 108px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
  padding: 8px 6px;
  gap: 3px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 0;
  }
}

.tb-group-label {
  font-size: 10px;
  color: #c0c4cc;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  padding: 6px 6px 2px;
  margin-top: 2px;

  &:first-child {
    margin-top: 0;
  }
}

.tb-btn {
  width: 100%;
  height: 32px;
  border-radius: 6px;
  border: 0.5px solid #ebeef5;
  background: #fff;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 8px;
  transition: background 0.12s, color 0.12s, border-color 0.12s;
  user-select: none;

  .el-icon {
    font-size: 14px;
    flex-shrink: 0;
  }

  &:hover {
    background: #ecf5ff;
    color: #409eff;
    border-color: #b3d8ff;
  }

  &.active {
    background: #ecf5ff;
    color: #409eff;
    border-color: #b3d8ff;
    font-weight: 500;
  }

  &.danger:hover {
    background: #fef0f0;
    color: #f56c6c;
    border-color: #fbc4c4;
  }
}

/* ===== 右侧结果区 ===== */
.result-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  background: #fff;
}

.result-code {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;

  &::-webkit-scrollbar {
    width: 6px;
    height: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ebeef5;
    border-radius: 3px;
  }
}

.code-block {
  margin: 0;
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: #303133;
  white-space: pre-wrap;
  word-break: break-all;
}

.java-code {
  color: #303133;
}

/* JSON 高亮 */
:deep(.hl-key) {
  color: #0C447C;
}

:deep(.hl-str) {
  color: #085041;
}

:deep(.hl-num) {
  color: #534AB7;
}

:deep(.hl-bool) {
  color: #712B13;
}

:deep(.hl-null) {
  color: #909399;
}

/* XML 高亮 */
:deep(.hl-xml-pi) {
  color: #909399;
}

:deep(.hl-xml-tag) {
  color: #606266;
}

:deep(.hl-xml-name) {
  color: #0C447C;
}

.result-tree {
  flex: 1;
  overflow: auto;
  padding: 12px 16px;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-thumb {
    background: #ebeef5;
    border-radius: 3px;
  }
}

.result-placeholder {
  color: #c0c4cc;
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
  color: #c0c4cc;

  .empty-icon {
    color: #dcdfe6;
  }

  p {
    font-size: 13px;
  }
}
</style>