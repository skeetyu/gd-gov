import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../components/Home.vue'

import GovIndex from '../components/gov/Index.vue'
import Cluster from '../components/gov/Cluster.vue'
import Monitor from '../components/monitor/Monitor.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/home',
    name: 'Home',
    component: Home,
    children:[
      {
        path: '/monitor',
        name: 'Monitor',
        component: Monitor
      },
      {
        path: '/gov',
        name: 'Gov',
        component: GovIndex,
        children:[
          {
            path: '/cluster',
            name: 'Cluster',
            component: Cluster
          }
        ]
      }
    ]
  },
]

const router = new VueRouter({
  routes
})

export default router
