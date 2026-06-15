import axios from 'axios'

export function listTasks() {
  return axios.get('/api/tasks')
}

export function getTask(id) {
  return axios.get(`/api/tasks/${id}`)
}

export function createTask(payload) {
  return axios.post('/api/tasks', payload)
}

export function deleteTask(id) {
  return axios.delete(`/api/tasks/${id}`)
}

export function executeTask(id) {
  return axios.post(`/api/tasks/${id}/execute`)
}

export function getExecutions(id) {
  return axios.get(`/api/tasks/${id}/executions`)
}
