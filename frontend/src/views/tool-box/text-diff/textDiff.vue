<template>
  <div class="diff-page">

    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4" />
            <line x1="6" y1="20" x2="6" y2="14" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">文本差异对比</h2>
          <p class="header-sub">逐行对比两段文本，高亮增删改内容</p>
        </div>
      </div>
      <div class="header-right">
        <div class="status-badge" :class="statusClass">
          <span class="status-dot" />
          {{ statusText }}
        </div>
      </div>
    </div>

    <div class="sub-toolbar">
      <div class="toolbar-left">
        <div class="seg-ctrl">
          <button :class="{ active: viewMode === 'split' }" @click="viewMode = 'split'"
                  data-tooltip="左右分栏，双列同步对照显示">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="8" height="18" rx="1" />
              <rect x="13" y="3" width="8" height="18" rx="1" />
            </svg>
            分栏视图
          </button>
          <button :class="{ active: viewMode === 'unified' }" @click="viewMode = 'unified'"
                  data-tooltip="单栏上下合并，呈现完整差异流">
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="1" />
              <line x1="3" y1="9" x2="21" y2="9" />
              <line x1="3" y1="15" x2="21" y2="15" />
            </svg>
            合并视图
          </button>
        </div>
        <div class="seg-ctrl">
          <button :class="{ active: diffMode === 'line' }" @click="diffMode = 'line'; runDiff()"
                  data-tooltip="以“行”为最小单位进行对比对齐">逐行
          </button>
          <button :class="{ active: diffMode === 'word' }" @click="diffMode = 'word'; runDiff()"
                  data-tooltip="以“词/空格”为单位进行精细化对比">逐词
          </button>
          <button :class="{ active: diffMode === 'char' }" @click="diffMode = 'char'; runDiff()"
                  data-tooltip="以单字/单个字符为单位进行极极致对比">逐字
          </button>
        </div>
        <label class="opt-toggle" data-tooltip="对比时去除每一行首尾的空格或制表符">
          <input type="checkbox" v-model="ignoreWhitespace" @change="runDiff" />
          忽略首尾空白
        </label>
        <label class="opt-toggle" data-tooltip="英文字母大小写视为相同内容">
          <input type="checkbox" v-model="ignoreCase" @change="runDiff" />
          忽略大小写
        </label>
      </div>
      <div class="toolbar-right">
        <div v-if="diffStats.total > 0" class="diff-stats">
          <span class="stat added" data-tooltip="本次修改中：纯新增的文本行数">+{{ diffStats.added }}</span>
          <span class="stat removed" data-tooltip="本次修改中：纯删除的文本行数">−{{ diffStats.removed }}</span>
          <span class="stat changed" data-tooltip="本次修改中：内容发生变更的对齐行数">~{{ diffStats.changed }}</span>
        </div>
        <button class="icon-btn" @click="swapTexts" data-tooltip="快速调换左右两侧文本框的内容位置">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="17 1 21 5 17 9" />
            <path d="M3 11V9a4 4 0 0 1 4-4h14" />
            <polyline points="7 23 3 19 7 15" />
            <path d="M21 13v2a4 4 0 0 1-4 4H3" />
          </svg>
          互换
        </button>
        <button class="icon-btn" @click="clearAll" data-tooltip="一键清空全部输入和对比结果">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="3 6 5 6 21 6" />
            <path d="M19 6l-1 14H6L5 6" />
            <path d="M10 11v6M14 11v6M9 6V4h6v2" />
          </svg>
          清空
        </button>
        <button class="icon-btn accent" @click="copyResult" :disabled="diffLines.length === 0"
                data-tooltip="复制带有 + - 前缀的标准差异报告">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="9" y="9" width="13" height="13" rx="2" />
            <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
          </svg>
          {{ copied ? '已复制 ✓' : '复制结果' }}
        </button>
      </div>
    </div>

    <div class="main-layout">

      <template v-if="viewMode === 'split'">

        <div class="col col-input">
          <div class="card input-card">
            <div class="card-header">
              <div class="input-label original">原始文本</div>
              <div class="card-actions">
                <button class="icon-btn sm" @click="pasteText('original')">粘贴</button>
                <button class="icon-btn sm" @click="loadSample">示例</button>
                <button class="icon-btn sm danger" @click="originalText = ''; runDiff()">清空</button>
              </div>
            </div>
            <textarea
              v-model="originalText"
              class="diff-textarea"
              placeholder="粘贴原始文本..."
              spellcheck="false"
              @input="debouncedDiff"
            />
            <div class="card-foot">
              <span class="foot-hint">{{ lineCount(originalText) }} 行 · {{ originalText.length }} 字符</span>
            </div>
          </div>
        </div>

        <div class="col-divider">
          <div class="divider-line" />
        </div>

        <div class="col col-input">
          <div class="card input-card">
            <div class="card-header">
              <div class="input-label modified">修改文本</div>
              <div class="card-actions">
                <button class="icon-btn sm" @click="pasteText('modified')">粘贴</button>
                <button class="icon-btn sm danger" @click="modifiedText = ''; runDiff()">清空</button>
              </div>
            </div>
            <textarea
              v-model="modifiedText"
              class="diff-textarea"
              placeholder="粘贴修改后的文本..."
              spellcheck="false"
              @input="debouncedDiff"
            />
            <div class="card-foot">
              <span class="foot-hint">{{ lineCount(modifiedText) }} 行 · {{ modifiedText.length }} 字符</span>
            </div>
          </div>
        </div>

        <div v-if="diffLines.length > 0" class="col col-result-split">
          <div class="card result-card">
            <div class="card-header">
              <span class="card-label">对比结果</span>
              <span v-if="diffStats.total === 0" class="identical-badge">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline
                  points="20 6 9 17 4 12" /></svg>
                两段文本完全相同
              </span>
            </div>
            <div class="diff-split-result">
              <div class="split-col original-col" ref="leftScrollRef">
                <div
                  v-for="(row, i) in splitRows"
                  :key="'l'+i"
                  class="diff-row"
                  :class="row.leftClass"
                >
                  <span class="line-no">{{ row.leftNo || '' }}</span>
                  <span class="line-content" v-html="row.leftHtml || '&nbsp;'" />
                </div>
              </div>
              <div class="split-col modified-col" ref="rightScrollRef">
                <div
                  v-for="(row, i) in splitRows"
                  :key="'r'+i"
                  class="diff-row"
                  :class="row.rightClass"
                >
                  <span class="line-no">{{ row.rightNo || '' }}</span>
                  <span class="line-content" v-html="row.rightHtml || '&nbsp;'" />
                </div>
              </div>
            </div>
          </div>
        </div>

      </template>

      <template v-else>
        <div class="col col-inputs-row">
          <div class="card input-card">
            <div class="card-header">
              <div class="input-label original">原始文本</div>
              <div class="card-actions">
                <button class="icon-btn sm" @click="pasteText('original')">粘贴</button>
                <button class="icon-btn sm" @click="loadSample">示例</button>
                <button class="icon-btn sm danger" @click="originalText = ''; runDiff()">清空</button>
              </div>
            </div>
            <textarea
              v-model="originalText"
              class="diff-textarea"
              placeholder="粘贴原始文本..."
              spellcheck="false"
              @input="debouncedDiff"
            />
            <div class="card-foot">
              <span class="foot-hint">{{ lineCount(originalText) }} 行 · {{ originalText.length }} 字符</span>
            </div>
          </div>
          <div class="card input-card">
            <div class="card-header">
              <div class="input-label modified">修改文本</div>
              <div class="card-actions">
                <button class="icon-btn sm" @click="pasteText('modified')">粘贴</button>
                <button class="icon-btn sm danger" @click="modifiedText = ''; runDiff()">清空</button>
              </div>
            </div>
            <textarea
              v-model="modifiedText"
              class="diff-textarea"
              placeholder="粘贴修改后的文本..."
              spellcheck="false"
              @input="debouncedDiff"
            />
            <div class="card-foot">
              <span class="foot-hint">{{ lineCount(modifiedText) }} 行 · {{ modifiedText.length }} 字符</span>
            </div>
          </div>
        </div>

        <div class="col col-unified-result">
          <div class="card result-card" v-if="diffLines.length > 0">
            <div class="card-header">
              <span class="card-label">对比结果</span>
              <span v-if="diffStats.total === 0" class="identical-badge">
                <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><polyline
                  points="20 6 9 17 4 12" /></svg>
                两段文本完全相同
              </span>
            </div>
            <div class="diff-unified-result">
              <div
                v-for="(line, i) in diffLines"
                :key="i"
                class="diff-row"
                :class="line.type"
              >
                <span class="line-sign">{{ lineSign(line.type) }}</span>
                <span class="line-no-left">{{ line.noLeft || '' }}</span>
                <span class="line-no-right">{{ line.noRight || '' }}</span>
                <span class="line-content" v-html="line.html" />
              </div>
            </div>
          </div>

          <div class="card empty-state" v-else>
            <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
                 opacity="0.2">
              <line x1="18" y1="20" x2="18" y2="10" />
              <line x1="12" y1="20" x2="12" y2="4" />
              <line x1="6" y1="20" x2="6" y2="14" />
            </svg>
            <p class="empty-title">在上方粘贴两段文本</p>
            <p class="empty-sub">支持逐行 / 逐词 / 逐字三种对比模式</p>
          </div>
        </div>
      </template>

      <div v-if="viewMode === 'split' && diffLines.length === 0" class="col col-empty-split">
        <div class="card empty-state">
          <svg width="52" height="52" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
               opacity="0.2">
            <line x1="18" y1="20" x2="18" y2="10" />
            <line x1="12" y1="20" x2="12" y2="4" />
            <line x1="6" y1="20" x2="6" y2="14" />
          </svg>
          <p class="empty-title">在上方粘贴两段文本</p>
          <p class="empty-sub">支持逐行 / 逐词 / 逐字三种对比模式</p>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'

