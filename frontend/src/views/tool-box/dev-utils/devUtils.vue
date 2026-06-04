<template>
  <div class="dev-utils-page">

    <!-- ===== Page Header ===== -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="16 18 22 12 16 6" />
            <polyline points="8 6 2 12 8 18" />
          </svg>
        </div>
        <div>
          <h2 class="header-title">开发者工具集</h2>
          <p class="header-sub">时间戳 · Cron · 正则 · URL · 进制 · SQL · 加密 · 配置生成</p>
        </div>
      </div>
    </div>

    <!-- ===== Tool Tabs ===== -->
    <div class="tool-tabs">
      <button
        v-for="tab in tabs" :key="tab.key"
        class="tool-tab" :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-icon" v-html="tab.icon" />
        {{ tab.label }}
      </button>
    </div>

    <!-- ===== Tool Panels ===== -->
    <div class="tool-body">

      <!-- ══════════════════════════════
           1. 时间戳转换
           ══════════════════════════════ -->
      <div v-show="activeTab === 'timestamp'" class="panel-wrap">
        <div class="two-col">

          <!-- 时间戳 → 时间 -->
          <div class="card">
            <div class="card-header">
              <span class="card-label">时间戳 → 日期时间</span>
              <button class="icon-btn sm" @click="tsInput = String(Date.now())">当前时间</button>
            </div>
            <div class="card-body">
              <div class="field-row">
                <label class="field-label">时间戳</label>
                <div class="input-with-seg">
                  <input class="field-input mono flex1" v-model="tsInput" placeholder="1716000000000"
                         @input="tsToDate" />
                  <div class="seg-ctrl">
                    <button :class="{ active: tsUnit === 'ms' }" @click="tsUnit='ms'; tsToDate()">毫秒</button>
                    <button :class="{ active: tsUnit === 's' }" @click="tsUnit='s';  tsToDate()">秒</button>
                  </div>
                </div>
              </div>
              <div v-if="tsResult.error" class="result-err">{{ tsResult.error }}</div>
              <template v-else-if="tsResult.utc">
                <div class="result-grid">
                  <div class="rg-item" v-for="r in tsResult.rows" :key="r.label">
                    <span class="rg-label">{{ r.label }}</span>
                    <div class="rg-val-wrap">
                      <span class="rg-val mono">{{ r.val }}</span>
                      <button class="copy-mini" @click="copyText(r.val)" title="复制">
                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2">
                          <rect x="9" y="9" width="13" height="13" rx="2" />
                          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>

          <!-- 时间 → 时间戳 -->
          <div class="card">
            <div class="card-header">
              <span class="card-label">日期时间 → 时间戳</span>
            </div>
            <div class="card-body">
              <div class="field-row">
                <label class="field-label">日期时间</label>
                <input class="field-input" type="datetime-local" v-model="dtInput" @input="dateToTs" />
              </div>
              <template v-if="dtResult.ms">
                <div class="result-grid">
                  <div class="rg-item" v-for="r in dtResult.rows" :key="r.label">
                    <span class="rg-label">{{ r.label }}</span>
                    <div class="rg-val-wrap">
                      <span class="rg-val mono">{{ r.val }}</span>
                      <button class="copy-mini" @click="copyText(r.val)" title="复制">
                        <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                             stroke-width="2">
                          <rect x="9" y="9" width="13" height="13" rx="2" />
                          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
                        </svg>
                      </button>
                    </div>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </div>

        <!-- 时区快查 -->
        <div class="card mt12">
          <div class="card-header">
            <span class="card-label">全球时区快查</span>
            <span class="card-sub">基于当前本地时间</span>
          </div>
          <div class="tz-grid">
            <div class="tz-item" v-for="tz in timezones" :key="tz.zone">
              <span class="tz-name">{{ tz.label }}</span>
              <span class="tz-time mono" :id="'tz-'+tz.zone">{{ tz.time }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- ══════════════════════════════
           2. Cron 解析
           ══════════════════════════════ -->
      <div v-show="activeTab === 'cron'" class="panel-wrap">
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

      <!-- ══════════════════════════════
           3. 正则表达式测试
           ══════════════════════════════ -->
      <div v-show="activeTab === 'regex'" class="panel-wrap">
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

      <!-- ══════════════════════════════
           4. URL 解析
           ══════════════════════════════ -->
      <div v-show="activeTab === 'url'" class="panel-wrap">
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
              <input class="field-input mono flex1" v-model="urlInput"
                     placeholder="https://example.com/path?key=value#hash" @input="parseUrl" />
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

      <!-- ══════════════════════════════
           5. 进制转换
           ══════════════════════════════ -->
      <div v-show="activeTab === 'radix'" class="panel-wrap">
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
                      <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor"
                           stroke-width="2">
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

      <!-- ══════════════════════════════
           6. SQL 工具
           ══════════════════════════════ -->
      <div v-show="activeTab === 'sql'" class="panel-wrap">
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
              <textarea class="field-input mono"
                        style="width:100%;resize:vertical;min-height:180px;box-sizing:border-box" v-model="sqlInput"
                        placeholder="输入 SQL 语句..." spellcheck="false" @input="debounceSql" />
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

      <!-- ══════════════════════════════
           7. 加密工具
           ══════════════════════════════ -->
      <div v-show="activeTab === 'crypto'" class="panel-wrap">
        <div class="two-col">
          <div class="card">
            <div class="card-header"><span class="card-label">加密配置</span></div>
            <div class="card-body gap10">
              <div class="field-row">
                <label class="field-label">算法</label>
                <div class="seg-ctrl">
                  <button v-for="a in cryptoAlgos" :key="a.val" :class="{ active: cryptoAlgo === a.val }"
                          @click="cryptoAlgo = a.val; cryptoResult = ''; cryptoError = ''">{{ a.label }}
                  </button>
                </div>
              </div>
              <template v-if="cryptoAlgo === 'aes'">
                <div class="field-row">
                  <label class="field-label">模式</label>
                  <div class="seg-ctrl">
                    <button v-for="m in ['CBC','ECB','GCM']" :key="m" :class="{ active: aesMode === m }"
                            @click="aesMode = m">{{ m }}
                    </button>
                  </div>
                </div>
                <div class="field-row">
                  <label class="field-label">密钥长度</label>
                  <div class="seg-ctrl">
                    <button v-for="k in [128,192,256]" :key="k" :class="{ active: aesKeyLen === k }"
                            @click="aesKeyLen = k">{{ k }}位
                    </button>
                  </div>
                </div>
                <div class="field-row">
                  <label class="field-label">Key</label>
                  <input class="field-input mono flex1" v-model="aesKey" placeholder="十六进制密钥..." />
                  <button class="icon-btn sm" @click="genAESKey">随机</button>
                </div>
                <div class="field-row" v-if="aesMode !== 'ECB'">
                  <label class="field-label">IV</label>
                  <input class="field-input mono flex1" v-model="aesIV" placeholder="十六进制 IV..." />
                  <button class="icon-btn sm" @click="genAESIV">随机</button>
                </div>
                <div class="field-row">
                  <label class="field-label">输出格式</label>
                  <div class="seg-ctrl">
                    <button v-for="f in ['hex','base64']" :key="f" :class="{ active: cryptoOutputFmt === f }"
                            @click="cryptoOutputFmt = f">{{ f }}
                    </button>
                  </div>
                </div>
              </template>
              <template v-if="cryptoAlgo === 'hmac'">
                <div class="field-row">
                  <label class="field-label">哈希算法</label>
                  <div class="seg-ctrl">
                    <button v-for="h in ['SHA-256','SHA-384','SHA-512']" :key="h" :class="{ active: hmacHash === h }"
                            @click="hmacHash = h">{{ h.replace('SHA-', '') }}
                    </button>
                  </div>
                </div>
                <div class="field-row">
                  <label class="field-label">Secret</label>
                  <input class="field-input mono flex1" v-model="hmacSecret" placeholder="密钥字符串..." />
                </div>
                <div class="field-row">
                  <label class="field-label">输出格式</label>
                  <div class="seg-ctrl">
                    <button v-for="f in ['hex','base64']" :key="f" :class="{ active: cryptoOutputFmt === f }"
                            @click="cryptoOutputFmt = f">{{ f }}
                    </button>
                  </div>
                </div>
              </template>
              <template v-if="cryptoAlgo === 'rsa'">
                <div class="field-row">
                  <label class="field-label">密钥长度</label>
                  <div class="seg-ctrl">
                    <button v-for="k in [1024,2048,4096]" :key="k" :class="{ active: rsaKeyLen === k }"
                            @click="rsaKeyLen = k">{{ k }}
                    </button>
                  </div>
                </div>
                <p class="field-label" style="color:var(--el-text-color-placeholder)">点击生成按钮，在右侧查看密钥对</p>
              </template>
              <div v-if="cryptoAlgo !== 'rsa'" class="field-row"
                   style="flex-direction:column;align-items:flex-start;gap:6px">
                <label class="field-label">{{ cryptoAlgo === 'hmac' ? '待签名内容' : '明文 / 密文' }}</label>
                <textarea class="field-input mono"
                          style="width:100%;resize:vertical;min-height:80px;box-sizing:border-box" v-model="cryptoInput"
                          placeholder="输入内容..." />
              </div>
              <div class="field-row" style="gap:8px">
                <button class="action-btn-sm primary" @click="doCryptoEncrypt" :disabled="cryptoLoading">
                  {{ cryptoAlgo === 'rsa' ? '生成密钥对' : cryptoAlgo === 'hmac' ? '生成签名' : '加密' }}
                </button>
                <button v-if="cryptoAlgo === 'aes'" class="action-btn-sm neutral" @click="doCryptoDecrypt"
                        :disabled="cryptoLoading">解密
                </button>
                <span v-if="cryptoLoading" class="loading-text">处理中...</span>
              </div>
              <div v-if="cryptoError" class="result-err">{{ cryptoError }}</div>
            </div>
          </div>
          <div class="card">
            <div class="card-header">
              <span
                class="card-label">{{ cryptoAlgo === 'rsa' ? 'RSA 密钥对' : cryptoAlgo === 'hmac' ? 'HMAC 签名' : '加解密结果'
                }}</span>
              <button v-if="cryptoResult || rsaKeys.pub" class="icon-btn sm accent"
                      @click="copyText(cryptoAlgo === 'rsa' ? getRsaFullKey() : cryptoResult)">复制
              </button>
            </div>
            <div class="card-body" style="overflow-y:auto">
              <template v-if="cryptoAlgo === 'rsa' && rsaKeys.pub">
                <div class="field-label mb6">公钥 (Public Key)</div>
                <pre class="code-pre"
                     style="font-size:10px;line-height:1.5;max-height:140px;overflow-y:auto">{{ rsaKeys.pub }}</pre>
                <button class="icon-btn sm mt4" @click="copyText(rsaKeys.pub)">复制公钥</button>
                <div class="field-label mb6 mt12">私钥 (Private Key)</div>
                <pre class="code-pre"
                     style="font-size:10px;line-height:1.5;max-height:140px;overflow-y:auto">{{ rsaKeys.priv }}</pre>
                <button class="icon-btn sm mt4" @click="copyText(rsaKeys.priv)">复制私钥</button>
              </template>
              <template v-else-if="cryptoResult">
                <pre class="code-pre"
                     style="word-break:break-all;white-space:pre-wrap;max-height:300px;overflow-y:auto">{{ cryptoResult
                  }}</pre>
              </template>
              <div v-else class="empty-hint-sm" style="padding:24px">结果将在此显示</div>
            </div>
          </div>
        </div>
      </div>

      <!-- ══════════════════════════════
           8. 配置生成
           ══════════════════════════════ -->
      <div v-show="activeTab === 'config'" class="panel-wrap">
        <div class="two-col">
          <div class="card" style="width:260px;flex:none">
            <div class="card-header"><span class="card-label">选择中间件</span></div>
            <div class="mw-list">
              <button v-for="mw in middlewares" :key="mw.key" class="mw-btn" :class="{ active: cfgMW === mw.key }"
                      @click="cfgMW = mw.key; resetCfgForm()">
                <span class="mw-icon" v-html="mw.icon" />
                <span class="mw-name">{{ mw.name }}</span>
                <span class="mw-tag">{{ mw.tag }}</span>
              </button>
            </div>
          </div>
          <div class="card" style="flex:1;min-width:0">
            <div class="card-header">
              <span class="card-label">{{ currentMW ? currentMW.name + ' 配置' : '参数配置' }}</span>
              <button class="icon-btn sm accent" @click="generateConfig" :disabled="!cfgMW">生成配置</button>
              <button v-if="cfgOutput" class="icon-btn sm" @click="copyText(cfgOutput)">复制</button>
            </div>
            <div class="card-body gap10" style="overflow-y:auto;flex:1">
              <template v-if="cfgMW">
                <div class="field-row" v-for="f in currentMWFields" :key="f.key">
                  <label class="field-label" style="min-width:96px;font-size:11px">{{ f.label }}</label>
                  <template v-if="f.type === 'select'">
                    <select class="field-input flex1" v-model="cfgForm[f.key]">
                      <option v-for="o in f.options" :key="o" :value="o">{{ o }}</option>
                    </select>
                  </template>
                  <template v-else-if="f.type === 'toggle'">
                    <div class="seg-ctrl">
                      <button :class="{ active: cfgForm[f.key] === true }" @click="cfgForm[f.key] = true">是</button>
                      <button :class="{ active: cfgForm[f.key] === false }" @click="cfgForm[f.key] = false">否</button>
                    </div>
                  </template>
                  <template v-else>
                    <input class="field-input mono flex1" v-model="cfgForm[f.key]" :placeholder="f.placeholder || ''" />
                  </template>
                </div>
                <div style="height:1px;background:var(--el-border-color-lighter,#ebeef5);margin:4px 0" />
                <template v-if="cfgOutput">
                  <div class="field-label mb6">生成结果</div>
                  <pre class="code-pre"
                       style="max-height:320px;overflow-y:auto;font-size:11px;line-height:1.7">{{ cfgOutput }}</pre>
                </template>
              </template>
              <div v-else class="empty-hint-sm" style="padding:24px">请先选择左侧中间件</div>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'

/* ============================================================
   Tab 配置
   ============================================================ */
const activeTab = ref('timestamp')

const tabs = [
  {
    key: 'timestamp', label: '时间戳',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>`
  },
  {
    key: 'cron', label: 'Cron',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>`
  },
  {
    key: 'regex', label: '正则',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`
  },
  {
    key: 'url', label: 'URL 解析',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>`
  },
  {
    key: 'radix', label: '进制转换',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>`
  },
  {
    key: 'sql', label: 'SQL 工具',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`
  },
  {
    key: 'crypto', label: '加密工具',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`
  },
  {
    key: 'config', label: '配置生成',
    icon: `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M19.07 4.93a10 10 0 0 1 0 14.14M4.93 4.93a10 10 0 0 0 0 14.14"/></svg>`
  }
]

/* ============================================================
   1. 时间戳工具
   ============================================================ */
const tsInput = ref(String(Date.now()))
const tsUnit = ref('ms')
const tsResult = ref({})
const dtInput = ref('')
const dtResult = ref({})

function tsToDate() {
  const raw = tsInput.value.trim()
  if (!raw) {
    tsResult.value = {}
    return
  }
  let ms = Number(raw)
  if (isNaN(ms)) {
    tsResult.value = { error: '无效的数字' }
    return
  }
  if (tsUnit.value === 's') ms = ms * 1000
  const d = new Date(ms)
  if (isNaN(d.getTime())) {
    tsResult.value = { error: '无效的时间戳' }
    return
  }

  tsResult.value = {
    utc: true,
    rows: [
      { label: 'UTC', val: d.toUTCString() },
      { label: 'ISO 8601', val: d.toISOString() },
      { label: '北京时间', val: d.toLocaleString('zh-CN', { timeZone: 'Asia/Shanghai' }) },
      { label: '毫秒戳', val: String(ms) },
      { label: '秒级戳', val: String(Math.floor(ms / 1000)) },
      {
        label: '星期',
        val: ['日', '一', '二', '三', '四', '五', '六'][d.getDay()] + ' (周' + ['日', '一', '二', '三', '四', '五', '六'][d.getDay()] + ')'
      }
    ]
  }
}

function dateToTs() {
  if (!dtInput.value) {
    dtResult.value = {}
    return
  }
  const d = new Date(dtInput.value)
  if (isNaN(d.getTime())) {
    dtResult.value = {}
    return
  }
  const ms = d.getTime()
  dtResult.value = {
    ms,
    rows: [
      { label: '毫秒时间戳', val: String(ms) },
      { label: '秒级时间戳', val: String(Math.floor(ms / 1000)) },
      { label: 'ISO 8601', val: d.toISOString() },
      { label: 'UTC 字符串', val: d.toUTCString() }
    ]
  }
}

// 初始化：当前时间
tsToDate()
const now = new Date()
const pad = n => String(n).padStart(2, '0')
dtInput.value = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`
dateToTs()

// 时区
const timezones = ref([
  { label: '北京 (CST)', zone: 'Asia/Shanghai' },
  { label: '东京 (JST)', zone: 'Asia/Tokyo' },
  { label: '首尔 (KST)', zone: 'Asia/Seoul' },
  { label: '新加坡', zone: 'Asia/Singapore' },
  { label: '伦敦 (GMT)', zone: 'Europe/London' },
  { label: '巴黎 (CET)', zone: 'Europe/Paris' },
  { label: '纽约 (EST)', zone: 'America/New_York' },
  { label: '洛杉矶 (PST)', zone: 'America/Los_Angeles' },
  { label: '迪拜 (GST)', zone: 'Asia/Dubai' },
  { label: 'UTC', zone: 'UTC' }
].map(tz => ({ ...tz, time: '' })))

function updateTimes() {
  const now = new Date()
  timezones.value.forEach(tz => {
    tz.time = now.toLocaleString('zh-CN', { timeZone: tz.zone, hour12: false })
  })
}

updateTimes()
const tzTimer = setInterval(updateTimes, 1000)
onBeforeUnmount(() => clearInterval(tzTimer))

/* ============================================================
   2. Cron 解析
   ============================================================ */
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

/* ============================================================
   3. 正则测试
   ============================================================ */
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

/* ============================================================
   4. URL 解析
   ============================================================ */
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

/* ============================================================
   5. 进制转换
   ============================================================ */
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


/* ============================================================
   6. SQL 工具
   ============================================================ */
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

/* ============================================================
   7. 加密工具
   ============================================================ */
const cryptoAlgo = ref('aes')
const cryptoInput = ref('')
const cryptoResult = ref('')
const cryptoError = ref('')
const cryptoLoading = ref(false)
const cryptoOutputFmt = ref('hex')
const aesMode = ref('CBC')
const aesKeyLen = ref(256)
const aesKey = ref('')
const aesIV = ref('')
const hmacHash = ref('SHA-256')
const hmacSecret = ref('')
const rsaKeyLen = ref(2048)
const rsaKeys = ref({ pub: '', priv: '' })

const cryptoAlgos = [
  { val: 'aes', label: 'AES' },
  { val: 'hmac', label: 'HMAC' },
  { val: 'rsa', label: 'RSA' }
]

function hexToBytes(hex) {
  const clean = hex.replace(/\s/g, '')
  const arr = new Uint8Array(clean.length / 2)
  for (let i = 0; i < arr.length; i++) arr[i] = parseInt(clean.slice(i * 2, i * 2 + 2), 16)
  return arr
}

function bytesToHex(buf) {
  return Array.from(new Uint8Array(buf)).map(b => b.toString(16).padStart(2, '0')).join('')
}

function bytesToBase64(buf) {
  return btoa(String.fromCharCode(...new Uint8Array(buf)))
}

function genRandHex(bytes) {
  const arr = new Uint8Array(bytes)
  crypto.getRandomValues(arr)
  return bytesToHex(arr.buffer)
}

function genAESKey() {
  aesKey.value = genRandHex(aesKeyLen.value / 8)
}

function genAESIV() {
  aesIV.value = genRandHex(16)
}

async function doCryptoEncrypt() {
  cryptoError.value = ''
  cryptoResult.value = ''
  cryptoLoading.value = true
  try {
    if (cryptoAlgo.value === 'aes') {
      if (!aesKey.value) {
        cryptoError.value = '请先输入或生成密钥'
        return
      }
      if (!cryptoInput.value) {
        cryptoError.value = '请输入明文'
        return
      }
      const keyBytes = hexToBytes(aesKey.value)
      const keyObj = await crypto.subtle.importKey('raw', keyBytes,
        { name: aesMode.value === 'GCM' ? 'AES-GCM' : 'AES-' + aesMode.value }, false, ['encrypt'])
      const iv = aesMode.value !== 'ECB' ? hexToBytes(aesIV.value || genRandHex(16)) : undefined
      const algo = aesMode.value === 'GCM' ? { name: 'AES-GCM', iv } :
        aesMode.value === 'CBC' ? { name: 'AES-CBC', iv } : { name: 'AES-ECB' }
      const enc = await crypto.subtle.encrypt(algo, keyObj, new TextEncoder().encode(cryptoInput.value))
      cryptoResult.value = cryptoOutputFmt.value === 'hex' ? bytesToHex(enc) : bytesToBase64(enc)
    } else if (cryptoAlgo.value === 'hmac') {
      if (!hmacSecret.value) {
        cryptoError.value = '请输入 Secret'
        return
      }
      if (!cryptoInput.value) {
        cryptoError.value = '请输入内容'
        return
      }
      const keyObj = await crypto.subtle.importKey('raw', new TextEncoder().encode(hmacSecret.value),
        { name: 'HMAC', hash: hmacHash.value }, false, ['sign'])
      const sig = await crypto.subtle.sign('HMAC', keyObj, new TextEncoder().encode(cryptoInput.value))
      cryptoResult.value = cryptoOutputFmt.value === 'hex' ? bytesToHex(sig) : bytesToBase64(sig)
    } else if (cryptoAlgo.value === 'rsa') {
      const pair = await crypto.subtle.generateKey(
        {
          name: 'RSA-OAEP',
          modulusLength: rsaKeyLen.value,
          publicExponent: new Uint8Array([1, 0, 1]),
          hash: 'SHA-256'
        },
        true, ['encrypt', 'decrypt'])
      const pubDer = await crypto.subtle.exportKey('spki', pair.publicKey)
      const privDer = await crypto.subtle.exportKey('pkcs8', pair.privateKey)
      const toPEM = (der, type) => {
        const b64 = btoa(String.fromCharCode(...new Uint8Array(der)))
        const lines = b64.match(/.{1,64}/g).join('\n')
        return `-----BEGIN ${type}-----\n${lines}\n-----END ${type}-----`
      }
      rsaKeys.value = { pub: toPEM(pubDer, 'PUBLIC KEY'), priv: toPEM(privDer, 'PRIVATE KEY') }
    }
  } catch (e) {
    cryptoError.value = '操作失败：' + e.message
  } finally {
    cryptoLoading.value = false
  }
}

async function doCryptoDecrypt() {
  cryptoError.value = ''
  cryptoResult.value = ''
  cryptoLoading.value = true
  try {
    if (!aesKey.value) {
      cryptoError.value = '请先输入密钥'
      return
    }
    if (!cryptoInput.value) {
      cryptoError.value = '请输入密文'
      return
    }
    const keyBytes = hexToBytes(aesKey.value)
    const keyObj = await crypto.subtle.importKey('raw', keyBytes,
      { name: aesMode.value === 'GCM' ? 'AES-GCM' : 'AES-' + aesMode.value }, false, ['decrypt'])
    // 尝试从 hex 或 base64 解析密文
    let cipherBytes
    try {
      cipherBytes = hexToBytes(cryptoInput.value)
    } catch {
      cipherBytes = Uint8Array.from(atob(cryptoInput.value), c => c.charCodeAt(0))
    }
    const iv = aesMode.value !== 'ECB' ? hexToBytes(aesIV.value) : undefined
    const algo = aesMode.value === 'GCM' ? { name: 'AES-GCM', iv } :
      aesMode.value === 'CBC' ? { name: 'AES-CBC', iv } : { name: 'AES-ECB' }
    const dec = await crypto.subtle.decrypt(algo, keyObj, cipherBytes)
    cryptoResult.value = new TextDecoder().decode(dec)
  } catch (e) {
    cryptoError.value = '解密失败：' + e.message
  } finally {
    cryptoLoading.value = false
  }
}

/* ============================================================
   8. 配置生成
   ============================================================ */
const cfgMW = ref('')
const cfgForm = ref({})
const cfgOutput = ref('')

const middlewares = [
  {
    key: 'nginx', name: 'Nginx', tag: 'Web服务器',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="12 2 2 7 2 17 12 22 22 17 22 7"/></svg>`,
    fields: [
      { key: 'serverName', label: '域名', type: 'text', placeholder: 'example.com', default: 'example.com' },
      { key: 'port', label: '监听端口', type: 'text', placeholder: '80', default: '80' },
      { key: 'ssl', label: '启用 HTTPS', type: 'toggle', default: false },
      {
        key: 'sslCert',
        label: 'SSL 证书路径',
        type: 'text',
        placeholder: '/etc/ssl/cert.pem',
        default: '/etc/ssl/cert.pem'
      },
      {
        key: 'sslKey',
        label: 'SSL 私钥路径',
        type: 'text',
        placeholder: '/etc/ssl/key.pem',
        default: '/etc/ssl/key.pem'
      },
      {
        key: 'proxyPass',
        label: '代理地址',
        type: 'text',
        placeholder: 'http://127.0.0.1:8080',
        default: 'http://127.0.0.1:8080'
      },
      { key: 'root', label: '静态文件根目录', type: 'text', placeholder: '/var/www/html', default: '' },
      { key: 'gzip', label: '启用 Gzip', type: 'toggle', default: true },
      { key: 'rateLimit', label: '限流 (r/s)', type: 'text', placeholder: '10', default: '10' },
      { key: 'clientMaxBody', label: '最大请求体', type: 'text', placeholder: '10m', default: '10m' }
    ]
  },
  {
    key: 'keycloak', name: 'Keycloak', tag: '身份认证',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>`,
    fields: [
      {
        key: 'hostname',
        label: '主机名 (hostname)',
        type: 'text',
        placeholder: 'auth.example.com',
        default: 'auth.example.com'
      },
      { key: 'httpPort', label: 'HTTP 端口', type: 'text', placeholder: '8080', default: '8080' },
      { key: 'httpsPort', label: 'HTTPS 端口', type: 'text', placeholder: '8443', default: '8443' },
      {
        key: 'startMode',
        label: '启动模式',
        type: 'select',
        options: ['production', 'development'],
        default: 'production'
      },
      {
        key: 'db',
        label: '数据库类型',
        type: 'select',
        options: ['postgres', 'mysql', 'mariadb', 'mssql', 'oracle', 'dev-file'],
        default: 'postgres'
      },
      {
        key: 'dbUrl',
        label: '数据库 URL',
        type: 'text',
        placeholder: 'jdbc:postgresql://localhost/keycloak',
        default: 'jdbc:postgresql://localhost/keycloak'
      },
      { key: 'dbUsername', label: '数据库用户名', type: 'text', placeholder: 'keycloak', default: 'keycloak' },
      { key: 'dbPassword', label: '数据库密码', type: 'text', placeholder: 'password', default: 'password' },
      { key: 'httpsEnabled', label: '启用 HTTPS', type: 'toggle', default: true },
      { key: 'httpEnabled', label: '启用 HTTP', type: 'toggle', default: false },
      {
        key: 'certFile',
        label: 'TLS 证书路径',
        type: 'text',
        placeholder: '/etc/certs/server.crt',
        default: '/etc/certs/server.crt'
      },
      {
        key: 'keyFile',
        label: 'TLS 私钥路径',
        type: 'text',
        placeholder: '/etc/certs/server.key',
        default: '/etc/certs/server.key'
      },
      {
        key: 'proxyHeaders',
        label: '反向代理头',
        type: 'select',
        options: ['none', 'forwarded', 'xforwarded'],
        default: 'xforwarded'
      },
      { key: 'cacheMode', label: '缓存模式', type: 'select', options: ['ispn', 'local'], default: 'ispn' },
      {
        key: 'features',
        label: '启用特性 (逗号分隔)',
        type: 'text',
        placeholder: 'token-exchange,admin-fine-grained-authz',
        default: ''
      },
      {
        key: 'logLevel',
        label: '日志级别',
        type: 'select',
        options: ['INFO', 'DEBUG', 'WARN', 'ERROR'],
        default: 'INFO'
      },
      { key: 'metricsEnabled', label: '启用 Metrics', type: 'toggle', default: true },
      { key: 'healthEnabled', label: '启用 Health Check', type: 'toggle', default: true },
      { key: 'adminUser', label: '初始管理员用户名', type: 'text', placeholder: 'admin', default: 'admin' },
      {
        key: 'adminPassword',
        label: '初始管理员密码',
        type: 'text',
        placeholder: 'Admin@123456',
        default: 'Admin@123456'
      }
    ]
  },
  {
    key: 'rabbitmq', name: 'RabbitMQ', tag: '消息队列',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>`,
    fields: [
      { key: 'host', label: '主机', type: 'text', placeholder: 'localhost', default: 'localhost' },
      { key: 'port', label: 'AMQP 端口', type: 'text', placeholder: '5672', default: '5672' },
      { key: 'mgmtPort', label: '管理端口', type: 'text', placeholder: '15672', default: '15672' },
      { key: 'username', label: '用户名', type: 'text', placeholder: 'admin', default: 'admin' },
      { key: 'password', label: '密码', type: 'text', placeholder: 'password', default: 'password' },
      { key: 'vhost', label: 'Virtual Host', type: 'text', placeholder: '/', default: '/' },
      { key: 'exchange', label: 'Exchange', type: 'text', placeholder: 'my-exchange', default: 'my.exchange' },
      { key: 'queue', label: 'Queue', type: 'text', placeholder: 'my-queue', default: 'my.queue' },
      { key: 'durable', label: '持久化', type: 'toggle', default: true }
    ]
  },
  {
    key: 'redis', name: 'Redis', tag: '缓存',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><ellipse cx="12" cy="5" rx="9" ry="3"/><path d="M21 12c0 1.66-4 3-9 3s-9-1.34-9-3"/><path d="M3 5v14c0 1.66 4 3 9 3s9-1.34 9-3V5"/></svg>`,
    fields: [
      { key: 'host', label: '主机', type: 'text', placeholder: '127.0.0.1', default: '127.0.0.1' },
      { key: 'port', label: '端口', type: 'text', placeholder: '6379', default: '6379' },
      { key: 'password', label: '密码', type: 'text', placeholder: '（无则留空）', default: '' },
      { key: 'db', label: '数据库编号', type: 'text', placeholder: '0', default: '0' },
      { key: 'maxMemory', label: '最大内存', type: 'text', placeholder: '256mb', default: '256mb' },
      {
        key: 'eviction',
        label: '淘汰策略',
        type: 'select',
        options: ['noeviction', 'allkeys-lru', 'volatile-lru', 'allkeys-random', 'volatile-random', 'volatile-ttl'],
        default: 'allkeys-lru'
      },
      {
        key: 'persistence',
        label: '持久化方式',
        type: 'select',
        options: ['none', 'RDB', 'AOF', 'RDB+AOF'],
        default: 'RDB'
      },
      { key: 'cluster', label: '集群模式', type: 'toggle', default: false }
    ]
  },
  {
    key: 'kafka', name: 'Kafka', tag: '消息流',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="3"/><path d="M12 3v3M12 18v3M3 12h3M18 12h3"/></svg>`,
    fields: [
      { key: 'brokers', label: 'Broker 地址', type: 'text', placeholder: 'localhost:9092', default: 'localhost:9092' },
      { key: 'topic', label: 'Topic', type: 'text', placeholder: 'my-topic', default: 'my-topic' },
      { key: 'groupId', label: 'Consumer Group', type: 'text', placeholder: 'my-group', default: 'my-group' },
      { key: 'partitions', label: '分区数', type: 'text', placeholder: '3', default: '3' },
      { key: 'replication', label: '副本数', type: 'text', placeholder: '1', default: '1' },
      {
        key: 'autoOffset',
        label: 'Auto Offset',
        type: 'select',
        options: ['earliest', 'latest', 'none'],
        default: 'earliest'
      },
      { key: 'ssl', label: '启用 SSL', type: 'toggle', default: false }
    ]
  },
  {
    key: 'mysql', name: 'MySQL', tag: '数据库',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2z"/><path d="M8 12h8M12 8v8"/></svg>`,
    fields: [
      { key: 'host', label: '主机', type: 'text', placeholder: '127.0.0.1', default: '127.0.0.1' },
      { key: 'port', label: '端口', type: 'text', placeholder: '3306', default: '3306' },
      { key: 'database', label: '数据库名', type: 'text', placeholder: 'mydb', default: 'mydb' },
      { key: 'username', label: '用户名', type: 'text', placeholder: 'root', default: 'root' },
      { key: 'password', label: '密码', type: 'text', placeholder: 'password', default: 'password' },
      { key: 'charset', label: '字符集', type: 'select', options: ['utf8mb4', 'utf8', 'latin1'], default: 'utf8mb4' },
      { key: 'timezone', label: '时区', type: 'text', placeholder: 'Asia/Shanghai', default: 'Asia/Shanghai' },
      { key: 'poolSize', label: '连接池大小', type: 'text', placeholder: '10', default: '10' },
      { key: 'ssl', label: '启用 SSL', type: 'toggle', default: false }
    ]
  },
  {
    key: 'nacos', name: 'Nacos', tag: '服务发现',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/></svg>`,
    fields: [
      {
        key: 'serverAddr',
        label: 'Server 地址',
        type: 'text',
        placeholder: '127.0.0.1:8848',
        default: '127.0.0.1:8848'
      },
      { key: 'namespace', label: 'Namespace', type: 'text', placeholder: 'public', default: 'public' },
      { key: 'group', label: 'Group', type: 'text', placeholder: 'DEFAULT_GROUP', default: 'DEFAULT_GROUP' },
      { key: 'username', label: '用户名', type: 'text', placeholder: 'nacos', default: 'nacos' },
      { key: 'password', label: '密码', type: 'text', placeholder: 'nacos', default: 'nacos' },
      {
        key: 'logLevel',
        label: '日志级别',
        type: 'select',
        options: ['debug', 'info', 'warn', 'error'],
        default: 'info'
      }
    ]
  },
  {
    key: 'elasticsearch', name: 'Elasticsearch', tag: '搜索',
    icon: `<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>`,
    fields: [
      {
        key: 'hosts',
        label: '节点地址',
        type: 'text',
        placeholder: 'http://localhost:9200',
        default: 'http://localhost:9200'
      },
      { key: 'username', label: '用户名', type: 'text', placeholder: 'elastic', default: 'elastic' },
      { key: 'password', label: '密码', type: 'text', placeholder: 'password', default: 'password' },
      { key: 'index', label: '索引名', type: 'text', placeholder: 'my-index', default: 'my-index' },
      { key: 'shards', label: '分片数', type: 'text', placeholder: '1', default: '1' },
      { key: 'replicas', label: '副本数', type: 'text', placeholder: '0', default: '0' },
      { key: 'ssl', label: '启用 SSL', type: 'toggle', default: false }
    ]
  }
]

const currentMW = computed(() => middlewares.find(m => m.key === cfgMW.value))
const currentMWFields = computed(() => currentMW.value?.fields || [])

function resetCfgForm() {
  cfgOutput.value = ''
  const form = {}
  currentMWFields.value.forEach(f => {
    form[f.key] = f.default ?? ''
  })
  cfgForm.value = form
}

function generateConfig() {
  if (!cfgMW.value) return
  const f = cfgForm.value
  const mw = cfgMW.value

  if (mw === 'nginx') {
    const lines = ['server {']
    lines.push(`    listen ${f.ssl ? '443 ssl' : f.port || '80'};`)
    lines.push(`    server_name ${f.serverName || 'example.com'};`)
    if (f.ssl) {
      lines.push(`    ssl_certificate     ${f.sslCert};`)
      lines.push(`    ssl_certificate_key ${f.sslKey};`)
      lines.push(`    ssl_protocols       TLSv1.2 TLSv1.3;`)
      lines.push(`    ssl_ciphers         HIGH:!aNULL:!MD5;`)
    }
    if (f.clientMaxBody) lines.push(`    client_max_body_size ${f.clientMaxBody};`)
    if (f.gzip) {
      lines.push('', '    gzip on;', '    gzip_types text/plain application/json application/javascript text/css;', '    gzip_min_length 1024;')
    }
    if (f.rateLimit) {
      lines.push('', `    limit_req_zone $binary_remote_addr zone=api:10m rate=${f.rateLimit}r/s;`)
    }
    lines.push('', '    location / {')
    if (f.root) {
      lines.push(`        root  ${f.root};`, `        index index.html;`, `        try_files $uri $uri/ /index.html;`)
    } else if (f.proxyPass) {
      lines.push(`        proxy_pass          ${f.proxyPass};`)
      lines.push(`        proxy_set_header    Host $host;`)
      lines.push(`        proxy_set_header    X-Real-IP $remote_addr;`)
      lines.push(`        proxy_set_header    X-Forwarded-For $proxy_add_x_forwarded_for;`)
      lines.push(`        proxy_set_header    X-Forwarded-Proto $scheme;`)
      if (f.rateLimit) lines.push(`        limit_req zone=api burst=20 nodelay;`)
    }
    lines.push('    }', '}')
    if (f.ssl) {
      lines.push('', '# HTTP → HTTPS 跳转', 'server {', `    listen 80;`, `    server_name ${f.serverName};`, `    return 301 https://$server_name$request_uri;`, '}')
    }
    cfgOutput.value = lines.join('\n')
  } else if (mw === 'keycloak') {
    // 生成官方 conf/keycloak.conf 格式（Server 端配置）
    const lines = [
      '# =============================================================',
      '# Keycloak Server 配置文件 (conf/keycloak.conf)',
      '# 参考文档: https://www.keycloak.org/server/configuration',
      '# =============================================================',
      '',
      '# ── 基础 & 主机名 ──',
      `hostname=${f.hostname}`,
      `# hostname-admin=${f.hostname}   # 可单独设置管理后台域名`,
      'hostname-backchannel-dynamic=false',
      '',
      '# ── HTTP / HTTPS ──',
      `http-enabled=${f.httpEnabled}`,
      `http-port=${f.httpPort}`,
      `https-port=${f.httpsPort}`
    ]

    if (f.httpsEnabled) {
      lines.push('')
      lines.push('# ── TLS 证书 ──')
      lines.push(`https-certificate-file=${f.certFile}`)
      lines.push(`https-certificate-key-file=${f.keyFile}`)
      lines.push('# https-key-store-file=/etc/certs/keystore.p12')
      lines.push('# https-key-store-password=changeit')
    }

    lines.push('')
    lines.push('# ── 反向代理 ──')
    if (f.proxyHeaders !== 'none') {
      lines.push(`proxy-headers=${f.proxyHeaders}`)
      lines.push('# 如果 Keycloak 在反向代理后面，确保代理转发 X-Forwarded-For / Forwarded 头')
    } else {
      lines.push('# proxy-headers=xforwarded   # 取消注释以启用反向代理头解析')
    }

    lines.push('')
    lines.push('# ── 数据库 ──')
    lines.push(`db=${f.db}`)
    lines.push(`db-url=${f.dbUrl}`)
    lines.push(`db-username=${f.dbUsername}`)
    lines.push(`db-password=${f.dbPassword}`)
    lines.push('# db-pool-initial-size=5')
    lines.push('# db-pool-min-size=5')
    lines.push('# db-pool-max-size=20')

    lines.push('')
    lines.push('# ── 缓存 / 集群 ──')
    lines.push(`cache=${f.cacheMode}`)
    if (f.cacheMode === 'ispn') {
      lines.push('# cache-stack=jdbc-ping   # 生产推荐，使用数据库作为集群发现')
      lines.push('# cache-embedded-mtls-enabled=true   # 节点间通信加密')
    } else {
      lines.push('# 开发模式使用 local 缓存，不支持集群')
    }

    lines.push('')
    lines.push('# ── 可观测性 ──')
    lines.push(`metrics-enabled=${f.metricsEnabled}`)
    lines.push(`health-enabled=${f.healthEnabled}`)
    if (f.metricsEnabled) {
      lines.push('# 访问路径: http://localhost:9000/metrics')
      lines.push('# 访问路径: http://localhost:9000/health/ready')
    }

    if (f.features) {
      lines.push('')
      lines.push('# ── 可选特性 ──')
      lines.push(`features=${f.features}`)
    }

    lines.push('')
    lines.push('# ── 日志 ──')
    lines.push(`log-level=${f.logLevel}`)
    lines.push('# log=console,file')
    lines.push('# log-file=/var/log/keycloak/keycloak.log')

    lines.push('')
    lines.push('# ── 请求限流 ──')
    lines.push('# http-max-queued-requests=1000')

    lines.push('')
    lines.push('# =============================================================')
    lines.push('# 启动命令示例')
    lines.push('# =============================================================')
    if (f.startMode === 'production') {
      lines.push('# 生产模式（先构建优化镜像，再启动）:')
      lines.push('#   bin/kc.sh build')
      lines.push('#   bin/kc.sh start --config-file=/path/to/keycloak.conf')
      lines.push('#')
      lines.push('# 或直接启动（自动 build）:')
      lines.push('#   bin/kc.sh start')
    } else {
      lines.push('# 开发模式（快速启动，非生产使用）:')
      lines.push('#   bin/kc.sh start-dev')
    }
    lines.push('')
    lines.push('# ── Docker Compose 示例 ──')
    lines.push('# services:')
    lines.push('#   keycloak:')
    lines.push('#     image: quay.io/keycloak/keycloak:latest')
    lines.push(`#     command: ${f.startMode === 'production' ? 'start' : 'start-dev'}`)
    lines.push('#     environment:')
    lines.push(`#       KC_HOSTNAME: ${f.hostname}`)
    lines.push(`#       KC_DB: ${f.db}`)
    lines.push(`#       KC_DB_URL: ${f.dbUrl}`)
    lines.push(`#       KC_DB_USERNAME: ${f.dbUsername}`)
    lines.push('#       KC_DB_PASSWORD: <your-db-password>')
    lines.push(`#       KC_METRICS_ENABLED: "${f.metricsEnabled}"`)
    lines.push(`#       KC_HEALTH_ENABLED: "${f.healthEnabled}"`)
    lines.push(`#       KC_BOOTSTRAP_ADMIN_USERNAME: ${f.adminUser}`)
    lines.push('#       KC_BOOTSTRAP_ADMIN_PASSWORD: <your-admin-password>')
    lines.push(`#     ports:`)
    lines.push(`#       - "${f.httpPort}:8080"`)
    if (f.httpsEnabled) {
      lines.push(`#       - "${f.httpsPort}:8443"`)
    }

    cfgOutput.value = lines.join('\n')
  } else if (mw === 'rabbitmq') {
    const springCfg = [
      '# Spring Boot application.yml - RabbitMQ',
      'spring:',
      '  rabbitmq:',
      `    host: ${f.host}`,
      `    port: ${f.port}`,
      `    username: ${f.username}`,
      `    password: ${f.password}`,
      `    virtual-host: ${f.vhost}`,
      '    listener:',
      '      simple:',
      '        acknowledge-mode: manual',
      '        prefetch: 10',
      '    template:',
      `      exchange: ${f.exchange}`,
      '',
      '# Queue & Exchange 定义',
      `# Queue:    ${f.queue}  (durable: ${f.durable})`,
      `# Exchange: ${f.exchange}  (type: direct, durable: ${f.durable})`,
      `# Binding:  routingKey = ${f.queue}`
    ]
    cfgOutput.value = springCfg.join('\n')
  } else if (mw === 'redis') {
    const lines = [
      `# Redis ${f.persistence === 'none' ? '(无持久化)' : f.persistence} 配置`,
      `bind ${f.host}`,
      `port ${f.port}`,
      f.password ? `requirepass ${f.password}` : '# requirepass (未设置密码)',
      `databases 16`,
      `maxmemory ${f.maxMemory}`,
      `maxmemory-policy ${f.eviction}`
    ]
    if (f.persistence === 'RDB' || f.persistence === 'RDB+AOF') {
      lines.push('', '# RDB 快照', 'save 900 1', 'save 300 10', 'save 60 10000', 'dbfilename dump.rdb', 'dir /var/lib/redis')
    }
    if (f.persistence === 'AOF' || f.persistence === 'RDB+AOF') {
      lines.push('', '# AOF 日志', 'appendonly yes', 'appendfilename "appendonly.aof"', 'appendfsync everysec')
    }
    if (f.cluster) {
      lines.push('', '# 集群模式', 'cluster-enabled yes', 'cluster-config-file nodes.conf', 'cluster-node-timeout 5000')
    }
    lines.push('', '# Spring Boot application.yml', 'spring:', '  redis:',
      `    host: ${f.host}`, `    port: ${f.port}`,
      f.password ? `    password: ${f.password}` : '    # password:',
      `    database: ${f.db}`,
      '    lettuce:', '      pool:', '        max-active: 8', '        max-idle: 8', '        min-idle: 0')
    cfgOutput.value = lines.join('\n')
  } else if (mw === 'kafka') {
    const lines = [
      '# Spring Boot application.yml - Kafka',
      'spring:',
      '  kafka:',
      `    bootstrap-servers: ${f.brokers}`,
      '    producer:',
      '      key-serializer: org.apache.kafka.common.serialization.StringSerializer',
      '      value-serializer: org.apache.kafka.common.serialization.StringSerializer',
      '      acks: all',
      '      retries: 3',
      '    consumer:',
      `      group-id: ${f.groupId}`,
      `      auto-offset-reset: ${f.autoOffset}`,
      '      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer',
      '      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer',
      '      enable-auto-commit: false',
      '',
      '# Topic 配置',
      `# topic: ${f.topic}  partitions: ${f.partitions}  replication: ${f.replication}`
    ]
    cfgOutput.value = lines.join('\n')
  } else if (mw === 'mysql') {
    const lines = [
      '# Spring Boot application.yml - MySQL',
      'spring:',
      '  datasource:',
      `    url: jdbc:mysql://${f.host}:${f.port}/${f.database}?useUnicode=true&characterEncoding=${f.charset}&serverTimezone=${f.timezone}&useSSL=${f.ssl}`,
      `    username: ${f.username}`,
      `    password: ${f.password}`,
      '    driver-class-name: com.mysql.cj.jdbc.Driver',
      '    hikari:',
      `      maximum-pool-size: ${f.poolSize}`,
      '      minimum-idle: 5',
      '      connection-timeout: 30000',
      '      idle-timeout: 600000',
      '      max-lifetime: 1800000',
      '',
      '# MyBatis-Plus',
      'mybatis-plus:',
      '  configuration:',
      '    map-underscore-to-camel-case: true',
      '    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl'
    ]
    cfgOutput.value = lines.join('\n')
  } else if (mw === 'nacos') {
    const lines = [
      '# Spring Boot bootstrap.yml - Nacos',
      'spring:',
      '  application:',
      '    name: my-service',
      '  cloud:',
      '    nacos:',
      '      discovery:',
      `        server-addr: ${f.serverAddr}`,
      `        namespace: ${f.namespace}`,
      `        group: ${f.group}`,
      `        username: ${f.username}`,
      `        password: ${f.password}`,
      '      config:',
      `        server-addr: ${f.serverAddr}`,
      `        namespace: ${f.namespace}`,
      `        group: ${f.group}`,
      '        file-extension: yaml',
      `        username: ${f.username}`,
      `        password: ${f.password}`,
      '',
      'logging:',
      `  level:`,
      `    com.alibaba.nacos: ${f.logLevel}`
    ]
    cfgOutput.value = lines.join('\n')
  } else if (mw === 'elasticsearch') {
    const lines = [
      '# Spring Boot application.yml - Elasticsearch',
      'spring:',
      '  elasticsearch:',
      `    uris: ${f.hosts}`,
      `    username: ${f.username}`,
      `    password: ${f.password}`,
      f.ssl ? '    ssl:\n      verification-mode: certificate' : '',
      '',
      '# Index 配置',
      `# index:    ${f.index}`,
      `# shards:   ${f.shards}`,
      `# replicas: ${f.replicas}`,
      '',
      '# Java 客户端依赖 (pom.xml)',
      '# <dependency>',
      '#   <groupId>org.springframework.boot</groupId>',
      '#   <artifactId>spring-boot-starter-data-elasticsearch</artifactId>',
      '# </dependency>'
    ].filter(l => l !== '')
    cfgOutput.value = lines.join('\n')
  }
}

