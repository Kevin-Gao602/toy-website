import api from './index'

export const cartApi = {
  // 获取购物车
  getCart() {
    return api.get('/cart')
  },

  // 添加商品到购物车
  addToCart(productId, quantity) {
    return api.post('/cart/items', { productId, quantity })
  },

  // 更新购物车商品数量
  updateCartItem(productId, quantity) {
    return api.put(`/cart/items/${productId}`, { quantity })
  },

  // 从购物车移除商品
  removeCartItem(productId) {
    return api.delete(`/cart/items/${productId}`)
  },

  // 清空购物车
  clearCart() {
    return api.delete('/cart')
  }
}

