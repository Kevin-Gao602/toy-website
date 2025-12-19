/**
 * 调试工具：检查产品数据
 * 在浏览器控制台运行此函数来诊断问题
 */

export async function debugProducts() {
  const apiUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'
  
  console.log('🔍 开始诊断产品数据...')
  console.log('API URL:', apiUrl)
  
  try {
    // 1. 测试基础连接
    console.log('\n1️⃣ 测试基础连接...')
    const testResponse = await fetch(`${apiUrl}/test/ping`)
    const testData = await testResponse.json()
    console.log('✅ 后端连接正常:', testData)
    
    // 2. 获取产品列表（只获取1个）
    console.log('\n2️⃣ 获取产品列表（size=1）...')
    const productsResponse = await fetch(`${apiUrl}/products?size=1`)
    console.log('响应状态:', productsResponse.status, productsResponse.statusText)
    console.log('响应头:', Object.fromEntries(productsResponse.headers.entries()))
    
    const productsData = await productsResponse.json()
    console.log('📦 完整响应数据:', productsData)
    console.log('📦 响应数据大小:', JSON.stringify(productsData).length, 'bytes')
    
    // 3. 检查产品数据
    if (productsData.content && productsData.content.length > 0) {
      const firstProduct = productsData.content[0]
      console.log('\n3️⃣ 第一个产品数据:')
      console.log('完整对象:', firstProduct)
      console.log('所有字段:', Object.keys(firstProduct))
      console.log('imageUrl字段:', firstProduct.imageUrl)
      console.log('image_url字段:', firstProduct.image_url)
      console.log('imageUrl类型:', typeof firstProduct.imageUrl)
      console.log('imageUrl是否为空:', !firstProduct.imageUrl)
      
      // 4. 测试图片URL
      if (firstProduct.imageUrl) {
        console.log('\n4️⃣ 测试图片URL...')
        const img = new Image()
        img.onload = () => {
          console.log('✅ 图片可以加载:', firstProduct.imageUrl)
        }
        img.onerror = () => {
          console.log('❌ 图片无法加载:', firstProduct.imageUrl)
        }
        img.src = firstProduct.imageUrl
      } else {
        console.log('⚠️ 没有imageUrl字段！')
      }
    } else {
      console.log('⚠️ 产品列表为空！')
      console.log('可能原因:')
      console.log('1. 数据库没有数据（data.sql未执行）')
      console.log('2. 后端服务未正确启动')
      console.log('3. 数据库连接失败')
    }
    
    // 5. 获取所有产品
    console.log('\n5️⃣ 获取所有产品...')
    const allProductsResponse = await fetch(`${apiUrl}/products`)
    const allProductsData = await allProductsResponse.json()
    console.log('产品总数:', allProductsData.totalElements)
    console.log('产品列表:', allProductsData.content)
    
    if (allProductsData.content && allProductsData.content.length > 0) {
      console.log('\n所有产品的imageUrl:')
      allProductsData.content.forEach((product, index) => {
        console.log(`${index + 1}. ${product.name}:`, product.imageUrl || product.image_url || '❌ 无图片URL')
      })
    }
    
  } catch (error) {
    console.error('❌ 诊断过程中出错:', error)
  }
}

// 如果在浏览器控制台，可以直接调用
if (typeof window !== 'undefined') {
  window.debugProducts = debugProducts
  console.log('💡 提示: 在控制台运行 debugProducts() 来诊断产品数据问题')
}

