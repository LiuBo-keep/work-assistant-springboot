<template>
  <div class="error-page">
    <div class="error-code">4<span>0</span>4</div>
    <div class="error-desc">
      啊哦~ 你所访问的页面不存在
    </div>
    <div class="error-handle">
      <router-link to="/">
        <el-button type="primary" round>
          返回首页
        </el-button>
      </router-link>
      <el-button class="error-btn" round @click="goBack">
        返回上一页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

// 路由
const router = useRouter()

// 返回上一页
const goBack = () => {
  router.go(-1)
}
</script>

<style scoped>
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
  box-sizing: border-box;
}

.error-code {
  font-size: 10rem;
  color: #2d8cf0;
}

.error-code span {
  color: #00a854;
}

.error-desc {
  font-size: 20px;
  color: #777;
}

.error-handle {
  margin-top: 30px;
}

.error-btn {
  margin-left: 20px;
}
</style>
