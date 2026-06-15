<template>
  <div ref="container" style="width:100%;height:600px;border:1px solid #eee"></div>
</template>

<script setup>
import { onMounted, ref, onBeforeUnmount, defineExpose } from 'vue'
import G6 from '@antv/g6'

const props = defineProps({
  initialData: {
    type: Object,
    default: null
  }
})

const container = ref(null)
let graph = null
let selectedNode = null

onMounted(() => {
  graph = new G6.Graph({
    container: container.value,
    width: container.value.clientWidth,
    height: container.value.clientHeight,
    modes: {
      default: ['drag-canvas','zoom-canvas','click-select','drag-node']
    },
    defaultNode: {
      type: 'rect',
      style: {
        fill: '#fff',
        stroke: '#5B8FF9',
        radius: 4
      }
    },
    defaultEdge: {
      style: {
        stroke: '#A3B1BF'
      }
    }
  })

  if (props.initialData) {
    try {
      graph.data(props.initialData)
    } catch (e) {
      console.error('Invalid initialData for DAGEditor', e)
    }
  } else {
    // default sample
    const data = {
      nodes: [
        { id: 'start', label: 'Start', x: 100, y: 100, data: { type: 'start' } },
        { id: 'task1', label: 'Task 1', x: 300, y: 100, data: { type: 'task', table: 'table_a' } },
        { id: 'task2', label: 'Task 2', x: 500, y: 100, data: { type: 'task', table: 'table_b' } }
      ],
      edges: [
        { source: 'start', target: 'task1' },
        { source: 'task1', target: 'task2' }
      ]
    }
    graph.data(data)
  }
  graph.render()

  // node click -> select and record
  graph.on('node:click', (ev) => {
    const item = ev.item
    selectedNode = item
    const model = item.getModel()
    // emit custom DOM event by setting dataset on container element (parent can read via ref)
    try { container.value.dispatchEvent(new CustomEvent('dag-node-selected', { detail: model })) } catch(e){}
  })

  // click canvas to clear selection
  graph.on('canvas:click', () => {
    selectedNode = null
    try { container.value.dispatchEvent(new CustomEvent('dag-node-selected', { detail: null })) } catch(e){}
  })

  // handle resize
  const resizeObserver = new ResizeObserver(() => {
    if (container.value && graph) {
      graph.changeSize(container.value.clientWidth, container.value.clientHeight)
    }
  })
  resizeObserver.observe(container.value)

  onBeforeUnmount(() => {
    if (graph) {
      graph.destroy()
      graph = null
    }
    resizeObserver.disconnect()
  })
})

// expose methods to parent via ref
function getGraphData() {
  if (!graph) return null
  try {
    const data = graph.save()
    return data
  } catch (e) {
    console.error('Failed to export graph data', e)
    return null
  }
}

function loadGraphData(data) {
  if (!graph) return
  try {
    graph.data(data)
    graph.render()
  } catch (e) {
    console.error('Failed to load graph data', e)
  }
}

function setNodeProperties(nodeId, props) {
  if (!graph) return false
  const node = graph.findById(nodeId)
  if (!node) return false
  const model = node.getModel()
  model.data = Object.assign({}, model.data || {}, props)
  if (props.label) model.label = props.label
  graph.updateItem(node, model)
  return true
}

function getSelectedNode() {
  if (!selectedNode) return null
  return selectedNode.getModel()
}

defineExpose({ getGraphData, loadGraphData, setNodeProperties, getSelectedNode })
</script>
