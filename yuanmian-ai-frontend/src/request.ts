import axios from 'axios'
import {message} from 'ant-design-vue'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/utils/auth'


// 创建axios实例
const myAxios = axios.create({
  baseURL: 'http://localhost:8101/api',
  timeout: 60000,
  withCredentials: false
})

let isRefreshing = false
let refreshSubscribers: Array<(token: string) => void> = []

// 全局请求拦截器
myAxios.interceptors.request.use(
  function(config) {
    const accessToken = getAccessToken()
    if (accessToken) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  function(error) {
    return Promise.reject(error)
  }
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  async function(response) {
    const {data} = response
    // 未登录
    if (data.code === 40100) {
      const originalRequest = response.config
      const refreshToken = getRefreshToken()
      if (
        refreshToken &&
        !String(originalRequest.url || '').includes('/user/refresh') &&
        !String(originalRequest.url || '').includes('/user/login')
      ) {
        if (!isRefreshing) {
          isRefreshing = true
          try {
            const refreshRes = await axios.post('http://localhost:8101/api/user/refresh', { refreshToken })
            if (refreshRes.data.code === 0 && refreshRes.data.data?.accessToken) {
              setTokens(refreshRes.data.data.accessToken, refreshRes.data.data.refreshToken)
              refreshSubscribers.forEach((cb) => cb(refreshRes.data.data?.accessToken || ''))
              refreshSubscribers = []
            } else {
              clearTokens()
              message.warning('登录已过期，请重新登录')
              window.location.href = `/user/login?redirect=${window.location.href}`
            }
          } finally {
            isRefreshing = false
          }
        }
        const retryOriginal = new Promise((resolve) => {
          refreshSubscribers.push((token: string) => {
            originalRequest.headers = originalRequest.headers || {}
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(myAxios(originalRequest))
          })
        })
        return retryOriginal as any
      }
      clearTokens()
      if(!window.location.pathname.includes('/user/login')){
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function(error) {
    // message.error(error.message)
    return Promise.reject(error)
  }
)

export default myAxios
