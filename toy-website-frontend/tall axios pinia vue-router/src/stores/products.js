import { defineStore } from 'pinia'
import { ref } from 'vue'
import { productApi } from '@/api/products'

export const useProductsStore = defineStore('products', () => {
  const products = ref([])
  const currentProduct = ref(null)
  const isLoading = ref(false)
  const totalElements = ref(0)
  const totalPages = ref(0)
  const currentPage = ref(0)

  async function fetchProducts(params = {}) {
    try {
      isLoading.value = true
      const response = await productApi.getProducts({
        page: params.page || 0,
        size: params.size || 20,
        search: params.search || '',
        category: params.category || ''
      })
      
      const data = response.data
      products.value = data.content || data || []
      totalElements.value = data.totalElements || products.value.length
      totalPages.value = data.totalPages || 1
      currentPage.value = data.number || 0
      
      // 调试：检查产品数据格式
      if (import.meta.env.DEV) {
        console.log('📦 Products API Response:', response.data)
        console.log('📦 Products fetched:', products.value.length)
        if (products.value.length > 0) {
          console.log('📦 First product:', products.value[0])
          console.log('📦 First product keys:', Object.keys(products.value[0]))
          console.log('📦 First product imageUrl:', products.value[0].imageUrl)
          console.log('📦 First product image_url:', products.value[0].image_url)
          console.log('📦 First product has imageUrl?', 'imageUrl' in products.value[0])
          console.log('📦 First product has image_url?', 'image_url' in products.value[0])
        } else {
          console.warn('⚠️ 产品列表为空！请检查后端数据库是否有数据')
        }
      }
      
      return response.data
    } catch (error) {
      console.error('Failed to fetch products:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  async function fetchProduct(id) {
    try {
      isLoading.value = true
      const response = await productApi.getProduct(id)
      currentProduct.value = response.data
      return response.data
    } catch (error) {
      console.error('Failed to fetch product:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  function clearCurrentProduct() {
    currentProduct.value = null
  }

  return {
    products,
    currentProduct,
    isLoading,
    totalElements,
    totalPages,
    currentPage,
    fetchProducts,
    fetchProduct,
    clearCurrentProduct
  }
})

