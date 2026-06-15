<template>
  <div>
    <el-button type="primary" @click="openCreate">新建任务</el-button>
    <el-table :data="tasks" style="margin-top:16px">
      <el-table-column prop="taskCode" label="任务编码" width="180" />
      <el-table-column prop="taskName" label="任务名称" />
      <el-table-column prop="status" label="状态" width="100" />
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="view(row.id)">查看</el-button>
          <el-button size="small" type="warning" @click="execute(row.id)">执行</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listTasks, executeTask, deleteTask } from '../api/tasks'
import { useRouter } from 'vue-router'
import axios from 'axios'

const tasks = ref([])
const router = useRouter()

const load = () => {
  listTasks().then(r => tasks.value = r.data)
}
onMounted(load)

function view(id) { router.push(`/tasks/${id}`) }
function execute(id) {
  executeTask(id).then(() => { alert('Triggered') })
}
function remove(id) {
  deleteTask(id).then(() => load())
}
function openCreate() {
  const code = 'task_' + Date.now()
  createSampleTask(code).then(() => load())
}

function createSampleTask(code) {
  return axios.post('/api/tasks', {
    taskCode: code,
    taskName: '示例任务 ' + code,
    sourceType: 'mysql',
    targetType: 'mysql',
    cronExpression: '0/30 * * * * ?',
    enabled: 1,
    status: 1
  })
}
</script>