/* ============================================================
   通用：复制
   ============================================================ */
const copiedText = ref('')

function getRsaFullKey() {
  return rsaKeys.value.pub + '\n\n' + rsaKeys.value.priv
}

async function copyText(text) {
  await navigator.clipboard.writeText(text).catch(() => {
  })
  copiedText.value = text
  setTimeout(() => {
    copiedText.value = ''
  }, 1500)
}
</script>

<style lang="scss" scoped>
/* ── 整体容器 ── */
.dev-utils-page {
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

/* ── Tool Tabs ── */
.tool-tabs {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
  border-bottom: 2px solid var(--el-border-color-lighter, #ebeef5);
  padding-bottom: 0;
}

.tool-tab {
  display: flex;
  align-items: center;
  gap: 7px;
  padding: 8px 18px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-text-color-secondary, #909399);
  cursor: pointer;
  transition: all .18s;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  border-radius: 8px 8px 0 0;

  .tab-icon {
    display: flex;
    align-items: center;
  }

  &:hover {
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.active {
    color: var(--el-color-primary, #409eff);
    border-bottom-color: var(--el-color-primary, #409eff);
    font-weight: 600;
    background: var(--el-bg-color, #fff);
  }
}

/* ── Tool Body ── */
.tool-body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 3px;
  }
}

.panel-wrap {
  display: flex;
  flex-direction: column;
  gap: 0;
  height: 100%;
}

/* ── Card ── */
.card {
  background: var(--el-bg-color, #fff);
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  display: flex;
  flex-direction: column;
  overflow: hidden;
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
  font-size: 11px;
  font-weight: 700;
  letter-spacing: .08em;
  text-transform: uppercase;
  color: var(--el-text-color-secondary, #909399);
  flex: 1;
}

.card-sub {
  font-size: 11px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

.card-actions {
  display: flex;
  gap: 5px;
  flex-wrap: wrap;
}

.card-body {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;

  &.gap10 {
    gap: 10px;
  }
}

/* ── Layout helpers ── */
.two-col {
  display: flex;
  gap: 12px;
  flex: 1;
  min-height: 0;

  > .card {
    flex: 1;
    min-width: 0;
  }
}

.mt12 {
  margin-top: 12px;
}

.mt4 {
  margin-top: 4px;
}

.mb6 {
  margin-bottom: 6px;
}

.mt8 {
  margin-top: 8px;
}

.mt12 {
  margin-top: 12px;
}

/* ── Form Elements ── */
.field-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.field-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  min-width: 64px;
  flex-shrink: 0;
}

.field-input {
  height: 32px;
  padding: 0 10px;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  font-size: 12px;
  background: var(--el-bg-color, #fff);
  color: var(--el-text-color-primary, #303133);
  outline: none;
  transition: border-color .15s;
  box-sizing: border-box;

  &:focus {
    border-color: var(--el-color-primary, #409eff);
  }

  &.mono {
    font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  }

  &.flex1 {
    flex: 1;
  }

  &[type="datetime-local"] {
    flex: 1;
  }
}

textarea.field-input {
  height: auto;
  padding: 8px 10px;
  line-height: 1.6;
}

.input-with-seg {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
}

/* ── Seg Control ── */
.seg-ctrl {
  display: flex;
  border: 1.5px solid var(--el-border-color, #dcdfe6);
  border-radius: 7px;
  overflow: hidden;
  flex-shrink: 0;

  button {
    padding: 5px 10px;
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

/* ── Icon Button ── */
.icon-btn {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 5px 12px;
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

  &.sm {
    padding: 3px 8px;
    font-size: 10px;
  }

  &:disabled {
    opacity: .4;
    cursor: not-allowed;
  }

  &.accent {
    background: var(--el-color-primary-light-9, #ecf5ff);
    border-color: var(--el-color-primary-light-5, #a0cfff);
    color: var(--el-color-primary, #409eff);
    font-weight: 600;
  }
}

/* ── Copy mini button ── */
.copy-mini {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  background: transparent;
  cursor: pointer;
  color: var(--el-text-color-placeholder, #c0c4cc);
  transition: all .15s;
  flex-shrink: 0;

  &:hover {
    color: var(--el-color-primary, #409eff);
    border-color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

/* ── Result Grid ── */
.result-grid {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rg-item {
  display: flex;
  align-items: center;
  padding: 7px 10px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.rg-label {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
  min-width: 80px;
  flex-shrink: 0;
}

.rg-val-wrap {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
  min-width: 0;
}

.rg-val {
  font-size: 12px;
  flex: 1;
  word-break: break-all;
}

.mono {
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
}

.result-err {
  font-size: 12px;
  color: #dc2626;
  background: #fff1f2;
  padding: 8px 10px;
  border-radius: 7px;
  border: 1px solid #fca5a5;
}

/* ── Timezone Grid ── */
.tz-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 0;
  padding: 4px 0;
}

.tz-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);

  &:last-child {
    border-bottom: none;
  }
}

.tz-name {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
}

.tz-time {
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
}

/* ── Cron ── */
.cron-fields {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.cron-field-item {
  flex: 1;
  min-width: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 8px;
  border: 1.5px solid var(--el-border-color-lighter, #ebeef5);
  border-radius: 10px;
  background: var(--el-fill-color-lighter, #fafafa);
}

.cf-val {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: .04em;

  &.cf-valid {
    color: var(--el-color-primary, #409eff);
  }

  &.cf-invalid {
    color: #dc2626;
  }
}

.cf-name {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
}

.cf-desc {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  text-align: center;
}

.cron-human {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  border: 1px solid var(--el-color-primary-light-5, #a0cfff);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: var(--el-color-primary, #409eff);
}

.cron-next {
  margin-top: 4px;
}

.next-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
  margin-bottom: 6px;
}

.next-list {
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.next-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px;
  border-radius: 7px;
  background: var(--el-fill-color-lighter, #fafafa);
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
}

.next-idx {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  background: var(--el-color-primary-light-7, #79bbff);
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

/* ── Regex ── */
.regex-input-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.regex-slash {
  font-size: 20px;
  font-weight: 300;
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-family: monospace;
}

.tpl-list {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.tpl-btn {
  padding: 3px 10px;
  border-radius: 5px;
  border: 1px solid var(--el-border-color, #dcdfe6);
  background: var(--el-fill-color-lighter, #fafafa);
  font-size: 11px;
  color: var(--el-text-color-regular, #606266);
  cursor: pointer;
  transition: all .15s;

  &:hover {
    border-color: var(--el-color-primary, #409eff);
    color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

.regex-preview {
  padding: 10px 12px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
  font-family: 'Menlo', 'Monaco', monospace;
  font-size: 12px;
  line-height: 1.7;
  word-break: break-all;
  white-space: pre-wrap;
  min-height: 48px;
}

:deep(mark.rx-match) {
  background: #fde68a;
  color: #92400e;
  border-radius: 2px;
  padding: 0 1px;
}

.match-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 600;
  background: #dcfce7;
  color: #16a34a;
}

.no-match-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 600;
  background: #fee2e2;
  color: #dc2626;
}

.match-list {
  display: flex;
  flex-direction: column;
  gap: 5px;
  max-height: 240px;
  overflow-y: auto;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

.match-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 10px;
  border-radius: 7px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.match-idx {
  width: 18px;
  height: 18px;
  border-radius: 4px;
  background: #fde68a;
  color: #92400e;
  font-size: 10px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.match-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.match-val {
  font-size: 12px;
  color: var(--el-text-color-primary, #303133);
  word-break: break-all;
}

.match-pos {
  font-size: 10px;
  color: var(--el-text-color-placeholder, #c0c4cc);
}

.match-groups {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-top: 2px;
}

.group-item {
  font-size: 10px;
  padding: 1px 6px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  border-radius: 4px;
}

/* ── URL ── */
.url-result-wrap {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.url-parts-card {
  flex-shrink: 0;
}

.url-parts {
  padding: 8px 0;
}

.up-item {
  display: flex;
  align-items: center;
  padding: 7px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);

  &:last-child {
    border-bottom: none;
  }
}

.up-key {
  font-size: 11px;
  color: var(--el-text-color-secondary, #909399);
  min-width: 56px;
  flex-shrink: 0;
}

.up-val-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.up-tag {
  font-size: 10px;
  font-weight: 700;
  padding: 1px 6px;
  border-radius: 4px;
  flex-shrink: 0;
}

.up-val {
  font-size: 12px;
  flex: 1;
  word-break: break-all;
  color: var(--el-text-color-primary, #303133);
}

.count-badge {
  padding: 2px 8px;
  border-radius: 5px;
  font-size: 11px;
  font-weight: 600;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
}

.param-table {
  overflow-x: auto;
}

.pt-head, .pt-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  padding: 7px 14px;
  border-bottom: 1px solid var(--el-border-color-lighter, #ebeef5);
  font-size: 12px;
  gap: 10px;
}

.pt-head {
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: .06em;
  color: var(--el-text-color-placeholder, #c0c4cc);
  background: var(--el-fill-color-lighter, #fafafa);
}

.pt-row:last-child {
  border-bottom: none;
}

.pt-key {
  color: var(--el-color-primary, #409eff);
  font-weight: 600;
}

.pt-raw {
  color: var(--el-text-color-secondary, #909399);
}

.pt-decoded {
  color: var(--el-text-color-primary, #303133);
}

/* ── Radix ── */
.radix-results {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rx-item {
  padding: 10px 12px;
  border-radius: 9px;
  border: 1.5px solid var(--el-border-color-lighter, #ebeef5);
  background: var(--el-fill-color-lighter, #fafafa);
}

.rx-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.rx-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--el-text-color-secondary, #909399);
}

.rx-base {
  font-size: 10px;
  padding: 1px 6px;
  background: var(--el-color-primary-light-9, #ecf5ff);
  color: var(--el-color-primary, #409eff);
  border-radius: 4px;
  font-weight: 700;
}

.rx-val-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.rx-val {
  font-size: 14px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
  flex: 1;
  word-break: break-all;
}

.rx-bits {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 6px;
}

.bit-group {
  padding: 2px 6px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 11px;
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-text-color-secondary, #909399);

  &.bit-one {
    background: var(--el-color-primary-light-8, #d9ecff);
    color: var(--el-color-primary, #409eff);
  }
}

.empty-hint-sm {
  font-size: 12px;
  color: var(--el-text-color-placeholder, #c0c4cc);
  text-align: center;
  padding: 20px;
}

.bit-info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 6px;
}

.bi-item {
  display: flex;
  flex-direction: column;
  gap: 3px;
  padding: 8px 10px;
  border-radius: 8px;
  background: var(--el-fill-color-lighter, #fafafa);
  border: 1px solid var(--el-border-color-lighter, #ebeef5);
}

.bi-label {
  font-size: 10px;
  color: var(--el-text-color-secondary, #909399);
}

.bi-val {
  font-size: 13px;
  font-weight: 700;
  color: var(--el-text-color-primary, #303133);
}

/* ── SQL 高亮 ── */
:deep(.sql-kw) {
  color: var(--el-color-primary, #409eff);
  font-weight: 700;
}

:deep(.sql-str) {
  color: #16a34a;
}

:deep(.sql-comment) {
  color: var(--el-text-color-placeholder, #c0c4cc);
  font-style: italic;
}

:deep(.sql-id) {
  color: #d97706;
}

/* ── code-pre ── */
.code-pre {
  margin: 0;
  padding: 12px 14px;
  font-family: 'Menlo', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.7;
  color: var(--el-text-color-primary, #303133);
  background: var(--el-fill-color-lighter, #fafafa);
  white-space: pre-wrap;
  word-break: break-all;
  overflow-y: auto;
  flex: 1;
}

/* ── 加密操作按钮 ── */
.action-btn-sm {
  padding: 7px 18px;
  border-radius: 7px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all .15s;

  &.primary {
    background: var(--el-color-primary, #409eff);
    color: #fff;

    &:hover:not(:disabled) {
      background: var(--el-color-primary-dark-2, #337ecc);
    }

    &:disabled {
      opacity: .4;
      cursor: not-allowed;
    }
  }

  &.neutral {
    background: var(--el-fill-color, #f0f2f5);
    border: 1.5px solid var(--el-border-color, #dcdfe6);
    color: var(--el-text-color-regular, #606266);

    &:hover:not(:disabled) {
      border-color: var(--el-color-primary, #409eff);
      color: var(--el-color-primary, #409eff);
    }

    &:disabled {
      opacity: .4;
      cursor: not-allowed;
    }
  }
}

.loading-text {
  font-size: 11px;
  color: var(--el-color-primary, #409eff);
}

/* ── 中间件选择 ── */
.mw-list {
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow-y: auto;
  flex: 1;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, .08);
    border-radius: 2px;
  }
}

.mw-btn {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px 12px;
  border-radius: 8px;
  border: 1.5px solid var(--el-border-color-lighter, #ebeef5);
  background: transparent;
  cursor: pointer;
  transition: all .15s;
  text-align: left;

  &:hover {
    border-color: var(--el-color-primary-light-5, #a0cfff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }

  &.active {
    border-color: var(--el-color-primary, #409eff);
    background: var(--el-color-primary-light-9, #ecf5ff);
  }
}

.mw-icon {
  color: var(--el-color-primary, #409eff);
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.mw-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--el-text-color-primary, #303133);
  flex: 1;
}

.mw-tag {
  font-size: 10px;
  padding: 1px 7px;
  border-radius: 4px;
  background: var(--el-fill-color, #f0f2f5);
  color: var(--el-text-color-secondary, #909399);
}

.flex1 {
  flex: 1;
}

.divider-line {
  height: 1px;
  background: var(--el-border-color-lighter, #ebeef5);
}

/* select input */
select.field-input {
  appearance: auto;
  cursor: pointer;
}

</style>