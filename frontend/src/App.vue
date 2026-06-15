<template>
  <el-container style="height:100vh">
    <el-header style="background:#409EFF;color:#fff;height:56px;line-height:56px;padding:0 16px">DataSync Hub</el-header>
    <el-container>
      <el-aside width="200px" style="background:#f5f7fa">
        <el-menu :default-active="$route.path" router>
          <el-menu-item index="/tasks">任务列表</el-menu-item>
          <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        </el-menu>
      </el-aside>
      <el-main style="padding:16px">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
</script>
