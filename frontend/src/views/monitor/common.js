/**
 * 系统监控模块共享工具。
 *
 * <p>抽取各监控子页面共用的格式化函数、图表主题适配函数与指标元信息，
 * 避免在多个子页面间重复实现。
 */

/* ---- 格式化工具 ---- */
export function formatBytes(bytes) {
  if (bytes == null) return '--'
  if (bytes === 0) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return (bytes / Math.pow(1024, i)).toFixed(1) + ' ' + units[i]
}

export function formatMs(ms) {
  if (ms == null) return '--'
  if (ms < 1000) return ms + ' ms'
  return (ms / 1000).toFixed(1) + ' s'
}

export function formatUptime(ms) {
  if (ms == null) return '--'
  const totalSec = Math.floor(ms / 1000)
  const days = Math.floor(totalSec / 86400)
  const hours = Math.floor((totalSec % 86400) / 3600)
  const minutes = Math.floor((totalSec % 3600) / 60)
  if (days > 0) return `${days}天 ${hours}小时`
  if (hours > 0) return `${hours}小时 ${minutes}分`
  return `${minutes}分`
}

export function formatTime(str) {
  if (!str) return '--'
  return str.replace('T', ' ').substring(0, 19)
}

/* ---- 图表主题适配 ---- */
export function isDark() {
  return document.documentElement.classList.contains('dark')
}

export function chartTextColor() {
  return isDark() ? '#8b95a1' : '#909399'
}

export function chartAxisLineColor() {
  return isDark() ? '#2a323d' : '#e4e7ed'
}

/* ---- 指标元信息（用于趋势图自适应单位/颜色） ---- */
export const METRIC_META = {
  'heap.used': { label: '堆内存', unit: 'MB', color: '#378add', scale: 1024 * 1024 },
  'nonheap.used': { label: '非堆内存', unit: 'MB', color: '#9b6cf0', scale: 1024 * 1024 },
  'thread.count': { label: '线程数', unit: '个', color: '#1d9e75', scale: 1 },
  'process.cpu': { label: 'CPU', unit: '%', color: '#e24b4a', scale: 1 },
  'db.active': { label: 'DB 活跃连接', unit: '个', color: '#ba7517', scale: 1 }
}

export const METRIC_OPTIONS = [
  { value: 'heap.used', label: '堆内存' },
  { value: 'nonheap.used', label: '非堆内存' },
  { value: 'thread.count', label: '线程数' },
  { value: 'process.cpu', label: 'CPU' },
  { value: 'db.active', label: 'DB 连接' }
]
