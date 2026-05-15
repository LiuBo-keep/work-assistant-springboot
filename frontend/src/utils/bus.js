import mitt from 'mitt'

// 使用 mitt 替代 Vue 2 的 Event Bus
const bus = mitt()

export default bus
