import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import { useCartStore } from '@/stores/cart'

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
      // After login, load the current user's cart so UI badge reflects the right user
      const cartStore = useCartStore()
      try {
        await cartStore.fetchCart()
      } catch (e) {
        // Don't fail login just because cart loading failed (backend error / transient network)
        console.warn('Login succeeded but failed to load cart:', e)
      }
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
      const cartStore = useCartStore()
      try {
        await cartStore.fetchCart()
      } catch (e) {
        console.warn('Register succeeded but failed to load cart:', e)
      }
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
      // Only logout on real auth failures. For network/server errors we keep the token
      // so the user doesn't "randomly" lose login state on refresh.
      const status = error?.response?.status
      console.error('Failed to fetch user:', error)
      if (status === 401 || status === 403) {
        logout()
      }
    }
  }

  function logout() {
    // Clear cart UI state when switching users / logging out
    const cartStore = useCartStore()
    cartStore.resetCart()
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

