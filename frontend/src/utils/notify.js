import { ElMessageBox, ElNotification } from 'element-plus'
import i18n from '../i18n'

class Notify {
  success(message) {
    ElNotification.closeAll()
    ElNotification({
      title: i18n.global.t(message),
      type: 'success'
    })
  }
  error(message) {
    ElNotification.closeAll()
    ElNotification({
      title: i18n.global.t(message),
      type: 'error'
    })
  }
  warn(message) {
    ElNotification.closeAll()
    ElNotification({
      title: i18n.global.t(message),
      type: 'warning'
    })
  }
  confirm(message) {
    return ElMessageBox.confirm(message, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        return true
      })
      .catch(() => {
        return false
      })
  }
}
export default new Notify()
