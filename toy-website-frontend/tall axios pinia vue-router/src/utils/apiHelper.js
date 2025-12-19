/**
 * API 辅助工具函数
 */

/**
 * 检查后端连接
 * @returns {Promise<boolean>} 连接是否成功
 */
export async function checkBackendConnection() {
  try {
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'}/products?size=1`,
      {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    )
    return response.ok
  } catch (error) {
    console.error('Backend connection check failed:', error)
    return false
  }
}

/**
 * 显示连接错误提示
 */
export function showConnectionError() {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  console.error(`
    ⚠️ 无法连接到后端服务器！
    
    请检查：
    1. 后端服务是否正在运行？
    2. 后端地址是否正确：${apiUrl}
    3. 后端是否配置了 CORS？
    
    如果后端运行在不同的端口，请在 .env.local 文件中设置：
    VITE_API_BASE_URL=http://localhost:YOUR_PORT/api
  `)
}

