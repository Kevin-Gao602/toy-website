<template>
  <div v-if="showStatus" class="connection-status" :class="statusClass">
    <span class="status-icon">{{ statusIcon }}</span>
    <span class="status-message">{{ statusMessage }}</span>
    <button v-if="!isConnected" @click="retryConnection" class="btn-retry">Retry</button>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { checkBackendConnection } from '@/utils/apiHelper'

const isConnected = ref(null)
const isLoading = ref(false)
const showStatus = ref(false)

const statusClass = computed(() => {
  if (isLoading.value) return 'loading'
  return isConnected.value ? 'connected' : 'disconnected'
})

const statusIcon = computed(() => {
  if (isLoading.value) return '⏳'
  return isConnected.value ? '✅' : '❌'
})

const statusMessage = computed(() => {
  if (isLoading.value) return 'Checking connection...'
  if (isConnected.value) return 'Backend connected'
  return 'Cannot connect to backend server'
})

async function checkConnection() {
  isLoading.value = true
  showStatus.value = true
  isConnected.value = await checkBackendConnection()
  isLoading.value = false

  // 如果连接成功，3秒后隐藏状态
  if (isConnected.value) {
    setTimeout(() => {
      showStatus.value = false
    }, 3000)
  }
}

async function retryConnection() {
  await checkConnection()
}

onMounted(() => {
  // 仅在开发环境显示连接状态
  if (import.meta.env.DEV) {
    checkConnection()
  }
})
</script>

<style scoped>
.connection-status {
  position: fixed;
  top: 80px;
  right: 20px;
  padding: 0.75rem 1rem;
  border-radius: 5px;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 0.875rem;
}

.connection-status.connected {
  background: #d4edda;
  color: #155724;
  border: 1px solid #c3e6cb;
}

.connection-status.disconnected {
  background: #f8d7da;
  color: #721c24;
  border: 1px solid #f5c6cb;
}

.connection-status.loading {
  background: #fff3cd;
  color: #856404;
  border: 1px solid #ffeaa7;
}

.status-icon {
  font-size: 1rem;
}

.status-message {
  font-weight: 500;
}

.btn-retry {
  background: #667eea;
  color: white;
  border: none;
  padding: 0.25rem 0.75rem;
  border-radius: 3px;
  cursor: pointer;
  font-size: 0.75rem;
  font-weight: 600;
  margin-left: 0.5rem;
}

.btn-retry:hover {
  background: #5568d3;
}
</style>

