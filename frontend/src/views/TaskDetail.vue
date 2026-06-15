<template>
  <div>
    <el-row>
      <el-col :span="16">
        <h3>{{task.taskName}}</h3>
        <dag-editor ref="dagEditor" v-if="showDag" :initialData="initialDag" />
        <div style="margin-top:12px">
          <el-button type="primary" @click="saveDag">保存 DAG</el-button>
          <el-button @click="exportDag">导出 JSON</el-button>
        </div>
      </el-col>
      <el-col :span="8">
        <h4>执行日志 / 实时进度</h4>
        <div v-for="e in executions" :key="e.id" style="margin-bottom:12px;border-bottom:1px dashed #eee;padding-bottom:8px">
          <div>执行ID: {{e.id}} 状态: {{e.status}} 开始: {{formatTime(e.startTime)}}</div>
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
import axios from 'axios'

const route = useRoute()
const id = route.params.id
const task = ref({})
const executions = ref([])
const showDag = ref(true)
const dagEditor = ref(null)
const initialDag = ref(null)

onMounted(() => {
  getTask(id).then(r => {
    task.value = r.data
    // if dagConfig exists, try to parse and load
    if (task.value.dagConfig) {
      try {
        initialDag.value = JSON.parse(task.value.dagConfig)
        // when DAGEditor is ready, it will receive initialData prop
      } catch (e) {
        console.warn('Invalid dagConfig JSON in task', e)
      }
    }
  })
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

async function saveDag() {
  if (!dagEditor.value) {
    alert('编辑器未就绪')
    return
  }
  const data = dagEditor.value.getGraphData()
  if (!data) {
    alert('获取 DAG 数据失败')
    return
  }
  try {
    await axios.put(`/api/tasks/${id}/dag`, { dagConfig: data })
    alert('保存成功')
  } catch (e) {
    console.error(e)
    alert('保存失败')
  }
}

function exportDag() {
  if (!dagEditor.value) {
    alert('编辑器未就绪')
    return
  }
  const data = dagEditor.value.getGraphData()
  if (!data) return
  const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `dag_${id}.json`
  a.click()
  URL.revokeObjectURL(url)
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return d.toLocaleString()
}
</script>
