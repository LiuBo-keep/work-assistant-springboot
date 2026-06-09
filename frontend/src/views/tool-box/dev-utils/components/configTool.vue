<template>
  <div class="panel-wrap">
    <div class="two-col cfg-col">
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
      <!-- 右侧：上下分栏 —— 参数区 + 结果区各占独立空间 -->
      <div class="cfg-right-col">

        <!-- 上：参数表单（固定高度，内部滚动） -->
        <div class="card cfg-form-card">
          <div class="card-header">
            <span class="card-label">{{ currentMW ? currentMW.name + ' 参数' : '参数配置' }}</span>
            <button class="icon-btn sm accent" @click="generateConfig" :disabled="!cfgMW">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                <polyline points="23 4 23 10 17 10" />
                <path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10" />
              </svg>
              生成配置
            </button>
          </div>
          <div class="card-body gap10 cfg-form-body">
            <template v-if="cfgMW">
              <div class="field-row" v-for="f in currentMWFields" :key="f.key">
                <label class="field-label cfg-field-label">{{ f.label }}</label>
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
            </template>
            <div v-else class="empty-hint-sm" style="padding:16px 0">请先选择左侧中间件</div>
          </div>
        </div>

        <!-- 下：生成结果（占满剩余高度） -->
        <div class="card cfg-result-card">
          <div class="card-header">
                <span class="card-label">
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline
                    points="16 18 22 12 16 6" /><polyline points="8 6 2 12 8 18" /></svg>
                  生成结果
                </span>
            <button v-if="cfgOutput" class="icon-btn sm accent" @click="copyText(cfgOutput)">
              <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="9" y="9" width="13" height="13" rx="2" />
                <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1" />
              </svg>
              复制
            </button>
          </div>
          <div class="cfg-result-body">
            <pre v-if="cfgOutput" class="cfg-result-pre">{{ cfgOutput }}</pre>
            <div v-else class="cfg-result-empty">
              <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="0.8"
                   opacity="0.25">
                <polyline points="16 18 22 12 16 6" />
                <polyline points="8 6 2 12 8 18" />
              </svg>
              <span>点击「生成配置」查看结果</span>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

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
      { key: 'nodeName', label: '节点名称', type: 'text', placeholder: 'rabbit@hostname', default: 'rabbit@localhost' },
      { key: 'listenAddr', label: '监听地址', type: 'text', placeholder: '0.0.0.0', default: '0.0.0.0' },
      { key: 'amqpPort', label: 'AMQP 端口', type: 'text', placeholder: '5672', default: '5672' },
      { key: 'mgmtPort', label: '管理后台端口', type: 'text', placeholder: '15672', default: '15672' },
      { key: 'username', label: '默认用户名', type: 'text', placeholder: 'admin', default: 'admin' },
      { key: 'password', label: '默认密码', type: 'text', placeholder: 'password', default: 'password' },
      { key: 'vhost', label: '默认 Virtual Host', type: 'text', placeholder: '/', default: '/' },
      { key: 'loopbackUsers', label: '限制 guest 仅本地', type: 'toggle', default: true },
      { key: 'tlsEnabled', label: '启用 TLS (AMQPS)', type: 'toggle', default: false },
      { key: 'tlsPort', label: 'TLS 端口', type: 'text', placeholder: '5671', default: '5671' },
      {
        key: 'tlsCACert',
        label: 'CA 证书路径',
        type: 'text',
        placeholder: '/etc/rabbitmq/ca_cert.pem',
        default: '/etc/rabbitmq/ca_cert.pem'
      },
      {
        key: 'tlsCert',
        label: '服务端证书路径',
        type: 'text',
        placeholder: '/etc/rabbitmq/server_cert.pem',
        default: '/etc/rabbitmq/server_cert.pem'
      },
      {
        key: 'tlsKey',
        label: '服务端私钥路径',
        type: 'text',
        placeholder: '/etc/rabbitmq/server_key.pem',
        default: '/etc/rabbitmq/server_key.pem'
      },
      {
        key: 'tlsVerify',
        label: 'TLS 验证模式',
        type: 'select',
        options: ['verify_none', 'verify_peer'],
        default: 'verify_none'
      },
      { key: 'heartbeat', label: '心跳超时 (秒)', type: 'text', placeholder: '60', default: '60' },
      { key: 'frameMax', label: '最大帧大小 (bytes)', type: 'text', placeholder: '131072', default: '131072' },
      { key: 'channelMax', label: '最大信道数', type: 'text', placeholder: '2047', default: '2047' },
      {
        key: 'vmMemoryHigh',
        label: '内存告警阈值',
        type: 'select',
        options: ['0.4', '0.5', '0.6', '0.7'],
        default: '0.4'
      },
      { key: 'diskFreeLimit', label: '磁盘空间告警', type: 'text', placeholder: '50MB', default: '50MB' },
      {
        key: 'logLevel',
        label: '日志级别',
        type: 'select',
        options: ['debug', 'info', 'warning', 'error', 'critical'],
        default: 'info'
      },
      {
        key: 'logDir',
        label: '日志目录',
        type: 'text',
        placeholder: '/var/log/rabbitmq',
        default: '/var/log/rabbitmq'
      },
      {
        key: 'mnesiaDirStr',
        label: '数据目录',
        type: 'text',
        placeholder: '/var/lib/rabbitmq/mnesia',
        default: '/var/lib/rabbitmq/mnesia'
      },
      { key: 'mgmtEnabled', label: '启用管理插件', type: 'toggle', default: true },
      { key: 'prometheusEnabled', label: '启用 Prometheus', type: 'toggle', default: false },
      { key: 'numAcceptors', label: 'TCP 接收器数量', type: 'text', placeholder: '10', default: '10' }
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
    // 生成官方 rabbitmq.conf 格式
    // 参考: https://github.com/rabbitmq/rabbitmq-server/blob/main/deps/rabbit/docs/rabbitmq.conf.example
    const lines = [
      '## ================================================================',
      '## RabbitMQ Server 配置文件 (rabbitmq.conf)',
      '## 参考文档: https://www.rabbitmq.com/docs/configure',
      '## ================================================================',
      '',
      '## ── 节点 ──',
      `# node.name = ${f.nodeName}`,
      '',
      '## ── 网络 & 监听 ──',
      `listeners.tcp.default = ${f.listenAddr}:${f.amqpPort}`,
      `num_acceptors.tcp = ${f.numAcceptors}`,
      '',
      `heartbeat = ${f.heartbeat}`,
      `frame_max = ${f.frameMax}`,
      `channel_max = ${f.channelMax}`,
      ''
    ]

    if (f.tlsEnabled) {
      lines.push('## ── TLS (AMQPS) ──')
      lines.push(`listeners.ssl.default = ${f.tlsPort}`)
      lines.push(`ssl_options.cacertfile = ${f.tlsCACert}`)
      lines.push(`ssl_options.certfile   = ${f.tlsCert}`)
      lines.push(`ssl_options.keyfile    = ${f.tlsKey}`)
      lines.push(`ssl_options.verify     = ${f.tlsVerify}`)
      lines.push(`ssl_options.fail_if_no_peer_cert = ${f.tlsVerify === 'verify_peer' ? 'true' : 'false'}`)
      lines.push('ssl_options.versions.1 = tlsv1.3')
      lines.push('ssl_options.versions.2 = tlsv1.2')
      lines.push('')
    }

    lines.push('## ── 访问控制 ──')
    lines.push(`loopback_users.guest = ${f.loopbackUsers}`)
    lines.push('')
    lines.push('## ── 默认 Virtual Host & 用户 ──')
    lines.push(`default_vhost = ${f.vhost}`)
    lines.push(`default_user  = ${f.username}`)
    lines.push(`default_pass  = ${f.password}`)
    lines.push('default_user_tags.administrator = true')
    lines.push('default_permissions.configure = .*')
    lines.push('default_permissions.read      = .*')
    lines.push('default_permissions.write     = .*')
    lines.push('')
    lines.push('## ── 资源告警 ──')
    lines.push(`vm_memory_high_watermark.relative = ${f.vmMemoryHigh}`)
    lines.push(`disk_free_limit.absolute = ${f.diskFreeLimit}`)
    lines.push('')
    lines.push('## ── 数据目录 ──')
    lines.push(`mnesia_base_dir = ${f.mnesiaDirStr}`)
    lines.push('')
    lines.push('## ── 日志 ──')
    lines.push(`log.dir        = ${f.logDir}`)
    lines.push('log.file       = rabbit.log')
    lines.push(`log.file.level = ${f.logLevel}`)
    lines.push('log.console    = false')
    lines.push('')

    if (f.mgmtEnabled) {
      lines.push('## ── 管理后台插件 ──')
      lines.push(`management.tcp.port = ${f.mgmtPort}`)
      lines.push('management.tcp.ip   = 0.0.0.0')
      lines.push('management.rates_mode = basic')
      lines.push('')
    }

    if (f.prometheusEnabled) {
      lines.push('## ── Prometheus 指标 ──')
      lines.push('prometheus.tcp.port = 15692')
      lines.push('prometheus.tcp.ip   = 0.0.0.0')
      lines.push('')
    }

    const enabledPlugins = []
    if (f.mgmtEnabled) enabledPlugins.push('rabbitmq_management')
    if (f.prometheusEnabled) enabledPlugins.push('rabbitmq_prometheus')

    lines.push('## ── enabled_plugins (/etc/rabbitmq/enabled_plugins) ──')
    lines.push(`[${enabledPlugins.join(',')}].`)
    lines.push('')
    lines.push('## ── Docker 启动示例 ──')
    lines.push('## docker run -d \\')
    lines.push(`##   -p ${f.amqpPort}:5672 \\`)
    if (f.tlsEnabled) lines.push(`##   -p ${f.tlsPort}:5671 \\`)
    lines.push(`##   -p ${f.mgmtPort}:15672 \\`)
    if (f.prometheusEnabled) lines.push('##   -p 15692:15692 \\')
    lines.push(`##   -e RABBITMQ_DEFAULT_USER=${f.username} \\`)
    lines.push(`##   -e RABBITMQ_DEFAULT_PASS=${f.password} \\`)
    lines.push(`##   -e RABBITMQ_DEFAULT_VHOST=${f.vhost} \\`)
    lines.push('##   -v /path/to/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf \\')
    lines.push('##   -v /path/to/enabled_plugins:/etc/rabbitmq/enabled_plugins \\')
    lines.push('##   -v rabbitmq_data:/var/lib/rabbitmq \\')
    lines.push('##   rabbitmq:4-management')
    lines.push('')
    lines.push('## ── Spring Boot application.yml 客户端配置 ──')
    lines.push('## spring:')
    lines.push('##   rabbitmq:')
    lines.push(`##     host: localhost`)
    lines.push(`##     port: ${f.amqpPort}`)
    lines.push(`##     username: ${f.username}`)
    lines.push(`##     password: ${f.password}`)
    lines.push(`##     virtual-host: ${f.vhost}`)
    if (f.tlsEnabled) {
      lines.push('##     ssl:')
      lines.push('##       enabled: true')
    }
    lines.push('##     listener:')
    lines.push('##       simple:')
    lines.push('##         acknowledge-mode: manual')
    lines.push('##         prefetch: 10')
    lines.push('##         retry:')
    lines.push('##           enabled: true')
    lines.push('##           max-attempts: 3')

    cfgOutput.value = lines.join('\n')
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

<style lang="scss">
@import '../shared.scss';
</style>