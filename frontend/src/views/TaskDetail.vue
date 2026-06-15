<template>
  <div>
    <el-row>
      <el-col :span="16">
        <h3>{{task.taskName}}</h3>
        <dag-editor v-if="showDag" />
      </el-col>
      <el-col :span="8">
        <h4>执行日志 / 实时进度</h4>
        <div v-for="e in executions" :key="e.id" style="margin-bottom:12px;border-bottom:1px dashed #eee;padding-bottom:8px">
          <div>执行ID: {{e.id}} 状态: {{e.status}}</div>
          <pre style="white-space:pre-wrap">{{ e.executionLog }}</pre>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getTask, getExecutions } from '../api/tasks'
import { useRoute } from 'vue-router'
import DAGEditor from '../components/DAGEditor.vue'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

const route = useRoute()
const id = route.params.id
const task = ref({})
const executions = ref([])
const showDag = ref(true)

onMounted(() => {
  getTask(id).then(r => task.value = r.data)
  loadExecutions()
  connectWebSocket()
})

function loadExecutions() {
  getExecutions(id).then(r => executions.value = r.data)
}

let stompClient = null
function connectWebSocket() {
  const socket = new SockJS('/ws')
  stompClient = Stomp.over(socket)
  stompClient.connect({}, () => {
    stompClient.subscribe('/topic/task-progress', (msg) => {
      try {
        const body = JSON.parse(msg.body)
        if (body.taskId == id) {
          loadExecutions()
        }
      } catch(e) { console.error(e) }
    })
  })
}
</script>
