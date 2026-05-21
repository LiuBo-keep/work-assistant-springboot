/**
 * PasswordService — AES-256-GCM 加密版
 *
 * 安全机制：
 * 1. 主密码通过 PBKDF2 派生 AES-256 密钥，密钥只在内存中存在
 * 2. 每条密码字段独立 IV（随机12字节），防止相同明文产生相同密文
 * 3. GCM 模式自带 AuthTag，解密前验证完整性，防篡改
 * 4. localStorage 只存 {iv, ciphertext, authTag} 三段密文，明文不落盘
 * 5. 调用 lock() 或页面关闭后，内存中的密钥自动消失
 */

const STORAGE_KEY = 'wa.passwords'
const SALT_KEY    = 'wa.vault.salt'
const VERIFY_KEY  = 'wa.vault.verify'
const PBKDF2_ITER = 120000
const VERIFY_TEXT = 'wa-vault-ok'

/* ================================================================
   内部工具
   ================================================================ */

/** 获取或生成盐（持久化，用于每次用同一主密码派生同一密钥） */
function getSalt() {
  const stored = localStorage.getItem(SALT_KEY)
  if (stored) return base64ToBytes(stored)
  const salt = crypto.getRandomValues(new Uint8Array(16))
  localStorage.setItem(SALT_KEY, bytesToBase64(salt))
  return salt
}

/** PBKDF2 派生 AES-256-GCM 密钥 */
async function deriveKey(masterPassword, salt) {
  const keyMaterial = await crypto.subtle.importKey(
    'raw',
    new TextEncoder().encode(masterPassword),
    'PBKDF2',
    false,
    ['deriveKey']
  )
  return crypto.subtle.deriveKey(
    { name: 'PBKDF2', salt, iterations: PBKDF2_ITER, hash: 'SHA-256' },
    keyMaterial,
    { name: 'AES-GCM', length: 256 },
    false,
    ['encrypt', 'decrypt']
  )
}

/** AES-256-GCM 加密，返回 base64 字符串 "iv:ciphertext" */
async function encryptField(key, plaintext) {
  const iv        = crypto.getRandomValues(new Uint8Array(12))
  const encoded   = new TextEncoder().encode(plaintext)
  const cipherBuf = await crypto.subtle.encrypt({ name: 'AES-GCM', iv }, key, encoded)
  return `${bytesToBase64(iv)}:${bytesToBase64(new Uint8Array(cipherBuf))}`
}

/** AES-256-GCM 解密，输入 "iv:ciphertext" 格式 */
async function decryptField(key, encrypted) {
  const [ivB64, ctB64] = encrypted.split(':')
  const iv        = base64ToBytes(ivB64)
  const cipherBuf = base64ToBytes(ctB64)
  const plainBuf  = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, cipherBuf)
  return new TextDecoder().decode(plainBuf)
}

function bytesToBase64(bytes) {
  return btoa(String.fromCharCode(...bytes))
}

function base64ToBytes(b64) {
  return Uint8Array.from(atob(b64), c => c.charCodeAt(0))
}

function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

function today() {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  }).replace(/\//g, '-')
}

function loadRaw() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') }
  catch { return [] }
}

