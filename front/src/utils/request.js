import axios from 'axios'
import { Message } from 'element-ui'
import router from '@/router'  // 导入路由




// 创建 axios 实例
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API || 'http://123.60.65.25:8080',
  timeout: 50000,  // 已经是50秒
  // 添加重试机制
  retry: 3,  // 重试次数
  retryDelay: 1000,  // 重试间隔
  shouldRetry: (error) => {
    // 判断是否需要重试
    return error.code === 'ECONNABORTED' || 
           error.message.includes('timeout') ||
           error.message.includes('Network Error');
  },
  headers: {
    'Content-Type': 'application/json',
  }
})

// 不需要token的白名单路径
const whiteList = ['/login', '/register']

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 如果不在白名单中，才添加token
    if (!whiteList.includes(config.url)) {
      const token = localStorage.getItem('token')
      if (token) {
        config.headers['Authorization'] = 'Bearer ' + token
      }
    }
    // 添加时间戳防止缓存
    if (config.method === 'get') {
      config.params = { ...config.params, _t: new Date().getTime() }
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 添加重试拦截器
service.interceptors.response.use(undefined, async (err) => {
  const config = err.config;
  // 如果配置不存在或未设置重试选项，直接返回错误
  if(!config || !config.retry) return Promise.reject(err);

  // 设置用于记录重试次数的变量
  config.__retryCount = config.__retryCount || 0;

  // 检查是否需要重试
  if(config.__retryCount >= config.retry || !config.shouldRetry(err)) {
    return Promise.reject(err);
  }

  // 增加重试次数
  config.__retryCount += 1;

  // 创建新的Promise来处理重试
  const backoff = new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, config.retryDelay || 1);
  });

  // 重试请求
  await backoff;
  return service(config);
});

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 只检查 401 错误码
    if (response.data && (
      response.data.code === 401 || 
      response.data.message?.toLowerCase().includes('jwt')
    )) {
      handleAuthError()
      return Promise.reject(new Error('Authentication failed'))
    }
    return response
  },
  error => {
    if (error.response) {
      console.error('Error response:', error.response.status, error.response.data);
      switch (error.response.status) {
        case 401:
          // 只有非白名单路径才提示登录失效
          if (!whiteList.includes(error.config.url)) {
            localStorage.removeItem('token')
            router.push('/login')
            Message.error('登录已失效，请重新登录')
          }
          break
        default:
          Message.error(error.response.data.message || error.response.data.msg || '请求失败')
      }
    } else if (error.request) {
      console.error('Error request (no response):', error.request);
      Message.error('服务器未响应，请检查网络连接')
    } else {
      console.error('Error setting up request:', error.message);
      Message.error('请求设置错误: ' + error.message)
    }
    return Promise.reject(error)
  }
)

// 统一处理认证错误
function handleAuthError() {
  // 如果已经在登录页或是权限不足，不做跳转
  if (router.currentRoute.path === '/login' || 
      router.currentRoute.path === '/logList') {
    return
  }
  
  localStorage.removeItem('token')
  Message.error('登录已失效，请重新登录')
  router.push('/login')
}

export default service 