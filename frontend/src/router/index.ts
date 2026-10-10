import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import AppLayout from '../layouts/AppLayout.vue'
import FulfillView from '../views/FulfillView.vue'
import IncidentView from '../views/IncidentView.vue'
import MapView from '../views/MapView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: '/fulfill' },
        { path: 'fulfill', component: FulfillView },
        { path: 'incidents', component: IncidentView },
        { path: 'map', component: MapView }
      ]
    }
  ]
})

router.beforeEach((to) => {
  const token = localStorage.getItem('clinic_token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/fulfill'
  }
  return true
})

export default router
