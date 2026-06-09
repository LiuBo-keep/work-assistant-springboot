<template>
  <div class="panel-wrap">
    <div class="two-col">

      <!-- 输入 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">输入</span>
        </div>
        <div class="card-body gap10">
          <div class="field-row">
            <label class="field-label">数值</label>
            <input
              class="field-input mono flex1"
              v-model="radixInput"
              :placeholder="radixPlaceholder"
              @input="convertRadix"
            />
          </div>
          <div class="field-row">
            <label class="field-label">输入进制</label>
            <div class="seg-ctrl">
              <button v-for="r in radixOptions" :key="r.val" :class="{ active: inputRadix === r.val }"
                      @click="inputRadix = r.val; convertRadix()">{{ r.label }}
              </button>
            </div>
          </div>
          <div v-if="radixError" class="result-err">{{ radixError }}</div>

          <!-- 快捷输入 -->
          <div class="tpl-list mt4">
            <button class="tpl-btn" v-for="s in radixSamples" :key="s.val"
                    @click="radixInput = s.val; inputRadix = s.base; convertRadix()">{{ s.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- 输出 -->
      <div class="card">
        <div class="card-header">
          <span class="card-label">转换结果</span>
        </div>
        <div class="card-body">
          <div class="radix-results" v-if="radixResults.length">
            <div class="rx-item" v-for="r in radixResults" :key="r.base">
              <div class="rx-header">
                <span class="rx-label">{{ r.label }}</span>
                <span class="rx-base">{{ r.base }}进制</span>
              </div>
              <div class="rx-val-row">
                <span class="rx-val mono">{{ r.val }}</span>
                <button class="copy-mini" @click="copyText(r.val)">
                  <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="9" y="9" width="13" height="13" rx="2" />
                    <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                  </svg>
                </button>
              </div>
              <!-- 二进制分组显示 -->
              <div v-if="r.base === 2 && r.val" class="rx-bits">
                    <span
                      v-for="(group, gi) in r.groups" :key="gi"
                      class="bit-group"
                      :class="{ 'bit-one': group.includes('1') }"
                    >{{ group }}</span>
              </div>
            </div>
          </div>
          <div v-else class="empty-hint-sm">输入数值后显示转换结果</div>

          <!-- 按位信息 -->
          <div v-if="radixBitInfo" class="bit-info mt12">
            <div class="field-label mb6">位运算信息</div>
            <div class="bit-info-grid">
              <div class="bi-item" v-for="b in radixBitInfo" :key="b.label">
                <span class="bi-label">{{ b.label }}</span>
                <span class="bi-val mono">{{ b.val }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const radixInput = ref('')
const inputRadix = ref(10)
const radixError = ref('')
const radixResults = ref([])
const radixBitInfo = ref(null)

const radixOptions = [
  { label: '二进制', val: 2 },
  { label: '八进制', val: 8 },
  { label: '十进制', val: 10 },
  { label: '十六进制', val: 16 }
]

const radixSamples = [
  { label: '255', val: '255', base: 10 },
  { label: 'FF', val: 'FF', base: 16 },
  { label: '1024', val: '1024', base: 10 },
  { label: '0b1010', val: '1010', base: 2 }
]

const radixPlaceholder = computed(() => {
  const m = { 2: '01001101...', 8: '0-7数字', 10: '任意整数', 16: '0-9 A-F' }
  return m[inputRadix.value] || ''
})

function convertRadix() {
  radixError.value = ''
  radixResults.value = []
  radixBitInfo.value = null
  const raw = radixInput.value.trim().replace(/^0[xXbBoO]/, '')
  if (!raw) return

  const num = parseInt(raw, inputRadix.value)
  if (isNaN(num)) {
    radixError.value = `"${raw}" 不是合法的 ${inputRadix.value} 进制数`
    return
  }

  const bases = [
    { base: 2, label: '二进制', prefix: '0b' },
    { base: 8, label: '八进制', prefix: '0o' },
    { base: 10, label: '十进制', prefix: '' },
    { base: 16, label: '十六进制', prefix: '0x' }
  ]

  radixResults.value = bases.map(b => {
    const val = Math.abs(num).toString(b.base).toUpperCase()
    const signed = num < 0 ? '-' + val : val
// 二进制按4位分组
    const groups = b.base === 2
      ? val.padStart(Math.ceil(val.length / 4) * 4, '0').match(/.{1,4}/g) || []
      : []
    return { ...b, val: b.base === 16 ? signed.toLowerCase() : signed, groups }
  })

// 位信息
  const abs = Math.abs(num)
  radixBitInfo.value = [
    { label: '位长度', val: abs.toString(2).length + ' bits' },
    { label: '最高位', val: Math.floor(Math.log2(abs || 1)) + '' },
    { label: '是否2的幂', val: abs > 0 && (abs & (abs - 1)) === 0 ? '是' : '否' },
    { label: '奇偶', val: num % 2 === 0 ? '偶数' : '奇数' },
    { label: '按位取反 ~', val: String(~num) }
  ]
}

</script>

<style lang="scss">
@import '../shared.scss';
</style>