/* ============================================================
   State
   ============================================================ */
const originalText = ref('')
const modifiedText = ref('')
const viewMode = ref('split')    // split | unified
const diffMode = ref('line')     // line | word | char
const ignoreWhitespace = ref(false)
const ignoreCase = ref(false)
const diffLines = ref([])
const splitRows = ref([])
const copied = ref(false)
const leftScrollRef = ref(null)
const rightScrollRef = ref(null)

/* ============================================================
   Stats
   ============================================================ */
const diffStats = computed(() => {
  let added = 0, removed = 0, changed = 0
  for (const l of diffLines.value) {
    if (l.type === 'added') added++
    if (l.type === 'removed') removed++
    if (l.type === 'changed-add' || l.type === 'changed-remove') changed++
  }
  return { added, removed, changed: Math.floor(changed / 2), total: added + removed + changed }
})

const statusClass = computed(() => {
  if (!originalText.value && !modifiedText.value) return 'status-neutral'
  if (diffStats.value.total === 0 && diffLines.value.length > 0) return 'status-valid'
  if (diffStats.value.total > 0) return 'status-diff'
  return 'status-neutral'
})

const statusText = computed(() => {
  if (!originalText.value && !modifiedText.value) return '等待输入'
  if (diffLines.value.length === 0) return '等待输入'
  if (diffStats.value.total === 0) return '两段文本完全相同'
  const { added, removed, changed } = diffStats.value
  const parts = []
  if (added) parts.push(`+${added} 行`)
  if (removed) parts.push(`−${removed} 行`)
  if (changed) parts.push(`~${changed} 行`)
  return parts.join('  ')
})

