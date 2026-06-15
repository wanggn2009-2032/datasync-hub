<template>
  <div ref="container" style="width:100%;height:600px;border:1px solid #eee"></div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import G6 from '@antv/g6'

const container = ref(null)
let graph = null

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
        stroke: '#5B8FF9'
      }
    },
    defaultEdge: {
      style: {
        stroke: '#A3B1BF'
      }
    }
  })

  const data = {
    nodes: [
      { id: 'start', label: 'Start', x: 100, y: 100 },
      { id: 'task1', label: 'Task 1', x: 300, y: 100 },
      { id: 'task2', label: 'Task 2', x: 500, y: 100 }
    ],
    edges: [
      { source: 'start', target: 'task1' },
      { source: 'task1', target: 'task2' }
    ]
  }
  graph.data(data)
  graph.render()
})
</script>
