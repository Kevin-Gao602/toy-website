import api from './index'

export const productApi = {
  // 获取产品列表（支持分页、搜索、分类筛选）
  getProducts(params = {}) {
    console.log('📡 发送产品列表请求:', {
      url: '/products',
      params: params
    })
    return api.get('/products', { params })
      .then(response => {
        console.log('✅ 产品列表请求成功:', response)
        return response
      })
      .catch(error => {
        console.error('❌ 产品列表请求失败:', error)
        throw error
      })
  },

  // 获取产品详情
  getProduct(id) {
    return api.get(`/products/${id}`)
  }
}

