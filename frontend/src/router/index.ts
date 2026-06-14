import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      component: MainLayout,
      children: [
        {
          path: '',
          redirect: '/today'
        },
        {
          path: '/today',
          component: () => import('@/views/today/index.vue'),
          meta: { title: '任务中心' }
        },
        {
          path: '/tasks',
          redirect: '/today'
        },
        {
          path: '/review',
          component: () => import('@/views/review/index.vue'),
          meta: { title: '每日复盘' }
        },
        {
          path: '/learnings',
          component: () => import('@/views/learnings/index.vue'),
          meta: { title: '学习记录' }
        },
        {
          path: '/profile',
          component: () => import('@/views/profile/index.vue'),
          meta: { title: '个人画像' }
        },
        {
          path: '/analytics',
          component: () => import('@/views/analytics/index.vue'),
          meta: { title: '数据分析' }
        }
      ]
    }
  ]
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('accessToken')
  if (to.path === '/login') {
    token ? next('/today') : next()
  } else {
    token ? next() : next('/login')
  }
})

export default router