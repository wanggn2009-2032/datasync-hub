<template>
  <div>
    <h3>仪表盘（占位）</h3>
    <p>任务总数：{{stats.total}}</p>
    <p>运行中：{{stats.running}}</p>
    <p>成功：{{stats.success}}</p>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
const stats = ref({ total: 0, running: 0, success: 0 })

onMounted(() => {
  // placeholder: later fetch aggregated stats from backend
  stats.value = { total: 10, running: 1, success: 8 }
})
</script>
