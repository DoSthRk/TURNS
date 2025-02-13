import Vue from 'vue';
import Router from 'vue-router';
import store from './store';
import Login from './components/Login.vue';
import Main from './components/Main.vue';
import axios from 'axios';
import Register from './components/Register.vue';
import Schedule from './components/Schedule.vue';
import LogList from './components/LogList.vue';
import Games from './components/Games.vue';
import { Message } from 'element-ui';

Vue.use(Router);

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/logList',
      name: 'LogList',
      component: LogList,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/login',
      name: 'login',
      component: Login,
      meta: { requiresAuth: false }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register
    },
    {
      path: '/main',
      name: 'main',
      component: Main,
      meta: { requiresAuth: true }
    },
    {
      path: '/schedule',
      name: 'Schedule',
      component: Schedule,
      meta: { requiresAuth: true }
    },
    {
      path: '/games',
      name: 'Games',
      component: Games,
      meta: { requiresAuth: true }
    },
    {
      path: '*',
      redirect: '/login'
    }
  ]
});

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userRole = localStorage.getItem('userRole');
  
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!token) {
      next({
        path: '/login',
        query: { redirect: to.fullPath }
      });
    } else if (to.matched.some(record => record.meta.requiresAdmin)) {
      if (userRole !== 'admin') {
        Message.error('无权限访问该页面');
        next(false);
      } else {
        next();
      }
    } else {
      next();
    }
  } else {
    if (token && (to.path === '/login' || to.path === '/register')) {
      next('/main');
    } else {
      next();
    }
  }
});

export default router;
