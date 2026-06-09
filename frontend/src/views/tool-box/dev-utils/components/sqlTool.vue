<template>
  <div class="panel-wrap">
    <div class="two-col">
      <div class="card">
        <div class="card-header">
          <span class="card-label">SQL 输入</span>
          <div class="card-actions">
            <button v-for="p in sqlPresets" :key="p.label" class="icon-btn sm" @click="sqlInput = p.sql; runSql()">
              {{ p.label }}
            </button>
          </div>
        </div>
        <div class="card-body" style="gap:10px">
          <div class="field-row">
            <label class="field-label">操作</label>
            <div class="seg-ctrl">
              <button v-for="m in sqlModes" :key="m.val" :class="{ active: sqlMode === m.val }"
                      @click="sqlMode = m.val; runSql()">{{ m.label }}
              </button>
            </div>
          </div>
          <div class="field-row" v-if="sqlMode === 'format'">
            <label class="field-label">风格</label>
            <div class="seg-ctrl">
              <button v-for="d in sqlDialects" :key="d" :class="{ active: sqlDialect === d }"
                      @click="sqlDialect = d; runSql()">{{ d }}
              </button>
            </div>
          </div>
          <textarea class="field-input mono" style="width:100%;resize:vertical;min-height:180px;box-sizing:border-box"
                    v-model="sqlInput" placeholder="输入 SQL 语句..." spellcheck="false" @input="debounceSql" />
        </div>
      </div>
      <div class="card">
        <div class="card-header">
          <span
            class="card-label">{{ sqlMode === 'format' ? '格式化结果' : sqlMode === 'compress' ? '压缩结果' : sqlMode === 'ddl2entity' ? 'Java 实体类' : 'JSON→INSERT'
            }}</span>
          <button class="icon-btn sm accent" @click="copyText(sqlOutput)" :disabled="!sqlOutput">复制</button>
        </div>
        <div class="card-body" style="padding:0;flex:1">
          <pre v-if="sqlOutput" class="code-pre" v-html="sqlOutputHtml" />
          <div v-else class="empty-hint-sm" style="padding:24px">选择操作后显示结果</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const sqlInput = ref('')
const sqlOutput = ref('')
const sqlMode = ref('format')
const sqlDialect = ref('MySQL')
const sqlDialects = ['MySQL', 'PostgreSQL', 'Standard']
const sqlModes = [
  { val: 'format', label: '格式化' },
  { val: 'compress', label: '压缩' },
  { val: 'ddl2entity', label: 'DDL→Java' },
  { val: 'json2insert', label: 'JSON→INSERT' }
]
const sqlPresets = [
  {
    label: 'SELECT 示例',
    sql: 'SELECT u.id,u.name,u.email,o.order_no,o.amount FROM users u LEFT JOIN orders o ON u.id=o.user_id WHERE u.status=1 AND o.amount>100 ORDER BY o.created_at DESC LIMIT 20'
  },
  {
    label: 'DDL 示例',
    sql: 'CREATE TABLE user (\n  id bigint NOT NULL AUTO_INCREMENT,\n  username varchar(64) NOT NULL COMMENT \'用户名\',\n  email varchar(128) DEFAULT NULL COMMENT \'邮箱\',\n  status tinyint NOT NULL DEFAULT 1 COMMENT \'状态\',\n  created_at datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n  PRIMARY KEY (id)\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT=\'用户表\';'
  }
]

