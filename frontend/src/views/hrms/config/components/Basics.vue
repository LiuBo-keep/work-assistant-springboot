<template>
  <div class="page-wrap" v-loading="loading">
    <div class="config-card">

      <!-- 卡片头 -->
      <div class="card-head">
        <div class="head-icon">
          <el-icon :size="14">
            <Setting/>
          </el-icon>
        </div>
        <span class="head-title">基础配置</span>
      </div>

      <!-- 表单体 -->
      <el-form
          ref="configFormRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="config-form"
      >
        <div class="form-grid">

          <!-- 是否启用 -->
          <div class="field-wrap">
            <div class="field-label">是否启用</div>
            <div class="switch-row">
              <el-switch v-model="form.enabled"/>
              <span class="switch-hint" :class="form.enabled ? 'on' : 'off'">
                {{ form.enabled ? '通知已开启' : '通知已关闭' }}
              </span>
            </div>
          </div>

          <!-- 通知渠道 -->
          <el-form-item prop="notifyChannel" class="field-wrap">
            <template #label><span class="field-label">通知渠道</span></template>
            <el-select
                v-model="form.notifyChannel"
                clearable
                placeholder="请选择通知渠道"
                style="width:100%"
            >
              <el-option
                  v-for="item in notifyChannelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
              />
            </el-select>
          </el-form-item>

          <!-- 刷卡记录地址 -->
          <el-form-item prop="hrmsUrl" class="field-wrap">
            <template #label><span class="field-label">刷卡记录地址</span></template>
            <el-input v-model="form.hrmsUrl" clearable placeholder="请输入刷卡记录地址"/>
          </el-form-item>

          <!-- 刷卡记录 Cookie -->
          <el-form-item prop="hrmsToken" class="field-wrap">
            <template #label><span class="field-label">刷卡记录 Cookie</span></template>
            <el-input
                v-model="form.hrmsToken"
                type="password"
                show-password
                clearable
                placeholder="请输入刷卡记录 Cookie"
            />
          </el-form-item>

          <!-- 微信渠道 -->
          <template v-if="isWechatChannel">
            <el-form-item prop="pushPlusUrl" class="field-wrap">
              <template #label><span class="field-label">PushPlus URL</span></template>
              <el-input v-model="form.pushPlusUrl" clearable placeholder="请输入 PushPlus URL"/>
            </el-form-item>
            <el-form-item prop="pushPlusToken" class="field-wrap">
              <template #label><span class="field-label">PushPlus Token</span></template>
              <el-input
                  v-model="form.pushPlusToken"
                  type="password"
                  show-password
                  clearable
                  placeholder="请输入 PushPlus Token"
              />
            </el-form-item>
          </template>

          <!-- 钉钉渠道 -->
          <template v-if="isDingTalkChannel">
            <el-form-item prop="dingTalkUrl" class="field-wrap">
              <template #label><span class="field-label">钉钉通知 URL</span></template>
              <el-input v-model="form.dingTalkUrl" clearable placeholder="请输入钉钉通知 URL"/>
            </el-form-item>
          </template>

        </div>

        <!-- 底部按钮 -->
        <div class="form-footer">
          <el-button @click="resetForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="save">保存配置</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElNotification} from 'element-plus'
import {Setting} from '@element-plus/icons-vue'
import systemService from '@/services/hrms/hrms-config/system.service'

const configFormRef = ref()
const loading = ref(false)

const NOTIFY_CHANNEL_OPTIONS = [
  {label: '微信', value: 'WECHAT'},
  {label: '钉钉', value: 'DING_TALK'}
]

const DEFAULT_FORM = {
  id: '', enabled: false, notifyChannel: '',
  pushPlusUrl: '', pushPlusToken: '',
  dingTalkUrl: '', hrmsUrl: '', hrmsToken: ''
}

const form = reactive({...DEFAULT_FORM})

const notifyChannelOptions = computed(() => NOTIFY_CHANNEL_OPTIONS)
const isWechatChannel = computed(() => form.notifyChannel === 'WECHAT')
const isDingTalkChannel = computed(() => form.notifyChannel === 'DING_TALK')

