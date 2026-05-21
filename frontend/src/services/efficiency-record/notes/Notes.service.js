/**
 * NotesService — 工作笔记本地存储服务
 * 数据存于 localStorage，key: wa.notes
 */

const STORAGE_KEY = 'wa.notes'

function load() {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]')
  } catch {
    return []
  }
}

function save(list) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(list))
}

function genId() {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 7)
}

function now() {
  return new Date().toISOString()
}

function formatDate(iso) {
  const d = new Date(iso)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

function relativeTime(iso) {
  const diff = Date.now() - new Date(iso).getTime()
  const min = Math.floor(diff / 60000)
  const hour = Math.floor(diff / 3600000)
  const day = Math.floor(diff / 86400000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  if (hour < 24) return `${hour} 小时前`
  if (day === 1) return '昨天'
  if (day < 7) return `${day} 天前`
  return formatDate(iso).slice(5)  // 超过 7 天显示 MM-DD
}

class NotesService {

  /** 获取全部，按更新时间倒序 */
  getAll() {
    return load().sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
  }

  /** 搜索：标题 + 内容 + 分类 */
  search({ keyword = '', category = '' } = {}) {
    const kw = keyword.trim().toLowerCase()
    return this.getAll().filter(item => {
      const matchKw = !kw || item.title.toLowerCase().includes(kw) ||
        item.content.toLowerCase().includes(kw)
      const matchCat = !category || item.category === category
      return matchKw && matchCat
    })
  }

  /** 新增 */
  add(data) {
    const list = load()
    const record = {
      id: genId(),
      title: data.title || '无标题',
      content: data.content || '',
      category: data.category || '其他',
      createdAt: now(),
      updatedAt: now()
    }
    list.unshift(record)
    save(list)
    return { code: 200, data: record }
  }

  /** 更新 */
  update(id, data) {
    const list = load()
    const idx = list.findIndex(i => i.id === id)
    if (idx === -1) return { code: 404, error: '笔记不存在' }
    list[idx] = {
      ...list[idx],
      title: data.title ?? list[idx].title,
      content: data.content ?? list[idx].content,
      category: data.category ?? list[idx].category,
      updatedAt: now()
    }
    save(list)
    return { code: 200, data: list[idx] }
  }

  /** 删除 */
  remove(id) {
    save(load().filter(i => i.id !== id))
    return { code: 200 }
  }

  /** 统计字数（去除 Markdown 标记） */
  static wordCount(content) {
    return content
      .replace(/```[\s\S]*?```/g, '')   // 代码块
      .replace(/`[^`]+`/g, '')          // 行内代码
      .replace(/#{1,6}\s/g, '')         // 标题
      .replace(/[*_~>\[\]!#\-]/g, '')   // 特殊字符
      .replace(/\s+/g, '')              // 空白
      .length
  }

  /** 工具：相对时间 */
  static relativeTime(iso) {
    return relativeTime(iso)
  }

  /** 工具：格式化日期 */
  static formatDate(iso) {
    return formatDate(iso)
  }

  /** 内容预览（取正文前 50 字，去掉 Markdown 符号） */
  static preview(content) {
    return content
      .replace(/```[\s\S]*?```/g, '[代码]')
      .replace(/#{1,6}\s/g, '')
      .replace(/[*_~>`\[\]!]/g, '')
      .replace(/\n+/g, ' ')
      .trim()
      .slice(0, 50)
  }
}

export default new NotesService()