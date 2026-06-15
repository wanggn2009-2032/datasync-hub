<template>
  <div style="display:flex;gap:12px;align-items:flex-start">
    <div style="flex:1">
      <el-row>
        <el-col :span="16">
          <h3>{{task.taskName}}</h3>
        </el-col>
        <el-col :span="8" style="text-align:right">
          <el-button type="primary" @click="saveDag">保存 DAG</el-button>
          <el-button @click="exportDag">导出 JSON</el-button>
        </el-col>
      </el-row>

      <div style="border:1px solid #eee;margin-top:8px">
        <dag-editor ref="dagEditor" v-if="showDag" :initialData="initialDag" />
      </div>
    </div>

    <div style="width:320px;border-left:1px solid #f0f0f0;padding-left:12px">
      <h4>节点属性</h4>
      <node-properties v-if="selectedNode" :node="selectedNode" @apply="applyNodeProperties" />
      <div v-else style="color:#888">未选中节点，点击节点以编辑属性</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getTask, getExecutions } from '../api/tasks'
import { useRoute } from 'vue-router'
import DAGEditor from '../components/DAGEditor.vue'
import NodeProperties from '../components/NodeProperties.vue'
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
const selectedNode = ref(null)

onMounted(() => {
  getTask(id).then(r => {
    task.value = r.data
    if (task.value.dagConfig) {
      try {
        initialDag.value = JSON.parse(task.value.dagConfig)
      } catch (e) {
        console.warn('Invalid dagConfig JSON in task', e)
      }
    }
  })
  loadExecutions()
  connectWebSocket()

  // listen for selection events dispatched from DAGEditor container
  setTimeout(() => {
    const editorEl = document.querySelector('[ref="dagEditor"]')
    // As ref isn't attached to DOM attribute reliably in template, listen to global container via setInterval
    // Instead, add event listener to document and rely on DAGEditor dispatching CustomEvent on its root container
    document.addEventListener('dag-node-selected', (ev) => {
      selectedNode.value = ev.detail
    })
  }, 300)
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

function applyNodeProperties(nodeId, props) {
  if (!dagEditor.value) return
  const ok = dagEditor.value.setNodeProperties(nodeId, props)
  if (ok) {
    // refresh selectedNode to updated model
    const model = dagEditor.value.getSelectedNode()
    selectedNode.value = model
  }
}
</script>
