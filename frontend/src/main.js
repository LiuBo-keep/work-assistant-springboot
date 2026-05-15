import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import './assets/fonts/iconfont.css'
import i18n from './i18n/index'
import notify from './utils/notify'
import util from './utils/util'
import autoFocus from './directives/autoFocus'
import '@vant/touch-emulator'
import 'vant/lib/index.css'
import '@toast-ui/editor/dist/toastui-editor.css'
import './assets/css/main.css';
import './assets/css/color-dark.css';
import {
  Form,
  Field,
  Row,
  Col,
  Dialog,
  Icon,
  Tabs,
  Tab,
  Cell,
  List,
  RadioGroup,
  Radio,
  Divider,
  Image as VanImage
} from 'vant'

const app = createApp(App)

app.use(router)
app.use(i18n)
app.use(ElementPlus, {
  size: 'small',
  locale: i18n.global.getLocaleMessage(i18n.global.locale.value)
})

// Vant 组件注册
app.use(Form)
app.use(Field)
app.use(Row)
app.use(Col)
app.use(Dialog)
app.use(Icon)
app.use(Tabs)
app.use(Tab)
app.use(Cell)
app.use(List)
app.use(RadioGroup)
app.use(Radio)
app.use(Divider)
app.use(VanImage)

// 全局自定义指令
app.directive('focus', autoFocus)

// 全局属性（替代 Vue.prototype）
app.config.globalProperties.$notify = notify
app.config.globalProperties.$util = util

/**
 * 全局统一处理error
 * @param {function} action 执行函数
 * @param {function} failed 执行失败回调
 */
app.config.globalProperties.$execute = async function(action, failed) {
  try {
    await action()
  } catch (error) {
    this.$notify.error(error.stack)
    if (failed) {
      failed()
    }
  }
}

util.setBaseUrl()
app.mount('#app')
