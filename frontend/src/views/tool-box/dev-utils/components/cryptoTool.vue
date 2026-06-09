<template>
  <div class="panel-wrap">
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
                <button v-for="k in [128,192,256]" :key="k" :class="{ active: aesKeyLen === k }" @click="aesKeyLen = k">
                  {{ k }}位
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
            <textarea class="field-input mono" style="width:100%;resize:vertical;min-height:80px;box-sizing:border-box"
                      v-model="cryptoInput" placeholder="输入内容..." />
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
            <pre class="code-pre" style="font-size:10px;line-height:1.5;max-height:140px;overflow-y:auto">{{ rsaKeys.pub
              }}</pre>
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
</template>

<script setup>
import { ref } from 'vue'

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

</script>

<style lang="scss">
@import '../shared.scss';
</style>