function lineCount(text) {
  if (!text) return 0
  return text.split('\n').length
}

/* ============================================================
   纯 JS Myers Diff 算法（LCS）
   ============================================================ */
function myersDiff(a, b) {
  const n = a.length, m = b.length
  const max = n + m
  const v = new Array(2 * max + 2).fill(0)
  const trace = []

  for (let d = 0; d <= max; d++) {
    trace.push([...v])
    for (let k = -d; k <= d; k += 2) {
      const idx = k + max
      let x
      if (k === -d || (k !== d && v[idx - 1] < v[idx + 1])) {
        x = v[idx + 1]
      } else {
        x = v[idx - 1] + 1
      }
      let y = x - k
      while (x < n && y < m && a[x] === b[y]) {
        x++
        y++
      }
      v[idx] = x
      if (x >= n && y >= m) {
        return backtrack(trace, a, b, max, d)
      }
    }
  }
  return backtrack(trace, a, b, max, max)
}

function backtrack(trace, a, b, max, d) {
  const result = []
  let x = a.length, y = b.length

  for (let dd = d; dd > 0; dd--) {
    const v = trace[dd]
    const k = x - y
    const idx = k + max
    let prevK
    if (k === -dd || (k !== dd && v[idx - 1] < v[idx + 1])) {
      prevK = k + 1
    } else {
      prevK = k - 1
    }
    const prevX = v[prevK + max]
    const prevY = prevX - prevK

    while (x > prevX && y > prevY) {
      x--
      y--
      result.unshift({ type: 'equal', val: a[x] })
    }
    if (dd > 0) {
      if (x === prevX) {
        result.unshift({ type: 'insert', val: b[y - 1] })
        y--
      } else {
        result.unshift({ type: 'delete', val: a[x - 1] })
        x--
      }
    }
  }

  while (x > 0 && y > 0) {
    x--
    y--
    result.unshift({ type: 'equal', val: a[x] })
  }
  while (x > 0) {
    x--
    result.unshift({ type: 'delete', val: a[x] })
  }
  while (y > 0) {
    y--
    result.unshift({ type: 'insert', val: b[y] })
  }
  return result
}

