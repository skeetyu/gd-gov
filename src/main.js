import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'

import axios from 'axios'

// 设置反向代理，前端请求默认发送到 http://localhost:8080/api
// var axios = require('axios')
axios.defaults.baseURL = 'http://localhost:8080/'
// 全局注册，之后可在其它组件中通过 this.$axios 发送数据
Vue.prototype.$axios = axios
Vue.config.productionTip = false

Vue.use(ElementUI)

new Vue({
  router,
  render: h => h(App)
}).$mount('#cpp')
