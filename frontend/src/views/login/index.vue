<template>
  <el-container class="login-container">
    <div class="login-main">
      <el-form
        :model="loginForm"
        label-position="top"
        @keydown.enter.prevent="login"
      >
        <el-form-item label="用户名">
          <el-input
            v-model="loginForm.userName"
            placeholder="用户名"
            clearable
          >
            <template #prefix>
              <el-icon>
                <User />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="密码">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="密码"
            show-password
            clearable
          >
            <template #prefix>
              <el-icon>
                <Lock />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            :disabled="disableLogin"
            class="login-btn"
            @click="login"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </el-container>
</template>

<script>
import { User, Lock } from '@element-plus/icons-vue'
import { ElNotification } from 'element-plus'

import browser from '../../store/persistent/browser'
import userService from '@/services/user/user.service.js'

export default {
  name: 'Login',

  components: {
    User,
    Lock
  },

  data() {
    return {
      loginForm: {
        userName: '',
        password: ''
      },

      loading: false
    }
  },

  computed: {
    disableLogin() {
      return !this.loginForm.userName || !this.loginForm.password
    }
  },

  methods: {
    async login() {
      if (this.disableLogin) {
        return
      }

      try {
        this.loading = true

        const response = await userService.login({
          userName: this.loginForm.userName,
          password: this.loginForm.password
        })

        if (response.code !== 200) {
          ElNotification.error({
            message: response.error
          })

          return
        }

        browser.setUsername(this.loginForm.userName)

        browser.setAccessToken(response.data.token)

        const currentTime = this.$util.nowDatetime()

        browser.setcCurrentTime(currentTime)

        const redirect = this.$route.query.redirect || '/'

        this.$router.push({
          path: decodeURIComponent(redirect)
        })
      } catch (e) {
        console.error(e)

        ElNotification.error({
          message: e.message || '登录失败'
        })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
:deep(.el-form-item__label) {
  height: 20px;
  line-height: 20px;
}

.login-container {
  width: 100%;
  height: 100vh;

  overflow: hidden;

  display: flex;
  align-items: center;
  justify-content: center;

  position: relative;

  background: url('@/assets/images/login-bg.jpg') no-repeat center center;

  background-size: cover;
}

.login-main {
  width: 450px;

  padding: 25px 48px;

  overflow: hidden;

  border-radius: 6px;

  background-color: #ffffff;

  box-shadow: 0 1px 4px rgb(0 0 0 / 50%);
}

:deep(.el-button) {
  width: 100%;

  font-size: 16px;

  background-color: #1e90ff;
}

.login-logo {
  width: 84px;
  height: 44px;
}

a {
  font-size: 13px;
}

a:link,
a:visited,
a:hover,
a:active {
  color: #0c3c6b;
}

.footer {
  margin-top: 20px;

  font-size: 11px;
}

:deep(.el-form) {
  margin-top: 30px;
}
</style>