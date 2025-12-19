import api from './index'

export const authApi = {
  // 注册
  register(data) {
    return api.post('/auth/register', data)
  },

  // 登录
  login(email, password) {
    return api.post('/auth/login', { email, password })
  },

  // 获取当前用户信息
  getCurrentUser() {
    return api.get('/auth/me')
  }
}

