<template>
  <div class="config-page">
    <el-card class="config-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span class="card-title">通知配置</span>
        </div>
      </template>

      <el-form
        ref="configFormRef"
        :model="form"
        :rules="rules"
        label-width="140px"
        label-position="right"
        class="config-form"
      >
        <el-row :gutter="20">
          <!-- 是否启用 -->
          <el-col :xs="24" :sm="12" :md="12" :lg="8">
            <el-form-item label="是否启用" prop="enabled">
              <el-switch v-model="form.enabled" />
            </el-form-item>
          </el-col>

          <!-- 通知渠道 -->
          <el-col :xs="24" :sm="12" :md="12" :lg="8">
            <el-form-item label="通知渠道" prop="notifyChannel">
              <el-select
                v-model="form.notifyChannel"
                clearable
                placeholder="请选择通知渠道"
                style="width: 100%"
              >
                <el-option
                  v-for="item in notifyChannelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>

          <!-- HRMS URL -->
          <el-col :xs="24" :sm="12" :md="12" :lg="8">
            <el-form-item label="刷卡记录地址" prop="hrmsUrl">
              <el-input
                v-model="form.hrmsUrl"
                clearable
                placeholder="请输入 刷卡记录地址"
              />
            </el-form-item>
          </el-col>

          <!-- HRMS TOKEN -->
          <el-col :xs="24" :sm="12" :md="12" :lg="8">
            <el-form-item label="刷卡记录Cookie" prop="hrmsToken">
              <el-input
                v-model="form.hrmsToken"
                type="password"
                show-password
                clearable
                placeholder="刷卡记录Cookie"
              />
            </el-form-item>
          </el-col>

          <!-- 微信配置 -->
          <template v-if="isWechatChannel">
            <el-col :xs="24" :sm="12" :md="12" :lg="8">
              <el-form-item label="PushPlus URL" prop="pushPlusUrl">
                <el-input
                  v-model="form.pushPlusUrl"
                  clearable
                  placeholder="请输入 PushPlus URL"
                />
              </el-form-item>
            </el-col>

            <el-col :xs="24" :sm="12" :md="12" :lg="8">
              <el-form-item label="PushPlus Token" prop="pushPlusToken">
                <el-input
                  v-model="form.pushPlusToken"
                  type="password"
                  show-password
                  clearable
                  placeholder="请输入 PushPlus Token"
                />
              </el-form-item>
            </el-col>
          </template>

          <!-- 钉钉配置 -->
          <template v-if="isDingTalkChannel">
            <el-col :xs="24" :sm="12" :md="12" :lg="8">
              <el-form-item label="钉钉通知 URL" prop="dingTalkUrl">
                <el-input
                  v-model="form.dingTalkUrl"
                  clearable
                  placeholder="请输入钉钉通知 URL"
                />
              </el-form-item>
            </el-col>
          </template>
        </el-row>

        <!-- 按钮 -->
        <div class="form-footer">
          <el-button
            type="primary"
            :loading="loading"
            @click="save"
          >
            保存配置
          </el-button>

          <el-button @click="resetForm">
            重置
          </el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElNotification } from 'element-plus'
import systemService from '@/services/hrms/hrms-config/system.service'

const configFormRef = ref()

const loading = ref(false)

const NOTIFY_CHANNEL_OPTIONS = [
  {
    label: '微信',
    value: 'WECHAT'
  },
  {
    label: '钉钉',
    value: 'DING_TALK'
  }
]

const DEFAULT_FORM = {
  id: '',
  enabled: false,
  notifyChannel: '',
  pushPlusUrl: '',
  pushPlusToken: '',
  dingTalkUrl: '',
  hrmsUrl: '',
  hrmsToken: ''
}

const form = reactive({
  ...DEFAULT_FORM
})

/**
 * 通知渠道选项
 */
const notifyChannelOptions = computed(() => {
  return NOTIFY_CHANNEL_OPTIONS
})

/**
 * 是否微信渠道
 */
const isWechatChannel = computed(() => {
  return form.notifyChannel === 'WECHAT'
})

/**
 * 是否钉钉渠道
 */
const isDingTalkChannel = computed(() => {
  return form.notifyChannel === 'DING_TALK'
})

/**
 * 表单校验
 */
const rules = computed(() => {
  return {
    notifyChannel: [
      {
        required: true,
        message: '请选择通知渠道',
        trigger: 'change'
      }
    ],

    hrmsUrl: [
      {
        required: true,
        message: 'HRMS URL 必填',
        trigger: 'blur'
      }
    ],

    hrmsToken: [
      {
        required: true,
        message: 'HRMS Token 必填',
        trigger: 'blur'
      }
    ],

    pushPlusUrl: [
      {
        required: isWechatChannel.value,
        message: 'PushPlus URL 必填',
        trigger: 'blur'
      }
    ],

    pushPlusToken: [
      {
        required: isWechatChannel.value,
        message: 'PushPlus Token 必填',
        trigger: 'blur'
      }
    ],

    dingTalkUrl: [
      {
        required: isDingTalkChannel.value,
        message: '钉钉通知 URL 必填',
        trigger: 'blur'
      }
    ]
  }
})

/**
 * 获取配置
 */
const getConfig = async () => {
  loading.value = true

  try {
    const response = await systemService.getConfig()

    if (response.code !== 200) {
      ElNotification.error({
        message: response.error || '获取配置失败'
      })
      return
    }

    if (response.data) {
      Object.assign(form, DEFAULT_FORM, response.data)
    }
  } catch (error) {
    ElNotification.error({
      message: error.message || '获取配置失败'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 保存配置
 */
const save = async () => {
  try {
    const valid = await configFormRef.value.validate().catch(() => false)

    if (!valid) {
      return
    }

    loading.value = true

    const request = {
      ...form
    }

    // 微信渠道清空钉钉配置
    if (request.notifyChannel === 'WECHAT') {
      request.dingTalkUrl = ''
    }

    // 钉钉渠道清空微信配置
    if (request.notifyChannel === 'DING_TALK') {
      request.pushPlusUrl = ''
      request.pushPlusToken = ''
    }

    const response = await systemService.save(request)

    if (response.code !== 200) {
      ElNotification.error({
        message: response.error || '保存失败'
      })
      return
    }

    ElNotification.success({
      message: '保存成功'
    })

    await getConfig()
  } catch (error) {
    ElNotification.error({
      message: error.message || '保存失败'
    })
  } finally {
    loading.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  configFormRef.value?.resetFields()

  Object.assign(form, DEFAULT_FORM)
}

onMounted(() => {
  getConfig()
})
</script>

<style scoped lang="scss">
.config-page {
  min-height: 100%;
  padding: 16px;
  box-sizing: border-box;
  background: #f5f7fa;
}

.config-card {
  border-radius: 8px;

  :deep(.el-card__header) {
    padding: 18px 20px;
    border-bottom: 1px solid #ebeef5;
  }

  :deep(.el-card__body) {
    padding: 24px;
  }
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.config-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
    color: #606266;
  }
}

.form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 12px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

@media screen and (max-width: 768px) {
  .config-page {
    padding: 12px;
  }

  .config-card {
    :deep(.el-card__body) {
      padding: 16px;
    }
  }

  .form-footer {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>