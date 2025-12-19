import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { debugProducts } from './utils/debugProducts'

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')

// 开发环境下，将调试工具添加到window对象
if (import.meta.env.DEV) {
  window.debugProducts = debugProducts
  console.log('💡 提示: 在控制台运行 debugProducts() 来诊断产品数据问题')
}
