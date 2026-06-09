<template>
  <div class="panel-wrap">
    <div class="card">
      <div class="card-header">
        <span class="card-label">Cron 表达式解析</span>
        <div class="card-actions">
          <button
            v-for="p in cronPresets" :key="p.expr"
            class="icon-btn sm"
            @click="cronExpr = p.expr; parseCron()"
          >{{ p.label }}
          </button>
        </div>
      </div>
      <div class="card-body">
        <div class="field-row">
          <label class="field-label">表达式</label>
          <input
            class="field-input mono flex1"
            v-model="cronExpr"
            placeholder="0 9 * * 1-5"
            @input="parseCron"
          />
        </div>

        <!-- 字段说明 -->
        <div v-if="!cronError" class="cron-fields">
          <div class="cron-field-item" v-for="f in cronFields" :key="f.name">
            <div class="cf-val mono" :class="f.valid ? 'cf-valid' : 'cf-invalid'">{{ f.raw }}</div>
            <div class="cf-name">{{ f.name }}</div>
            <div class="cf-desc">{{ f.desc }}</div>
          </div>
        </div>
        <div v-if="cronError" class="result-err mt8">{{ cronError }}</div>

        <!-- 人话描述 -->
        <div v-if="cronHuman && !cronError" class="cron-human">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10" />
            <polyline points="12 6 12 12 16 14" />
          </svg>
          {{ cronHuman }}
        </div>

        <!-- 下次执行时间 -->
        <div v-if="cronNextTimes.length && !cronError" class="cron-next">
          <div class="next-title">最近 5 次执行时间</div>
          <div class="next-list">
            <div class="next-item mono" v-for="(t,i) in cronNextTimes" :key="i">
              <span class="next-idx">{{ i + 1 }}</span>{{ t }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const cronExpr = ref('0 9 * * 1-5')
const cronError = ref('')
const cronHuman = ref('')
const cronFields = ref([])
const cronNextTimes = ref([])

const cronPresets = [
  { label: '每分钟', expr: '* * * * *' },
  { label: '每小时', expr: '0 * * * *' },
  { label: '每天9点', expr: '0 9 * * *' },
  { label: '工作日9点', expr: '0 9 * * 1-5' },
  { label: '每周日', expr: '0 0 * * 0' },
  { label: '每月1号', expr: '0 0 1 * *' }
]

const CRON_FIELDS_DEF = [
  { name: '分钟', min: 0, max: 59 },
  { name: '小时', min: 0, max: 23 },
  { name: '日', min: 1, max: 31 },
  { name: '月', min: 1, max: 12 },
  { name: '星期', min: 0, max: 7 }
]

function parseCronField(raw, min, max) {
  if (raw === '*') return { valid: true, desc: '任意值' }
  if (/^\d+$/.test(raw)) {
    const v = +raw
    if (v < min || v > max) return { valid: false, desc: `超出范围 ${min}-${max}` }
    return { valid: true, desc: `固定值 ${v}` }
  }
  if (/^\*\/\d+$/.test(raw)) {
    const step = +raw.split('/')[1]
    return { valid: step > 0, desc: `每 ${step} 个单位` }
  }
  if (/^\d+-\d+$/.test(raw)) {
    const [a, b] = raw.split('-').map(Number)
    if (a > b || a < min || b > max) return { valid: false, desc: '范围无效' }
    return { valid: true, desc: `${a} 到 ${b}` }
  }
  if (/^\d+-\d+\/\d+$/.test(raw)) {
    return { valid: true, desc: '范围步进' }
  }
  if (raw.includes(',')) {
    const vals = raw.split(',').map(Number)
    const ok = vals.every(v => !isNaN(v) && v >= min && v <= max)
    return { valid: ok, desc: `枚举: ${vals.join(', ')}` }
  }
  return { valid: false, desc: '格式不识别' }
}

function humanCron(parts) {
  const [min, hour, day, month, week] = parts
  const lines = []

  if (week !== '*' && week !== '?') {
    const weekMap = {
      '0': '周日',
      '1': '周一',
      '2': '周二',
      '3': '周三',
      '4': '周四',
      '5': '周五',
      '6': '周六',
      '7': '周日'
    }
    if (/^\d+$/.test(week)) lines.push(`每${weekMap[week] || `周${week}`}`)
    else if (/^\d+-\d+$/.test(week)) {
      const [a, b] = week.split('-')
      lines.push(`每${weekMap[a]}到${weekMap[b]}`)
    } else lines.push('指定星期')
  } else if (day !== '*') {
    lines.push(month !== '*' ? `每年${month}月${day}日` : `每月${day}日`)
  } else {
    lines.push('每天')
  }

  if (hour === '*' && min === '*') lines.push('每分钟执行')
  else if (min === '*') lines.push(`${hour}时每分钟执行`)
  else if (hour === '*') lines.push(`每小时第${min}分执行`)
  else if (/^\*\/\d+$/.test(hour)) lines.push(`每隔${hour.split('/')[1]}小时的第${min}分执行`)
  else if (/^\*\/\d+$/.test(min)) lines.push(`${hour}时起每隔${min.split('/')[1]}分执行`)
  else lines.push(`${hour}:${min.padStart?.(2, '0') ?? min} 执行`)

  return lines.join(' ')
}

// 计算下次执行时间（纯 JS，不依赖库）
function calcNextTimes(parts, count = 5) {
  const [minP, hourP, dayP, monthP, weekP] = parts
  const results = []
  let d = new Date()
  d.setSeconds(0, 0)
  d.setMinutes(d.getMinutes() + 1)

  const matchField = (val, pattern, base = 0) => {
    if (pattern === '*' || pattern === '?') return true
    if (/^\d+$/.test(pattern)) return val === +pattern
    if (/^\*\/\d+$/.test(pattern)) {
      const s = +pattern.split('/')[1]
      return (val - base) % s === 0
    }
    if (/^\d+-\d+$/.test(pattern)) {
      const [a, b] = pattern.split('-').map(Number)
      return val >= a && val <= b
    }
    if (/^\d+-\d+\/\d+$/.test(pattern)) {
      const [r, s] = pattern.split('/')
      const [a, b] = r.split('-').map(Number)
      return val >= a && val <= b && (val - a) % +s === 0
    }
    if (pattern.includes(',')) return pattern.split(',').map(Number).includes(val)
    return false
  }

  let safety = 0
  while (results.length < count && safety++ < 100000) {
    const mn = d.getMinutes(), hr = d.getHours()
    const dy = d.getDate(), mo = d.getMonth() + 1, wd = d.getDay()

    if (
      matchField(mo, monthP, 1) &&
      (dayP === '*' || matchField(dy, dayP, 1)) &&
      (weekP === '*' || matchField(wd, weekP, 0)) &&
      matchField(hr, hourP, 0) &&
      matchField(mn, minP, 0)
    ) {
      results.push(d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }))
      d = new Date(d.getTime() + 60000)
    } else {
      d = new Date(d.getTime() + 60000)
    }
  }
  return results
}

function parseCron() {
  cronError.value = ''
  cronHuman.value = ''
  cronNextTimes.value = []
  const expr = cronExpr.value.trim()
  if (!expr) {
    cronFields.value = []
    return
  }

  const parts = expr.split(/\s+/)
  if (parts.length !== 5) {
    cronError.value = `格式错误：需要 5 个字段（当前 ${parts.length} 个），格式：分 时 日 月 周`
    cronFields.value = []
    return
  }

  cronFields.value = parts.map((raw, i) => {
    const def = CRON_FIELDS_DEF[i]
    const { valid, desc } = parseCronField(raw, def.min, def.max)
    return { raw, name: def.name, desc, valid }
  })

  if (cronFields.value.some(f => !f.valid)) {
    cronError.value = '表达式含有无效字段，请检查高亮项'
    return
  }

  cronHuman.value = humanCron(parts)
  cronNextTimes.value = calcNextTimes(parts)
}

parseCron()

</script>

<style lang="scss">
@import '../shared.scss';
</style>