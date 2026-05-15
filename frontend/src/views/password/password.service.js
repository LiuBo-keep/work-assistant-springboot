/**
 * PasswordService
 * 数据存于 localStorage，key: wa.passwords
 * 密码字段使用 btoa/atob 简单混淆（生产环境建议对接后端加密接口）
 */

const STORAGE_KEY = 'wa.passwords'

function load() {
  try { return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]') }
  catch { return [] }
}

function save(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

function encode(str) {
  try { return btoa(unescape(encodeURIComponent(str))) } catch { return str }
}

function decode(str) {
  try { return decodeURIComponent(escape(atob(str))) } catch { return str }
}

function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

function today() {
  return new Date().toLocaleDateString('zh-CN', { year:'numeric', month:'2-digit', day:'2-digit' })
    .replace(/\//g, '-')
}

class PasswordService {
  getAll() {
    return load().map(item => ({ ...item, password: decode(item.password) }))
  }

  add(data) {
    const list   = load()
    const record = {
      id:         genId(),
      name:       data.name       || '',
      category:   data.category   || '其他',
      account:    data.account    || '',
      password:   encode(data.password || ''),
      remark:     data.remark     || '',
      createDate: today(),
      updateDate: today()
    }
    list.unshift(record)
    save(list)
    return { code: 200, data: { ...record, password: data.password } }
  }

  update(id, data) {
    const list = load()
    const idx  = list.findIndex(i => i.id === id)
    if (idx === -1) return { code: 404, error: '记录不存在' }
    list[idx] = {
      ...list[idx],
      name:       data.name       ?? list[idx].name,
      category:   data.category   ?? list[idx].category,
      account:    data.account    ?? list[idx].account,
      password:   encode(data.password || ''),
      remark:     data.remark     ?? list[idx].remark,
      updateDate: today()
    }
    save(list)
    return { code: 200, data: { ...list[idx], password: data.password } }
  }

  remove(id) {
    save(load().filter(i => i.id !== id))
    return { code: 200 }
  }

  search({ keyword = '', category = '' } = {}) {
    const kw = keyword.trim().toLowerCase()
    return this.getAll().filter(item => {
      const matchKw  = !kw || [item.name, item.account, item.remark]
        .some(f => f.toLowerCase().includes(kw))
      const matchCat = !category || item.category === category
      return matchKw && matchCat
    })
  }
}

export default new PasswordService()