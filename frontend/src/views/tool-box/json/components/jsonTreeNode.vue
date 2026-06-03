<template>
  <div class="tree-node">

    <!-- 节点行 -->
    <div class="node-line" :style="{ paddingLeft: depth * 16 + 'px' }">

      <!-- 折叠/展开按钮（对象和数组才有） -->
      <span
        v-if="isExpandable"
        class="toggle-btn"
        @click="toggle"
        :aria-label="expanded ? '折叠' : '展开'"
      >
        <el-icon :size="10">
          <ArrowRight v-if="!expanded" />
          <ArrowDown v-else />
        </el-icon>
      </span>
      <span v-else class="toggle-placeholder" />

      <!-- key 名 -->
      <span v-if="keyName !== null" class="node-key">"{{ keyName }}"<span class="colon">: </span></span>

      <!-- 值渲染 -->
      <!-- 对象 -->
      <template v-if="isObject">
        <span class="brace">&#123;</span>
        <span v-if="!expanded" class="collapsed-hint" @click="toggle">
          {{ objectKeys.length }} 个字段
          <span class="brace">&#125;</span>
        </span>
        <span v-else class="brace-hint">{{ objectKeys.length }} 个字段</span>
      </template>

      <!-- 数组 -->
      <template v-else-if="isArray">
        <span class="brace">[</span>
        <span v-if="!expanded" class="collapsed-hint" @click="toggle">
          {{ data.length }} 个元素
          <span class="brace">]</span>
        </span>
        <span v-else class="brace-hint">{{ data.length }} 个元素</span>
      </template>

      <!-- 字符串 -->
      <span v-else-if="typeof data === 'string'" class="val-string">"{{ data }}"</span>

      <!-- 数字 -->
      <span v-else-if="typeof data === 'number'" class="val-number">{{ data }}</span>

      <!-- 布尔 -->
      <span v-else-if="typeof data === 'boolean'" class="val-bool">{{ data }}</span>

      <!-- null -->
      <span v-else-if="data === null" class="val-null">null</span>

      <!-- 尾随逗号 -->
      <span v-if="!isLast" class="comma">,</span>

      <!-- 复制单个值按钮 -->
      <el-tooltip content="复制此值" placement="right" :show-after="500">
        <span class="copy-val-btn" @click.stop="copyValue" aria-label="复制此值">
          <el-icon :size="11"><CopyDocument /></el-icon>
        </span>
      </el-tooltip>
    </div>

    <!-- 子节点（展开时递归渲染） -->
    <template v-if="expanded">
      <!-- 对象的子字段 -->
      <template v-if="isObject">
        <JsonTreeNode
          v-for="(val, idx) in objectEntries"
          :key="val[0]"
          :data="val[1]"
          :key-name="val[0]"
          :depth="depth + 1"
          :is-last="idx === objectEntries.length - 1"
        />
        <div class="node-line close-brace" :style="{ paddingLeft: depth * 16 + 'px' }">
          <span class="toggle-placeholder" />
          <span class="brace">&#125;</span>
          <span v-if="!isLast" class="comma">,</span>
        </div>
      </template>

      <!-- 数组的子元素 -->
      <template v-if="isArray">
        <JsonTreeNode
          v-for="(item, idx) in data"
          :key="idx"
          :data="item"
          :key-name="idx"
          :depth="depth + 1"
          :is-last="idx === data.length - 1"
        />
        <div class="node-line close-brace" :style="{ paddingLeft: depth * 16 + 'px' }">
          <span class="toggle-placeholder" />
          <span class="brace">]</span>
          <span v-if="!isLast" class="comma">,</span>
        </div>
      </template>
    </template>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ArrowRight, ArrowDown, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const props = defineProps({
  data: { required: true },
  keyName: { default: null },
  depth: { type: Number, default: 0 },
  isLast: { type: Boolean, default: true }
})

/* ---- 类型判断 ---- */
const isObject = computed(() => props.data !== null && typeof props.data === 'object' && !Array.isArray(props.data))
const isArray = computed(() => Array.isArray(props.data))
const isExpandable = computed(() => isObject.value || isArray.value)

/* ---- 对象的 key 列表和条目 ---- */
const objectKeys = computed(() => isObject.value ? Object.keys(props.data) : [])
const objectEntries = computed(() => isObject.value ? Object.entries(props.data) : [])

/* ---- 折叠状态（depth <= 1 默认展开） ---- */
const expanded = ref(props.depth <= 1)

function toggle() {
  if (isExpandable.value) expanded.value = !expanded.value
}

/* ---- 复制单个值 ---- */
async function copyValue() {
  const text = typeof props.data === 'object'
    ? JSON.stringify(props.data, null, 2)
    : String(props.data)
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制')
  } catch {
    ElMessage.error('复制失败')
  }
}
</script>

<style scoped>
.tree-node {
  font-size: 12px;
}

.node-line {
  display: flex;
  align-items: center;
  min-height: 22px;
  gap: 2px;
  border-radius: 3px;
  transition: background 0.1s;

  &:hover {
    background: #f5f7fa;
  }

  &:hover .copy-val-btn {
    opacity: 1;
  }
}

/* 折叠按钮 */
.toggle-btn {
  width: 16px;
  height: 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #909399;
  flex-shrink: 0;
  border-radius: 3px;

  &:hover {
    background: #e4e7ed;
    color: #303133;
  }
}

.toggle-placeholder {
  width: 16px;
  flex-shrink: 0;
  display: inline-block;
}

/* key */
.node-key {
  color: #0C447C;
}

.colon {
  color: #606266;
}

/* 值类型颜色 */
.val-string {
  color: #085041;
  word-break: break-all;
}

.val-number {
  color: #534AB7;
}

.val-bool {
  color: #712B13;
}

.val-null {
  color: #909399;
  font-style: italic;
}

/* 括号 */
.brace {
  color: #303133;
  font-weight: 500;
}

.brace-hint {
  font-size: 10px;
  color: #c0c4cc;
  margin-left: 6px;
}

.comma {
  color: #606266;
}

/* 折叠状态的省略提示 */
.collapsed-hint {
  font-size: 11px;
  color: #909399;
  cursor: pointer;
  margin-left: 4px;
  padding: 0 4px;
  border-radius: 3px;
  background: #f5f7fa;

  &:hover {
    background: #ecf5ff;
    color: #409eff;
  }
}

/* 关闭括号行 */
.close-brace {
  padding-top: 0;
}

/* 复制按钮（hover 才显示） */
.copy-val-btn {
  opacity: 0;
  cursor: pointer;
  color: #c0c4cc;
  margin-left: 4px;
  display: inline-flex;
  align-items: center;
  transition: opacity 0.15s, color 0.15s;

  &:hover {
    color: #409eff;
  }
}
</style>