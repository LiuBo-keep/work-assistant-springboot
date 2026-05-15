// Vue 3 自定义指令：inserted → mounted
export default {
  mounted(el) {
    if (el.tagName.toLocaleLowerCase() == 'input') {
      el.focus()
    } else {
      if (el.getElementsByTagName('input')) {
        el.getElementsByTagName('input')[0].focus()
      }
    }
  }
}
