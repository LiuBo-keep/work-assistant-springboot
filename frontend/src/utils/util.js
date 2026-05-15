import dayjs from 'dayjs'
import { saveAs } from 'file-saver'
import browser from '../store/persistent/browser'

class Util {
  datetime(value) {
    if (!value) return ''

    return dayjs(value).format('YYYY-MM-DD HH:mm:ss')
  }

  nowDatetime() {
    return dayjs().format('YYYY-MM-DDTHH:mm:ss')
  }

  date(value) {
    if (!value) return ''

    return dayjs(value).format('YYYY-MM-DD')
  }

  time() {
    return Date.now()
  }

  getCurrentTime() {
    const now = new Date()

    const yy = now.getFullYear()
    const mm = this.timeAdd0(String(now.getMonth() + 1))
    const dd = this.timeAdd0(String(now.getDate()))
    const hh = this.timeAdd0(String(now.getHours()))
    const mf = this.timeAdd0(String(now.getMinutes()))
    const ss = this.timeAdd0(String(now.getSeconds()))

    return `${yy}${mm}${dd}${hh}${mf}${ss}`
  }

  timeAdd0(str) {
    return str.padStart(2, '0')
  }

  compare(start, end) {
    return dayjs(end).diff(dayjs(start), 'hour') > 0
  }

  saveFile(data, fileName) {
    saveAs(data, fileName)
  }

  convertBase64ToBlob(data) {
    const bstr = atob(data.replace(/\s/g, ''))
    let len = bstr.length

    const buffer = new ArrayBuffer(len)
    const u8arr = new Uint8Array(buffer)

    while (len--) {
      u8arr[len] = bstr.charCodeAt(len)
    }

    return new Blob([u8arr], {
      type: 'application/pdf'
    })
  }

  debounce(func, wait, immediate) {
    let timeout = null
    let result

    return (...args) => {
      const context = this

      const later = () => {
        timeout = null

        if (!immediate) {
          result = func.apply(context, args)
        }
      }

      const callNow = immediate && !timeout

      clearTimeout(timeout)

      timeout = setTimeout(later, wait)

      if (callNow) {
        result = func.apply(context, args)
      }

      return result
    }
  }

  setBaseUrl() {
    const pathname = window.location.pathname

    if (pathname === '/') {
      browser.setBaseURL(window.location.origin)
    } else {
      browser.setBaseURL(
        `${window.location.origin}${window.location.pathname}`
      )
    }
  }

  getBrowserType() {
    const browserInfo = navigator.userAgent.toLowerCase()

    if (browserInfo.includes('micromessenger')) {
      return 'WECHAT'
    }

    return 'NORMAL'
  }
}

export default new Util()