/* ============================================================
   Diff 执行
   ============================================================ */
function normalize(text) {
  let t = text
  if (ignoreWhitespace.value) t = t.split('\n').map(l => l.trim()).join('\n')
  if (ignoreCase.value) t = t.toLowerCase()
  return t
}

function tokenize(text, mode) {
  if (mode === 'line') return text.split('\n')
  if (mode === 'word') return text.match(/\S+|\s+/g) || []
  return text.split('')
}

function escapeHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;')
}

function highlightIntraLine(orig, mod) {
  const ops = myersDiff(orig.split(''), mod.split(''))
  let leftHtml = '', rightHtml = ''
  let inDelBlock = false, inInsBlock = false

  const closeBlocks = () => {
    if (inDelBlock) {
      leftHtml += '</mark>'
      inDelBlock = false
    }
    if (inInsBlock) {
      rightHtml += '</mark>'
      inInsBlock = false
    }
  }

  for (const op of ops) {
    const esc = escapeHtml(op.val)
    if (op.type === 'equal') {
      closeBlocks()
      leftHtml += esc
      rightHtml += esc
    } else if (op.type === 'delete') {
      if (!inDelBlock) {
        leftHtml += '<mark class="hl-del">'
        inDelBlock = true
      }
      leftHtml += esc
    } else {
      if (!inInsBlock) {
        rightHtml += '<mark class="hl-ins">'
        inInsBlock = true
      }
      rightHtml += esc
    }
  }
  closeBlocks()
  return { leftHtml, rightHtml }
}

function runDiff() {
  if (!originalText.value && !modifiedText.value) {
    diffLines.value = []
    splitRows.value = []
    return
  }

  const origNorm = normalize(originalText.value)
  const modNorm = normalize(modifiedText.value)

  const origTokens = tokenize(origNorm, diffMode.value)
  const modTokens = tokenize(modNorm, diffMode.value)

  const ops = myersDiff(origTokens, modTokens)

  const lines = []
  let loNo = 0, roNo = 0

  if (diffMode.value === 'line') {
    const rawLines = []
    for (const op of ops) {
      rawLines.push({ type: op.type === 'equal' ? 'equal' : op.type === 'delete' ? 'removed' : 'added', val: op.val })
    }

    let i = 0
    while (i < rawLines.length) {
      const cur = rawLines[i]
      if (cur.type === 'removed' && i + 1 < rawLines.length && rawLines[i + 1].type === 'added') {
        const orig = cur.val, mod = rawLines[i + 1].val
        loNo++
        roNo++
        const { leftHtml, rightHtml } = highlightIntraLine(orig, mod)
        lines.push({ type: 'changed-remove', noLeft: loNo, noRight: '', html: leftHtml, raw: orig })
        lines.push({ type: 'changed-add', noLeft: '', noRight: roNo, html: rightHtml, raw: mod })
        i += 2
      } else {
        if (cur.type === 'equal') {
          loNo++
          roNo++
          lines.push({ type: 'equal', noLeft: loNo, noRight: roNo, html: escapeHtml(cur.val), raw: cur.val })
        } else if (cur.type === 'removed') {
          loNo++
          lines.push({ type: 'removed', noLeft: loNo, noRight: '', html: escapeHtml(cur.val), raw: cur.val })
        } else {
          roNo++
          lines.push({ type: 'added', noLeft: '', noRight: roNo, html: escapeHtml(cur.val), raw: cur.val })
        }
        i++
      }
    }
  } else {
    let leftHtml = '', rightHtml = ''
    for (const op of ops) {
      const esc = escapeHtml(op.val)
      if (op.type === 'equal') {
        leftHtml += esc
        rightHtml += esc
      } else if (op.type === 'delete') leftHtml += `<mark class="hl-del">${esc}</mark>`
      else rightHtml += `<mark class="hl-ins">${esc}</mark>`
    }
    if (leftHtml !== rightHtml) {
      lines.push({ type: 'changed-remove', noLeft: 1, noRight: '', html: leftHtml, raw: origNorm })
      lines.push({ type: 'changed-add', noLeft: '', noRight: 1, html: rightHtml, raw: modNorm })
    } else {
      lines.push({ type: 'equal', noLeft: 1, noRight: 1, html: leftHtml, raw: origNorm })
    }
  }

  diffLines.value = lines
  buildSplitRows(lines)
}