const rules = computed(() => ({
  notifyChannel: [{required: true, message: '请选择通知渠道', trigger: 'change'}],
  hrmsUrl: [{required: true, message: '刷卡记录地址必填', trigger: 'blur'}],
  hrmsToken: [{required: true, message: '刷卡记录 Cookie 必填', trigger: 'blur'}],
  pushPlusUrl: [{required: isWechatChannel.value, message: 'PushPlus URL 必填', trigger: 'blur'}],
  pushPlusToken: [{required: isWechatChannel.value, message: 'PushPlus Token 必填', trigger: 'blur'}],
  dingTalkUrl: [{required: isDingTalkChannel.value, message: '钉钉通知 URL 必填', trigger: 'blur'}]
}))

async function getConfig() {
  loading.value = true
  try {
    const res = await systemService.getConfig()
    if (res.code !== 200) return ElNotification.error({message: res.error || '获取配置失败'})
    if (res.data) Object.assign(form, DEFAULT_FORM, res.data)
  } catch (e) {
    ElNotification.error({message: e.message || '获取配置失败'})
  } finally {
    loading.value = false
  }
}

async function save() {
  const valid = await configFormRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const req = {...form}
    if (req.notifyChannel === 'WECHAT') req.dingTalkUrl = ''
    if (req.notifyChannel === 'DING_TALK') {
      req.pushPlusUrl = '';
      req.pushPlusToken = ''
    }
    const res = await systemService.save(req)
    if (res.code !== 200) return ElNotification.error({message: res.error || '保存失败'})
    ElNotification.success({message: '保存成功'})
    await getConfig()
  } catch (e) {
    ElNotification.error({message: e.message || '保存失败'})
  } finally {
    loading.value = false
  }
}

function resetForm() {
  configFormRef.value?.resetFields()
  Object.assign(form, DEFAULT_FORM)
}

onMounted(() => getConfig())
</script>

<style lang="scss" scoped>
/* ===== 页面容器 ===== */
.page-wrap {
  padding: 16px;
  background: #f5f7fa;
  min-height: 100%;
  box-sizing: border-box;
}

/* ===== 卡片 ===== */
.config-card {
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
}

/* 卡片头部 */
.card-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.head-icon {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: #e6f1fb;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #185fa5;
  flex-shrink: 0;
}

.head-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

/* ===== 表单主体 ===== */
.config-form {
  padding: 20px 24px 0;
}

/* 3 列网格 */
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0 24px;
}

/* 每个字段容器 */
.field-wrap {
  margin-bottom: 20px !important;

  /* 覆盖 el-form-item 默认 label 样式 */
  :deep(.el-form-item__label) {
    padding: 0 0 6px !important;
    line-height: 1 !important;
    height: auto !important;
  }
}

/* 字段 label */
.field-label {
  font-size: 13px;
  font-weight: 500;
  color: #606266;
}

/* 必填红星位置微调 */
:deep(.el-form-item.is-required:not(.is-no-asterisk)) {
  .el-form-item__label::before {
    margin-right: 3px;
  }
}

/* 统一输入框高度 */
:deep(.el-input__wrapper),
:deep(.el-select .el-input__wrapper) {
  height: 32px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;

  &:hover {
    box-shadow: 0 0 0 1px #c0c4cc inset;
  }

  &.is-focus {
    box-shadow: 0 0 0 1px #409eff inset;
  }
}

/* 开关行 */
.switch-row {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 32px;
}

.switch-hint {
  font-size: 12px;

  &.on {
    color: #0f6e56;
  }

  &.off {
    color: #909399;
  }
}

/* ===== 底部按钮 ===== */
.form-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 10px;
  padding: 16px 0 20px;
  border-top: 1px solid #f0f0f0;
  margin-top: 4px;
}

/* ===== 响应式 ===== */
@media screen and (max-width: 960px) {
  .form-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media screen and (max-width: 600px) {
  .config-form {
    padding: 16px 16px 0;
  }
  .form-grid {
    grid-template-columns: 1fr;
  }
  .form-footer {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>