function saveRaw(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

/* ================================================================
   PasswordService
   ================================================================ */

class PasswordService {
  constructor() {
    this._key = null  // 内存中的 CryptoKey，lock() 后置 null
  }

  /* ---- 主密码管理 ---- */

  /** 是否已设置主密码（有 salt + verify 记录） */
  hasVault() {
    return !!localStorage.getItem(VERIFY_KEY)
  }

  /** 是否已解锁（密钥在内存中） */
  isUnlocked() {
    return this._key !== null
  }

  /**
   * 首次设置主密码，生成 salt，派生密钥，写入校验串
   * @returns {Promise<{code:number, error?:string}>}
   */
  async setupMasterPassword(masterPassword) {
    try {
      const salt      = getSalt()
      const key       = await deriveKey(masterPassword, salt)
      const verifyEnc = await encryptField(key, VERIFY_TEXT)
      localStorage.setItem(VERIFY_KEY, verifyEnc)
      this._key = key
      return { code: 200 }
    } catch (e) {
      return { code: 500, error: e.message }
    }
  }

  /**
   * 解锁：用主密码派生密钥并验证
   * @returns {Promise<{code:number, error?:string}>}
   */
  async unlock(masterPassword) {
    try {
      const salt        = getSalt()
      const key         = await deriveKey(masterPassword, salt)
      const verifyEnc   = localStorage.getItem(VERIFY_KEY)
      if (!verifyEnc) return { code: 400, error: '未设置主密码' }
      const verifyPlain = await decryptField(key, verifyEnc)
      if (verifyPlain !== VERIFY_TEXT) return { code: 401, error: '主密码错误' }
      this._key = key
      return { code: 200 }
    } catch {
      return { code: 401, error: '主密码错误' }
    }
  }

  /** 锁定：清除内存中的密钥 */
  lock() {
    this._key = null
  }

  /** 修改主密码：用新密码重加密所有条目 */
  async changeMasterPassword(oldPwd, newPwd) {
    const unlockRes = await this.unlock(oldPwd)
    if (unlockRes.code !== 200) return unlockRes
    try {
      const allDecrypted = await this.getAll()
      const salt         = getSalt()
      const newKey       = await deriveKey(newPwd, salt)
      const newList      = await Promise.all(
        allDecrypted.map(async item => ({
          ...item,
          password: await encryptField(newKey, item.password)
        }))
      )
      saveRaw(newList)
      const verifyEnc = await encryptField(newKey, VERIFY_TEXT)
      localStorage.setItem(VERIFY_KEY, verifyEnc)
      this._key = newKey
      return { code: 200 }
    } catch (e) {
      return { code: 500, error: e.message }
    }
  }

  /* ---- CRUD ---- */

  /** 获取全部（解密密码字段） */
  async getAll() {
    if (!this._key) return []
    const raw = loadRaw()
    return Promise.all(
      raw.map(async item => {
        try {
          return { ...item, password: await decryptField(this._key, item.password) }
        } catch {
          return { ...item, password: '[解密失败]' }
        }
      })
    )
  }

  /** 新增 */
  async add(data) {
    if (!this._key) return { code: 401, error: '请先解锁密码库' }
    try {
      const list   = loadRaw()
      const record = {
        id:         genId(),
        name:       data.name     || '',
        category:   data.category || '其他',
        account:    data.account  || '',
        password:   await encryptField(this._key, data.password || ''),
        url:        data.url      || '',   // 网站地址（可选，明文存储）
        remark:     data.remark   || '',
        createDate: today(),
        updateDate: today()
      }
      list.unshift(record)
      saveRaw(list)
      return { code: 200, data: { ...record, password: data.password } }
    } catch (e) {
      return { code: 500, error: e.message }
    }
  }

  /** 编辑 */
  async update(id, data) {
    if (!this._key) return { code: 401, error: '请先解锁密码库' }
    try {
      const list = loadRaw()
      const idx  = list.findIndex(i => i.id === id)
      if (idx === -1) return { code: 404, error: '记录不存在' }
      list[idx] = {
        ...list[idx],
        name:       data.name     ?? list[idx].name,
        category:   data.category ?? list[idx].category,
        account:    data.account  ?? list[idx].account,
        password:   await encryptField(this._key, data.password || ''),
        url:        data.url      ?? list[idx].url ?? '',  // 兼容旧数据无 url 字段
        remark:     data.remark   ?? list[idx].remark,
        updateDate: today()
      }
      saveRaw(list)
      return { code: 200, data: { ...list[idx], password: data.password } }
    } catch (e) {
      return { code: 500, error: e.message }
    }
  }

  /** 删除 */
  remove(id) {
    saveRaw(loadRaw().filter(i => i.id !== id))
    return { code: 200 }
  }

  /** 搜索（本地过滤，需先解密） */
  async search({ keyword = '', category = '' } = {}) {
    const all = await this.getAll()
    const kw  = keyword.trim().toLowerCase()
    return all.filter(item => {
      const matchKw  = !kw || [item.name, item.account, item.url, item.remark]
        .some(f => (f || '').toLowerCase().includes(kw))
      const matchCat = !category || item.category === category
      return matchKw && matchCat
    })
  }

  /* ---- 导出 / 导入 ---- */

  /**
   * 导出加密备份文件（密文直接导出，无需解密再加密）
   * 文件格式：{ version, salt, verify, data: [...] }
   */
  exportBackup() {
    const backup = {
      version: 1,
      salt:    localStorage.getItem(SALT_KEY)   || '',
      verify:  localStorage.getItem(VERIFY_KEY) || '',
      data:    loadRaw()
    }
    const blob = new Blob(
      [JSON.stringify(backup, null, 2)],
      { type: 'application/json' }
    )
    const url  = URL.createObjectURL(blob)
    const a    = document.createElement('a')
    a.href     = url
    a.download = `password-vault-${Date.now()}.json`
    a.click()
    URL.revokeObjectURL(url)
    return { code: 200 }
  }

  /**
   * 导入备份文件（替换当前数据）
   * @param {string} jsonText — 备份文件内容
   * @param {string} masterPassword — 验证备份文件的主密码
   */
  async importBackup(jsonText, masterPassword) {
    try {
      const backup = JSON.parse(jsonText)
      if (!backup.version || !backup.salt || !backup.verify || !backup.data) {
        return { code: 400, error: '备份文件格式不正确' }
      }
      const salt        = base64ToBytes(backup.salt)
      const key         = await deriveKey(masterPassword, salt)
      const verifyPlain = await decryptField(key, backup.verify)
      if (verifyPlain !== VERIFY_TEXT) return { code: 401, error: '主密码错误，导入失败' }

      localStorage.setItem(SALT_KEY,   backup.salt)
      localStorage.setItem(VERIFY_KEY, backup.verify)
      saveRaw(backup.data)
      this._key = key
      return { code: 200 }
    } catch (e) {
      return { code: 500, error: `导入失败：${e.message}` }
    }
  }
}

export default new PasswordService()