function buildSplitRows(lines) {
  const rows = []
  let i = 0
  while (i < lines.length) {
    const l = lines[i]
    if (l.type === 'changed-remove' && i + 1 < lines.length && lines[i + 1].type === 'changed-add') {
      rows.push({
        leftClass: 'changed-remove',
        rightClass: 'changed-add',
        leftNo: l.noLeft,
        rightNo: lines[i + 1].noRight,
        leftHtml: l.html,
        rightHtml: lines[i + 1].html
      })
      i += 2
    } else if (l.type === 'removed') {
      rows.push({
        leftClass: 'removed',
        rightClass: 'empty',
        leftNo: l.noLeft,
        rightNo: '',
        leftHtml: l.html,
        rightHtml: ''
      })
      i++
    } else if (l.type === 'added') {
      rows.push({
        leftClass: 'empty',
        rightClass: 'added',
        leftNo: '',
        rightNo: l.noRight,
        leftHtml: '',
        rightHtml: l.html
      })
      i++
    } else {
      rows.push({
        leftClass: 'equal',
        rightClass: 'equal',
        leftNo: l.noLeft,
        rightNo: l.noRight,
        leftHtml: l.html,
        rightHtml: l.html
      })
      i++
    }
  }
  splitRows.value = rows
}

function syncScroll(e) {
  const other = e.target === leftScrollRef.value ? rightScrollRef.value : leftScrollRef.value
  if (other) other.scrollTop = e.target.scrollTop
}

watch(leftScrollRef, el => {
  if (el) el.addEventListener('scroll', syncScroll)
})
watch(rightScrollRef, el => {
  if (el) el.addEventListener('scroll', syncScroll)
})

let diffTimer = null

function debouncedDiff() {
  clearTimeout(diffTimer)
  diffTimer = setTimeout(runDiff, 300)
}

/* ============================================================
   辅助
   ============================================================ */
function lineSign(type) {
  if (type === 'added' || type === 'changed-add') return '+'
  if (type === 'removed' || type === 'changed-remove') return '−'
  return ' '
}

async function pasteText(target) {
  try {
    const text = await navigator.clipboard.readText()
    if (target === 'original') originalText.value = text
    else modifiedText.value = text
    runDiff()
  } catch {
  }
}

function swapTexts() {
  const tmp = originalText.value
  originalText.value = modifiedText.value
  modifiedText.value = tmp
  runDiff()
}

function clearAll() {
  originalText.value = ''
  modifiedText.value = ''
  diffLines.value = []
  splitRows.value = []
}

async function copyResult() {
  const text = diffLines.value.map(l => lineSign(l.type) + ' ' + l.raw).join('\n')
  await navigator.clipboard.writeText(text).catch(() => {
  })
  copied.value = true
  setTimeout(() => {
    copied.value = false
  }, 2000)
}

function loadSample() {
  originalText.value = `function greet(name) {
  console.log("Hello, " + name);
  return name;
}

const user = "Alice";
greet(user);

// TODO: add error handling`

  modifiedText.value = `function greet(name, greeting = "Hello") {
  if (!name) throw new Error("Name is required");
  console.log(\`\${greeting}, \${name}!\`);
  return name.trim();
}

const user = "Bob";
greet(user, "Hi");`

  runDiff()
}
</script>

