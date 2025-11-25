import { createRouter, createWebHistory } from 'vue-router'
import DashboardView from '../views/DashboardView.vue'
import LoginView from '../views/LoginView.vue'
import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: DashboardView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    },
    {
      path: '/library',
      name: 'library',
      component: () => import('../views/LibraryView.vue')
    }
  ],
})

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore()
  if (!authStore.authInitialized) {
    await authStore.initAuth()
  }
  next()
})

export default router
