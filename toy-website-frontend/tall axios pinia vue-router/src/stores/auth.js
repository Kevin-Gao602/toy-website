import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || null)
  const user = ref(null)
  const isLoading = ref(false)

  const isAuthenticated = computed(() => !!token.value)

  async function login(email, password) {
    try {
      isLoading.value = true
      const response = await authApi.login(email, password)
      token.value = response.data.token
      localStorage.setItem('token', token.value)
      await fetchUser()
      return response
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function register(name, email, password) {
    try {
      isLoading.value = true
      const response = await authApi.register({ name, email, password })
      token.value = response.data.token
      localStorage.setItem('token', token.value)
      await fetchUser()
      return response
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchUser() {
    if (!token.value) return
    try {
      const response = await authApi.getCurrentUser()
      user.value = response.data
    } catch (error) {
      console.error('Failed to fetch user:', error)
      logout()
    }
  }

  function logout() {
    token.value = null
    user.value = null
    localStorage.removeItem('token')
  }

  // 初始化时恢复用户信息
  if (token.value) {
    fetchUser()
  }

  return {
    token,
    user,
    isLoading,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUser
  }
})

