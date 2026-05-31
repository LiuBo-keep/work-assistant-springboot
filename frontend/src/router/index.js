import {
  createRouter,
  createWebHashHistory
} from 'vue-router'

import browser from '../store/persistent/browser'

// Layout
const Layout = () => import('../components/Layout.vue')
// 首页
const Home = () => import('../views/home/index.vue')
// HRMS
const CardRecord = () =>
  import('../views/hrms/reports/cardrecord/components/List.vue')
const ConfigBasics = () =>
  import('../views/hrms/config/components/Basics.vue')

// 其它
const Login = () => import('../views/login/index.vue')

const NotFound = () => import('../components/404.vue')

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    meta: {
      auth: true
    },
    children: [
      {
        path: 'index',
        name: 'Home',
        component: Home,
        meta: {
          title: '首页'
        }
      },

      // 打卡记录
      {
        path: 'hrms/reports/card-record',
        name: 'CardRecord',
        component: CardRecord,
        meta: {
          title: '打卡记录',
          active: '/hrms/reports/card-record'
        }
      },

      // 配置
      {
        path: 'hrms/config',
        name: 'Config',
        component: ConfigBasics,
        meta: {
          title: '通知设置',
          active: '/hrms/config'
        }
      },
      {
        path: '/password/list',
        component: () => import('../views/password/List.vue'),
        meta: { title: '密码管理', active: '/password/list', auth: true }
      },
      {
        path: '/efficiency-record/notes',
        component: () => import('../views/efficiency-record/notes/notes.vue'),
        meta: { title: '工作笔记', active: '/efficiency-record/notes', auth: true }
      }
    ]
  },

  // 登录
  {
    path: '/login',
    name: 'Login',
    component: Login
  },

  // 404
  {
    path: '/404',
    name: '404',
    component: NotFound
  },

  // 未匹配
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHashHistory(),

  routes
})

// 白名单
const whiteList = ['/login', '/404']

// 路由守卫
router.beforeEach((to) => {
  const token = browser.accessToken

  // 白名单直接放行
  if (whiteList.includes(to.path)) {
    // 已登录访问登录页
    if (token && to.path === '/login') {
      return '/'
    }
    return true
  }

  // 未登录
  if (!token) {
    return '/login'
  }
  return true
})

export default router