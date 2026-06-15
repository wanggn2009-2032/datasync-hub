import { createRouter, createWebHistory } from 'vue-router'
import Tasks from '../views/Tasks.vue'
import Dashboard from '../views/Dashboard.vue'
import TaskDetail from '../views/TaskDetail.vue'

const routes = [
  { path: '/', redirect: '/tasks' },
  { path: '/tasks', component: Tasks },
  { path: '/tasks/:id', component: TaskDetail, props: true },
  { path: '/dashboard', component: Dashboard }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
