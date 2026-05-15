<template>
  <div class="dashboard-container">
    <el-card class="dashboard-card" shadow="never">
      <div class="title">
        工作助手
      </div>

      <ul class="menu-list">
        <li>消息通知</li>
        <li>任务管理</li>
      </ul>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'Dashboard'
}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
}

.dashboard-card {
  border-radius: 8px;
}

.title {
  margin-bottom: 16px;

  font-size: 18px;
  font-weight: 600;

  color: #303133;
}

.menu-list {
  padding-left: 20px;
}

.menu-list li {
  margin-bottom: 12px;

  font-size: 16px;

  color: #606266;
}
</style>