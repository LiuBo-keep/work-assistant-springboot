<template>
  <div class="json-tree-root">
    <JsonTreeNode :data="data" :key-name="null" :depth="0" :is-last="true" />
  </div>
</template>

<script setup>
import JsonTreeNode from './jsonTreeNode.vue'

defineProps({
  data: { required: true }
})
</script>

<style scoped>
.json-tree-root {
  font-family: 'Cascadia Code', 'Fira Code', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.8;
}
</style>