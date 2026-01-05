import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])
  const isLoading = ref(false)

  const subtotal = computed(() => {
    return items.value.reduce((sum, item) => {
      return sum + (item.price * item.quantity)
    }, 0)
  })

  const itemCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const isEmpty = computed(() => items.value.length === 0)

  async function fetchCart() {
    try {
      isLoading.value = true
      const response = await cartApi.getCart()
      items.value = response.data.items || []
      return response.data
    } catch (error) {
      console.error('Failed to fetch cart:', error)
      // If the user is not authenticated, treat cart as empty (UI will redirect to login where appropriate)
      if (error?.response?.status === 401) {
        items.value = []
        return { items: [] }
      }
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function addItem(productId, quantity) {
    try {
      isLoading.value = true
      await cartApi.addToCart(productId, quantity)
      await fetchCart() // 重新获取购物车
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function updateQuantity(productId, quantity) {
    try {
      isLoading.value = true
      await cartApi.updateCartItem(productId, quantity)
      await fetchCart()
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function removeItem(productId) {
    try {
      isLoading.value = true
      await cartApi.removeCartItem(productId)
      await fetchCart()
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function clearCart() {
    try {
      isLoading.value = true
      await cartApi.clearCart()
      items.value = []
    } catch (error) {
      throw error
    } finally {
      isLoading.value = false
    }
  }

  // Local-only reset (used on logout / auth loss)
  function resetCart() {
    items.value = []
    isLoading.value = false
  }

  return {
    items,
    isLoading,
    subtotal,
    itemCount,
    isEmpty,
    fetchCart,
    addItem,
    updateQuantity,
    removeItem,
    clearCart,
    resetCart
  }
})

