<template>
  <div id="app">
    <NavBar />
    <ConnectionStatus />
    <main class="main-content">
      <router-view />
    </main>
    <footer class="footer">
      <p>&copy; 2024 Toy Shop. All rights reserved.</p>
    </footer>
  </div>
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
import ConnectionStatus from '@/components/ConnectionStatus.vue'
import { onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { checkBackendConnection, showConnectionError } from '@/utils/apiHelper'

const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(async () => {
  // 检查后端连接（仅在开发环境）
  if (import.meta.env.DEV) {
    const isConnected = await checkBackendConnection()
    if (!isConnected) {
      showConnectionError()
    } else {
      console.log('✅ Backend connection successful!')
    }
  }

  // 如果已登录，恢复用户信息和购物车
  if (authStore.isAuthenticated) {
    try {
      await authStore.fetchUser()
      await cartStore.fetchCart()
    } catch (error) {
      console.error('Failed to restore user session:', error)
    }
  }
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
  line-height: 1.6;
  color: #333;
}

#app {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
}

.footer {
  background: #2c3e50;
  color: white;
  text-align: center;
  padding: 1.5rem;
  margin-top: auto;
}

.footer p {
  margin: 0;
}
</style>
