import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

// 获取 API 基础 URL，支持环境变量配置
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

console.log('🔗 API Base URL:', API_BASE_URL)

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000 // 10 秒超时
})

// 请求拦截器：添加 token
api.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore()
    if (authStore.token) {
      config.headers.Authorization = `Bearer ${authStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：处理错误
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // 处理网络错误或连接失败
    if (!error.response) {
      console.error('❌ Network Error:', error.message)
      console.error('💡 Make sure the backend server is running at:', API_BASE_URL)
      
      // 如果是网络错误，提供更友好的提示
      if (error.code === 'ECONNABORTED') {
        error.message = 'Request timeout. Please check if the backend server is running.'
      } else if (error.message === 'Network Error') {
        error.message = `Cannot connect to backend server at ${API_BASE_URL}. Please make sure the server is running.`
      }
    }
    
    // 处理 401 未授权
    if (error.response?.status === 401) {
      const authStore = useAuthStore()
      authStore.logout()
      // 如果不在登录页，重定向到登录页
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    
    // 处理其他 HTTP 错误
    if (error.response) {
      console.error('❌ API Error:', {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data,
        url: error.config?.url
      })
    }
    
    return Promise.reject(error)
  }
)

export default api