const sqlOutputHtml = computed(() => {
  if (!sqlOutput.value) return ''
  return sqlOutput.value
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\b(SELECT|FROM|WHERE|JOIN|LEFT|RIGHT|INNER|ON|AND|OR|ORDER BY|GROUP BY|HAVING|LIMIT|INSERT|INTO|VALUES|UPDATE|SET|DELETE|CREATE|TABLE|ALTER|DROP|INDEX|PRIMARY|KEY|NOT|NULL|DEFAULT|AUTO_INCREMENT|ENGINE|CHARSET|COMMENT|AS|DISTINCT|COUNT|SUM|AVG|MAX|MIN|CASE|WHEN|THEN|ELSE|END|IN|EXISTS|BETWEEN|LIKE|IS|WITH|UNION|ALL)\b/gi,
      w => `<span class="sql-kw">${w.toUpperCase()}</span>`)
    .replace(/('.*?')/g, '<span class="sql-str">$1</span>')
    .replace(/(--[^\n]*)/g, '<span class="sql-comment">$1</span>')
    .replace(/(\`[^\`]+\`)/g, '<span class="sql-id">$1</span>')
})

let sqlTimer = null

function debounceSql() {
  clearTimeout(sqlTimer)
  sqlTimer = setTimeout(runSql, 400)
}

function runSql() {
  if (!sqlInput.value.trim()) {
    sqlOutput.value = ''
    return
  }
  if (sqlMode.value === 'format') sqlOutput.value = formatSQL(sqlInput.value)
  if (sqlMode.value === 'compress') sqlOutput.value = compressSQL(sqlInput.value)
  if (sqlMode.value === 'ddl2entity') sqlOutput.value = ddlToJava(sqlInput.value)
  if (sqlMode.value === 'json2insert') sqlOutput.value = jsonToInsert(sqlInput.value)
}

function formatSQL(sql) {
  const keywords = ['SELECT', 'FROM', 'WHERE', 'LEFT JOIN', 'RIGHT JOIN', 'INNER JOIN', 'JOIN', 'ON', 'AND', 'OR',
    'ORDER BY', 'GROUP BY', 'HAVING', 'LIMIT', 'OFFSET', 'INSERT INTO', 'VALUES', 'UPDATE', 'SET',
    'DELETE FROM', 'CREATE TABLE', 'ALTER TABLE', 'DROP TABLE', 'UNION', 'UNION ALL', 'WITH']
  let result = sql.trim()
  result = result.replace(/\s+/g, ' ')
  keywords.forEach(kw => {
    const re = new RegExp(`\\b${kw}\\b`, 'gi')
    result = result.replace(re, '\n' + kw)
  })
// 缩进 SELECT 字段
  result = result.replace(/SELECT\n/i, 'SELECT\n  ')
  result = result.split('\n').map(l => l.trim()).filter(Boolean).join('\n')
// 逗号换行
  result = result.replace(/,\s*(?=[^']*(?:'[^']*'[^']*)*$)/g, ',\n  ')
  return result.trim()
}

function compressSQL(sql) {
  return sql.replace(/\s+/g, ' ').replace(/\s*,\s*/g, ',').trim()
}

function ddlToJava(ddl) {
  const tableMatch = ddl.match(/CREATE\s+TABLE\s+[\`"]?(\w+)[\`"]?/i)
  const tableName = tableMatch ? tableMatch[1] : 'Entity'
  const className = tableName.split('_').map(p => p.charAt(0).toUpperCase() + p.slice(1).toLowerCase()).join('')

  const colRe = /[\`"]?(\w+)[\`"]?\s+(\w+)(?:\([^)]*\))?[^,]*?(?:COMMENT\s+'([^']*)')?[,\n]/gi
  const fields = []
  let m
  while ((m = colRe.exec(ddl)) !== null) {
    const col = m[1].toLowerCase()
    if (['primary', 'key', 'unique', 'index', 'engine', 'charset'].includes(col)) continue
    const sqlType = m[2].toLowerCase()
    const comment = m[3] || ''
    const javaType = {
      bigint: 'Long', int: 'Integer', tinyint: 'Integer', smallint: 'Integer',
      varchar: 'String', char: 'String', text: 'String', longtext: 'String', mediumtext: 'String',
      datetime: 'LocalDateTime', timestamp: 'LocalDateTime', date: 'LocalDate',
      decimal: 'BigDecimal', double: 'Double', float: 'Float', boolean: 'Boolean', bit: 'Boolean'
    }[sqlType] || 'Object'
    const fieldName = col.replace(/_([a-z])/g, (_, c) => c.toUpperCase())
    fields.push({ javaType, fieldName, col, comment })
  }

  const imports = new Set(['lombok.Data'])
  if (fields.some(f => f.javaType === 'LocalDateTime')) imports.add('java.time.LocalDateTime')
  if (fields.some(f => f.javaType === 'LocalDate')) imports.add('java.time.LocalDate')
  if (fields.some(f => f.javaType === 'BigDecimal')) imports.add('java.math.BigDecimal')

  const lines = [
    ...[...imports].map(i => `import ${i};`),
    '',
    `/**`,
    ` * ${tableName} 表实体`,
    ` */`,
    '@Data',
    `public class ${className} {`,
    ...fields.map(f => [
      f.comment ? `    /** ${f.comment} */` : null,
      `    private ${f.javaType} ${f.fieldName};`
    ].filter(Boolean).join('\n')),
    '}'
  ]
  return lines.join('\n')
}

function jsonToInsert(input) {
  try {
    let data = JSON.parse(input.trim())
    if (!Array.isArray(data)) data = [data]
    if (!data.length) return '-- 空数组'
    const table = 'table_name'
    const keys = Object.keys(data[0])
    const cols = keys.map(k => `\`${k}\``).join(', ')
    const rows = data.map(row => {
      const vals = keys.map(k => {
        const v = row[k]
        if (v === null || v === undefined) return 'NULL'
        if (typeof v === 'boolean') return v ? '1' : '0'
        if (typeof v === 'number') return String(v)
        return `'${String(v).replace(/'/g, '\'')}'`
      }).join(', ')
      return `  (${vals})`
    }).join(',\n')
    return `INSERT INTO \`${table}\` (${cols})\nVALUES\n${rows};`
  } catch (e) {
    return `-- JSON 解析失败: ${e.message}`
  }
}

</script>

<style lang="scss">
@import '../shared.scss';
</style>