import axios from 'axios'
import { message } from 'ant-design-vue'
import { clearTokens, getAccessToken, getRefreshToken, setTokens } from '@/utils/auth'

// 创建axios实例
const myAxios = axios.create({
  baseURL: 'http://localhost:8101/api',
  timeout: 60000,
  withCredentials: false,
})

/** 并发 401 时共用一个 refresh，避免多次换票 */
let refreshPromise: Promise<string> | null = null

async function refreshAccessToken(): Promise<string> {
  if (refreshPromise) {
    return refreshPromise
  }
  const refreshToken = getRefreshToken()
  if (!refreshToken) {
    return Promise.reject(new Error('无 refreshToken'))
  }
  refreshPromise = (async () => {
    try {
      const refreshRes = await axios.post('http://localhost:8101/api/user/refresh', { refreshToken })
      if (refreshRes.data.code === 0 && refreshRes.data.data?.accessToken) {
        setTokens(refreshRes.data.data.accessToken, refreshRes.data.data.refreshToken)
        return refreshRes.data.data.accessToken as string
      }
      throw new Error(refreshRes.data?.message || '刷新失败')
    } finally {
      refreshPromise = null
    }
  })()
  return refreshPromise
}

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    const accessToken = getAccessToken()
    if (accessToken) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${accessToken}`
    }
    return config
  },
  function (error) {
    return Promise.reject(error)
  }
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  async function (response) {
    const { data } = response
    if (data.code !== 40100) {
      return response
    }

    const originalRequest = response.config as typeof response.config & { _retry?: boolean }
    const url = String(originalRequest.url || '')

    if (url.includes('/user/refresh') || url.includes('/user/login')) {
      clearTokens()
      if (!window.location.pathname.includes('/user/login')) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${encodeURIComponent(window.location.href)}`
      }
      return Promise.reject(response)
    }

    if (originalRequest._retry) {
      clearTokens()
      if (!window.location.pathname.includes('/user/login')) {
        message.warning('登录已过期，请重新登录')
        window.location.href = `/user/login?redirect=${encodeURIComponent(window.location.href)}`
      }
      return Promise.reject(response)
    }

    if (!getRefreshToken()) {
      clearTokens()
      if (!window.location.pathname.includes('/user/login')) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${encodeURIComponent(window.location.href)}`
      }
      return Promise.reject(response)
    }

    try {
      const newAccess = await refreshAccessToken()
      originalRequest._retry = true
      originalRequest.headers = originalRequest.headers || {}
      originalRequest.headers.Authorization = `Bearer ${newAccess}`
      return myAxios(originalRequest)
    } catch {
      clearTokens()
      if (!window.location.pathname.includes('/user/login')) {
        message.warning('登录已过期，请重新登录')
        window.location.href = `/user/login?redirect=${encodeURIComponent(window.location.href)}`
      }
      return Promise.reject(response)
    }
  },
  function (error) {
    return Promise.reject(error)
  }
)

export default myAxios
