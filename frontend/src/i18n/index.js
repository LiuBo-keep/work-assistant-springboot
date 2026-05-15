import { createI18n } from 'vue-i18n'
import browser from '../store/persistent/browser'

import zhCN from './zh-CN'
import en from './en'

const i18n = createI18n({
  legacy: false,
  locale: browser.language || 'zh-CN',
  fallbackLocale: 'zh-CN',
  globalInjection: true,
  messages: {
    'zh-CN': zhCN,
    en
  }
})

export default i18n