import api from './index'

export const orderApi = {
  // 创建订单
  createOrder(orderData) {
    return api.post('/orders', orderData)
  },

  // 获取订单列表
  getOrders() {
    return api.get('/orders')
  },

  // 获取订单详情
  getOrder(id, config = {}) {
    return api.get(`/orders/${id}`, config)
  },

  // 取消订单（可选）
  cancelOrder(id) {
    return api.put(`/orders/${id}/cancel`)
  },

  // 永久删除订单记录
  deleteOrder(id) {
    return api.delete(`/orders/${id}`)
  }
}

