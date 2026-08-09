import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import DashboardView from '../views/DashboardView.vue'
import DeviceView from '../views/DeviceView.vue'
import AlarmView from '../views/AlarmView.vue'
import AlarmRuleView from '../views/AlarmRuleView.vue'
import HistoryView from '../views/HistoryView.vue'
import LoginView from '../views/LoginView.vue'
import { isAuthenticated } from '../api/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true },
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: DashboardView,
      meta: { requiresAuth: true },
    },
    {
      path: '/device',
      name: 'device',
      component: DeviceView,
      meta: { requiresAuth: true },
    },
    {
      path: '/alarm',
      name: 'alarm',
      component: AlarmView,
      meta: { requiresAuth: true },
    },
    {
      path: '/alarm/rules',
      name: 'alarmRules',
      component: AlarmRuleView,
      meta: { requiresAuth: true },
    },
    {
      path: '/history',
      name: 'history',
      component: HistoryView,
      meta: { requiresAuth: true },
    },
  ],
})

router.beforeEach((to, _from, next) => {
  const authenticated = isAuthenticated()

  if (to.meta.requiresAuth && !authenticated) {
    // 未登录访问需要认证的页面 → 跳转 login
    next('/login')
  } else if (to.path === '/login' && authenticated) {
    // 已登录访问 login → 跳转 dashboard
    next('/dashboard')
  } else {
    next()
  }
})

export default router
