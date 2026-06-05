<template>
  <div class="panel-wrap">
    <div class="two-col">

      <!-- 左：输入区 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">正则表达式</span>
        </div>
        <div class="card-body gap10">
          <div class="regex-input-row">
            <span class="regex-slash">/</span>
            <input
              class="field-input mono flex1"
              v-model="regexPattern"
              placeholder="输入正则表达式..."
              @input="runRegex"
            />
            <span class="regex-slash">/</span>
            <input class="field-input mono" style="width:70px" v-model="regexFlags" placeholder="gim"
                   @input="runRegex" />
          </div>

          <div v-if="regexError" class="result-err">{{ regexError }}</div>

          <!-- 快捷模板 -->
          <div class="tpl-list">
            <button
              class="tpl-btn"
              v-for="t in regexTemplates" :key="t.label"
              @click="regexPattern = t.pattern; regexFlags = t.flags; runRegex()"
            >{{ t.label }}
            </button>
          </div>

          <div class="field-row" style="flex-direction:column; align-items:flex-start; gap:6px">
            <label class="field-label">测试文本</label>
            <textarea
              class="field-input mono"
              style="width:100%; resize:vertical; min-height:100px; box-sizing:border-box"
              v-model="regexText"
              placeholder="输入测试文本..."
              @input="runRegex"
            />
          </div>
        </div>
      </div>

      <!-- 右：结果区 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">匹配结果</span>
          <span v-if="regexMatches.length" class="match-badge">{{ regexMatches.length }} 个匹配</span>
          <span v-else-if="regexPattern && !regexError && regexText" class="no-match-badge">无匹配</span>
        </div>
        <div class="card-body">
          <!-- 高亮预览 -->
          <div class="field-label mb6">高亮预览</div>
          <div class="regex-preview"
               v-html="regexHighlighted || '<span style=&quot;color:var(--el-text-color-placeholder)&quot;>匹配结果将在此高亮显示...</span>'" />

          <!-- 匹配详情 -->
          <template v-if="regexMatches.length">
            <div class="field-label mt12 mb6">匹配详情</div>
            <div class="match-list">
              <div class="match-item" v-for="(m,i) in regexMatches" :key="i">
                <span class="match-idx">{{ i + 1 }}</span>
                <div class="match-info">
                  <span class="match-val mono">{{ m.value }}</span>
                  <span class="match-pos">位置 {{ m.index }}–{{ m.index + m.value.length }}</span>
                  <div v-if="m.groups && m.groups.length" class="match-groups">
                        <span v-for="(g,gi) in m.groups" :key="gi" class="group-item">
                          组{{ gi + 1 }}: <span class="mono">{{ g ?? 'undefined' }}</span>
                        </span>
                  </div>
                </div>
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

const regexPattern = ref('')
const regexFlags = ref('g')
const regexText = ref('')
const regexError = ref('')
const regexMatches = ref([])
const regexHighlighted = ref('')

const regexTemplates = [
  { label: '邮箱', pattern: '[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}', flags: 'g' },
  { label: 'IPv4', pattern: '\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b', flags: 'g' },
  { label: '手机号', pattern: '1[3-9]\\d{9}', flags: 'g' },
  { label: 'URL', pattern: 'https?:\\/\\/[^\\s]+', flags: 'g' },
  { label: '中文字符', pattern: '[\\u4e00-\\u9fa5]+', flags: 'g' },
  { label: 'HTML标签', pattern: '<[^>]+>', flags: 'g' },
  { label: '日期', pattern: '\\d{4}[-/]\\d{2}[-/]\\d{2}', flags: 'g' },
  { label: '整数', pattern: '-?\\d+', flags: 'g' }
]

function runRegex() {
  regexError.value = ''
  regexMatches.value = []
  regexHighlighted.value = ''
  if (!regexPattern.value || !regexText.value) return

  let re
  try {
    re = new RegExp(regexPattern.value, regexFlags.value.includes('g') ? regexFlags.value : regexFlags.value + 'g')
  } catch (e) {
    regexError.value = '正则语法错误：' + e.message
    return
  }

  const matches = []
  let m
  re.lastIndex = 0
  while ((m = re.exec(regexText.value)) !== null) {
    matches.push({ value: m[0], index: m.index, groups: m.length > 1 ? Array.from(m).slice(1) : [] })
    if (!regexFlags.value.includes('g')) break
  }
  regexMatches.value = matches

// 高亮
  if (matches.length === 0) {
    regexHighlighted.value = escHtml(regexText.value)
    return
  }
  let html = '', last = 0
  for (const match of matches) {
    html += escHtml(regexText.value.slice(last, match.index))
    html += `<mark class="rx-match">${escHtml(match.value)}</mark>`
    last = match.index + match.value.length
  }
  html += escHtml(regexText.value.slice(last))
  regexHighlighted.value = html
}

function escHtml(s) {
  return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

</script>

<style lang="scss">
@import '../shared.scss';
</style>