<style lang="scss" scoped>
/* ── 整体容器 ── */
.diff-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 20px 24px 16px;
  box-sizing: border-box;
  gap: 12px;
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

  &.status-diff {
    background: #fef3c7;
    border-color: #fcd34d;
    color: #d97706;
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

  .status-diff & {
    background: #d97706;
    box-shadow: 0 0 0 2px #fcd34d;
  }
}

/* ── Sub Toolbar ── */
.sub-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  flex-shrink: 0;
  padding: 8px 14px;
  background: var(--el-bg-color, #fff);
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seg-ctrl {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;

  button {
    display: flex;
    align-items: center;
    gap: 5px;
    padding: 5px 11px;
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

.opt-toggle {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  user-select: none;

  input[type="checkbox"] {
    accent-color: var(--el-color-primary, #409eff);
    cursor: pointer;
  }
}

.diff-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  font-family: monospace;
}

.stat {
  padding: 2px 8px;
  border-radius: 5px;

  &.added {
    background: #dcfce7;
    color: #16a34a;
  }

  &.removed {
    background: #fee2e2;
    color: #dc2626;
  }

  &.changed {
    background: #fef3c7;
    color: #d97706;
  }
}

/* ── Icon Button ── */
.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 11px;
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

  &.accent:hover {
    background: var(--el-color-primary-light-8, #d9ecff);
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

/* ── Main Layout ── */
.main-layout {
  flex: 1;
  min-height: 0;
  display: flex;
  gap: 0;
  overflow: hidden;
  flex-direction: column;
}

/* ── 分栏视图列 ── */
.col-input {
  flex: none;
  height: 220px;
}

.col-divider {
  width: 12px;
  flex-shrink: 0;
  display: flex;
  align-items: stretch;
  justify-content: center;

  .divider-line {
    width: 1px;
    background: var(--el-border-color-lighter, #ebeef5);
  }
}

.col-result-split {
  width: 100%;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  position: static;
  flex: 1;
  min-height: 0;
  margin-top: 12px;
}

.col-inputs-row {
  display: flex;
  gap: 12px;
  height: 200px;
  flex-shrink: 0;

  .card {
    flex: 1;
  }
}

.col-unified-result {
  position: static;
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  margin-top: 12px;
}

/* ── Card ── */
.card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: 100%;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
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
  gap: 5px;
}

.card-foot {
  padding: 5px 12px;
  border-top: 1px solid var(--el-border-color-lighter, #ebeef5);
  flex-shrink: 0;
}

.foot-hint {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
}

/* ── Input Label Pills ── */
.input-label {
  font-size: 11px;
  font-weight: 700;
  padding: 2px 9px;
  border-radius: 5px;
  letter-spacing: .06em;

  &.original {
    background: #fee2e2;
    color: #dc2626;
  }

  &.modified {
    background: #dcfce7;
    color: #16a34a;
  }
}

/* ── Textarea ── */
.diff-textarea {
  flex: 1;
  padding: 10px 12px;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;
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

/* ── Identical Badge ── */
.identical-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #16a34a;
  padding: 2px 8px;
  background: #dcfce7;
  border-radius: 5px;
}

/* ── Unified ── */
.diff-unified-result {
  flex: 1;
  overflow-y: auto;
  overflow-x: auto;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;

  &::-webkit-scrollbar {
    width: 5px;
    height: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

/* ── Split ── */
.diff-split-result {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.split-col {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  overflow-x: auto;
  font-family: 'Menlo', 'Monaco', 'Cascadia Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.7;

  &::-webkit-scrollbar {
    width: 5px;
    height: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }

  &.original-col {
    border-right: 1px solid var(--el-border-color-lighter, #ebeef5);
  }
}

/* ── Diff Row ── */
.diff-row {
  display: flex;
  align-items: baseline;
  min-width: max-content;
  width: 100%;
  padding: 0 0;

  &.equal {
    background: transparent;
  }

  &.added {
    background: #f0fdf4;
  }

  &.removed {
    background: #fff5f5;
  }

  &.changed-add {
    background: #f0fdf4;
  }

  &.changed-remove {
    background: #fff5f5;
  }

  &.empty {
    background: var(--el-fill-color-lighter, #fafafa);
  }
}

.line-sign {
  width: 20px;
  flex-shrink: 0;
  text-align: center;
  font-weight: 700;
  font-size: 12px;
  padding: 1px 0;
  user-select: none;

  .added &, .changed-add & {
    color: #16a34a;
  }

  .removed &, .changed-remove & {
    color: #dc2626;
  }

  .equal & {
    color: transparent;
  }
}

.line-no, .line-no-left, .line-no-right {
  min-width: 36px;
  padding: 1px 8px 1px 4px;
  text-align: right;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-size: 11px;
  flex-shrink: 0;
  user-select: none;
  border-right: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-fill-color-lighter, #fafafa);

  .added &, .changed-add & {
    background: #dcfce7;
    color: #16a34a;
  }

  .removed &, .changed-remove & {
    background: #fee2e2;
    color: #dc2626;
  }
}

.line-content {
  flex: 1;
  padding: 1px 12px;
  white-space: pre;
  word-break: normal;

  .equal & {
    color: var(--el-text-color-primary, #303133);
  }

  .added & {
    color: #166534;
  }

  .removed & {
    color: #991b1b;
  }

  .changed-add & {
    color: #166534;
  }

  .changed-remove & {
    color: #991b1b;
  }
}

/* ── 行内高亮 ── */
:deep(mark.hl-del) {
  background: #fca5a5;
  color: #7f1d1d;
  border-radius: 2px;
  padding: 0 1px;
}

:deep(mark.hl-ins) {
  background: #86efac;
  color: #14532d;
  border-radius: 2px;
  padding: 0 1px;
}

/* ── Empty State ── */
.col-empty-split {
  flex: 1;
  display: flex;
  flex-direction: column;
  margin-top: 12px;
}

.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-style: dashed !important;
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

.result-card {
  height: 100%;
}

/* ============================================================
   核心新增：纯 CSS 精美气泡提示 (Tooltip) 核心样式
   ============================================================ */
/* 只要带有 data-tooltip 属性的元素，悬停时均会触发气泡 */
[data-tooltip] {
  position: relative; /* 建立气泡定位的绝对坐标基准 */
}

/* 气泡提示框主体 */
[data-tooltip]::after {
  content: attr(data-tooltip); /* 高级特性：动态提取并填充 HTML 属性中的字符串内容 */
  position: absolute;
  bottom: 135%; /* 稳定撑起在目标图标上方 */
  left: 50%;
  transform: translateX(-50%) scale(0.92); /* 默认居中，且略微缩小，用于做出微动的动画感 */
  background: rgba(30, 41, 59, 0.95); /* 现代科技感深色背景 (Slate-800 偏向半透明) */
  color: #ffffff;
  padding: 6px 10px;
  font-size: 11px;
  font-family: system-ui, -apple-system, sans-serif;
  font-weight: 400;
  line-height: 1.4;
  border-radius: 6px;
  white-space: nowrap; /* 强行禁止气泡内的文本内容发生意外换行 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15), 0 2px 4px rgba(0, 0, 0, 0.1);
  pointer-events: none; /* 关键：防止鼠标意外移入气泡引起闪烁抖动 */
  opacity: 0; /* 默认全透明隐藏 */
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1); /* 优雅平滑的贝塞尔曲线动画 */
  z-index: 999;
}

/* 小三角气泡箭头 */
[data-tooltip]::before {
  content: "";
  position: absolute;
  bottom: 110%;
  left: 50%;
  transform: translateX(-50%) scale(0.92);
  border-width: 5px 5px 0; /* 经典的 CSS 三角形写法 */
  border-style: solid;
  border-color: rgba(30, 41, 59, 0.95) transparent transparent; /* 三角形颜色与主体契合 */
  pointer-events: none;
  opacity: 0;
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
  z-index: 999;
}

/* 当鼠标移入时，同步改变气泡和箭头的不透明度，并将其过渡恢复到原始比例 scale(1) */
[data-tooltip]:hover::after,
[data-tooltip]:hover::before {
  opacity: 1;
  transform: translateX(-50%) scale(1);
}
</style>