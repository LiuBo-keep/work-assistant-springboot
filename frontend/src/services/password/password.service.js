import BaseService from '../baseservice'

/**
 * PasswordService — 服务端存储 + 非对称加密传输版
 *
 * 安全机制：
 * 1. 密码不再存 localStorage，统一由服务端落库（服务端 AES 加密存储）
 * 2. 传输采用双向 RSA 非对称加密：
 *    - 上传：客户端用「服务端公钥」加密密码 → 服务端私钥解密
 *    - 下发：服务端用「客户端公钥」加密密码 → 客户端私钥解密
 * 3. 客户端公钥为会话级临时 RSA 密钥对，仅存在于内存，页面关闭即失效
 * 4. 服务端公钥通过 /api/v1/password/public-key 动态下发
 */

const CLIENT_PUBLIC_KEY_HEADER = 'X-Client-Public-Key'

/**
 * 会话级客户端 RSA 密钥对（内存缓存，不落盘）
 */
let clientKeyPair = null

/* ================================================================
   内部工具
   ================================================================ */

function bytesToBase64(bytes) {
  let binary = ''
  bytes.forEach(b => { binary += String.fromCharCode(b) })
  return btoa(binary)
}

function base64ToBytes(b64) {
  return Uint8Array.from(atob(b64), c => c.charCodeAt(0))
}

/**
 * 生成会话级客户端 RSA 密钥对（2048，OAEP-SHA256）
 */
async function ensureClientKeyPair() {
  if (clientKeyPair) return clientKeyPair
  clientKeyPair = await crypto.subtle.generateKey(
    { name: 'RSA-OAEP', modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: 'SHA-256' },
    false,
    ['encrypt', 'decrypt']
  )
  return clientKeyPair
}

/**
 * 将客户端公钥导出为 Base64（SPKI），用于请求头传递
 */
async function exportClientPublicKey() {
  const keyPair = await ensureClientKeyPair()
  const exported = await crypto.subtle.exportKey('spki', keyPair.publicKey)
  return bytesToBase64(new Uint8Array(exported))
}

/**
 * 将服务端公钥（Base64 SPKI）导入为 CryptoKey
 */
async function importServerPublicKey(base64) {
  return crypto.subtle.importKey(
    'spki',
    base64ToBytes(base64),
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['encrypt']
  )
}

/**
 * 用服务端公钥加密密码明文，返回 Base64 密文
 */
async function encryptWithServerKey(plaintext) {
  const serverKeyB64 = await fetchServerPublicKey()
  const serverKey = await importServerPublicKey(serverKeyB64)
  const encrypted = await crypto.subtle.encrypt(
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    serverKey,
    new TextEncoder().encode(plaintext)
  )
  return bytesToBase64(new Uint8Array(encrypted))
}

/**
 * 用客户端私钥解密服务端下发的密码密文
 */
async function decryptWithClientKey(ciphertext) {
  const keyPair = await ensureClientKeyPair()
  const decrypted = await crypto.subtle.decrypt(
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    keyPair.privateKey,
    base64ToBytes(ciphertext)
  )
  return new TextDecoder().decode(decrypted)
}

/**
 * 服务端公钥缓存（Base64）
 */
let serverPublicKeyCache = null

async function fetchServerPublicKey() {
  if (serverPublicKeyCache) return serverPublicKeyCache
  const res = await new BaseService().get('/api/v1/password/public-key')
  if (res.code !== 200 || !res.data?.publicKey) {
    throw new Error('获取服务端公钥失败')
  }
  serverPublicKeyCache = res.data.publicKey
  return serverPublicKeyCache
}

/* ================================================================
   PasswordService
   ================================================================ */

class PasswordService extends BaseService {

  /**
   * 查询全部密码（密码字段为客户端公钥加密后的密文，需解密）
   */
  async getAll() {
    const clientPubKey = await exportClientPublicKey()
    const res = await this.get('/api/v1/password/list', {
      headers: { [CLIENT_PUBLIC_KEY_HEADER]: clientPubKey }
    })
    if (res.code !== 200) throw new Error(res.error || '查询失败')
    const list = res.data || []
    return Promise.all(list.map(async item => {
      let password = ''
      if (item.password) {
        try {
          password = await decryptWithClientKey(item.password)
        } catch {
          password = '[解密失败]'
        }
      }
      return { ...item, password }
    }))
  }

  /**
   * 搜索（本地过滤，先取全量解密）
   */
  async search({ keyword = '', category = '' } = {}) {
    const all = await this.getAll()
    const kw = keyword.trim().toLowerCase()
    return all.filter(item => {
      const matchKw = !kw || [item.name, item.account, item.url, item.remark]
        .some(f => (f || '').toLowerCase().includes(kw))
      const matchCat = !category || item.category === category
      return matchKw && matchCat
    })
  }

  /**
   * 新增
   */
  async add(data) {
    const clientPubKey = await exportClientPublicKey()
    const encryptedPwd = data.password ? await encryptWithServerKey(data.password) : ''
    const res = await this.post('/api/v1/password/save', {
      data: {
        name: data.name || '',
        category: data.category || '其他',
        account: data.account || '',
        password: encryptedPwd,
        url: data.url || '',
        remark: data.remark || '',
        clientPublicKey: clientPubKey
      }
    })
    if (res.code !== 200) return { code: res.code, error: res.error || '新增失败' }
    const item = res.data
    return { code: 200, data: { ...item, password: data.password } }
  }

  /**
   * 编辑
   */
  async update(id, data) {
    const clientPubKey = await exportClientPublicKey()
    const encryptedPwd = data.password ? await encryptWithServerKey(data.password) : ''
    const res = await this.post('/api/v1/password/save', {
      data: {
        id,
        name: data.name || '',
        category: data.category || '其他',
        account: data.account || '',
        password: encryptedPwd,
        url: data.url || '',
        remark: data.remark || '',
        clientPublicKey: clientPubKey
      }
    })
    if (res.code !== 200) return { code: res.code, error: res.error || '修改失败' }
    return { code: 200, data: { ...res.data, password: data.password } }
  }

  /**
   * 删除
   */
  async remove(id) {
    const res = await this.delete(`/api/v1/password/${id}`)
    return { code: res.code }
  }
}

export default new PasswordService()
