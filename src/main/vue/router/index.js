import { createRouter, createWebHistory } from 'vue-router'
import Login from '@/main/vue/pages/Login.vue'
import StartView from '../views/Start.vue'
import Register from '../pages/Register.vue'
import forgetPassword from '../pages/ForgotPassword.vue'
import resetPassword from '../pages/ResetPassword.vue'
import user from '../views/User.vue'
import UserManagement from '../views/UserManagement.vue'
import { useUserStore } from '../stores/users'
import AnalysisPage from '../pages/AnalysisPage.vue'
import Profile from '../pages/Profile.vue'
import AdminSettings from '../pages/AdminSettings.vue'
import Impressum from '../pages/Impressum.vue'
import Home from '../views/Home.vue'
import History from '../pages/History.vue'
import Dashboard from '../views/Dashboard.vue'


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{
    path:'/',
    name:'home',
    component: StartView, meta: {requireAuth: true},
    children: [
      { path: '', component: Dashboard }
    ]
  },
  {
    path:'/login',
    component:Login
  },
  {
    path:'/history',
    component: History, meta: {requireAuth: true}
  },
  {
    path:'/home',
    component: Home
  },
  {
    path:'/register',
    component: Register
  },
  {
    path:'/forget-password',
    component: forgetPassword
  },
  {
    path:'/reset-password',
    component: resetPassword
  },
  {
    path:'/user/:id',
    name:'user',
    component:user, meta: {requireAuth: true}
  },
  {
    path:'/user-management',
    name:'usermanagement',
    component:UserManagement, meta: {requireAuth: true}
  },
  {
    path:'/volcano-analysis',
    name:'volcanoanalysis',
    component:AnalysisPage, meta: {requireAuth: true}
  },
  {
    path:'/profile',
    name:'profile',
    component:Profile, meta: {requiresAuth: true}
  },
  {
    path: '/admin-settings',
    component: AdminSettings, meta: {requiresAuth: true}
  },
  {
    path: '/impressum',
    component: Impressum
  }
],
})

router.beforeEach((to => {
  const userStore = useUserStore()
  if(to.meta.requireAuth && !userStore.authenticated && to.path !== '/forget-password' && to.path !== '/reset-password') return '/login'
}))

export default router
