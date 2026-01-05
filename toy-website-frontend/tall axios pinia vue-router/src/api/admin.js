import api from './index'

export const adminApi = {
  // Products
  getProducts(params = {}) {
    return api.get('/admin/products', { params })
  },
  createProduct(data) {
    return api.post('/admin/products', data)
  },
  updateProduct(id, data) {
    return api.put(`/admin/products/${id}`, data)
  },
  deleteProduct(id) {
    return api.delete(`/admin/products/${id}`)
  },

  // Orders
  getOrders() {
    return api.get('/admin/orders')
  },
  updateOrderStatus(id, status) {
    return api.put(`/admin/orders/${id}/status`, { status })
  },

  // Users
  getUsers() {
    return api.get('/admin/users')
  },
  deleteUser(id) {
    return api.delete(`/admin/users/${id}`)
  }
}


