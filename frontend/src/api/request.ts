import axios, { AxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 60000,
})

let isRedirectingToLogin = false

axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error: any) => Promise.reject(error)
)

axiosInstance.interceptors.response.use(
  (response: any) => {
    const { data } = response
    if (data.code === 200) {
      return data.data
    }
    ElMessage.error(data.msg || '请求失败')
    return Promise.reject(new Error(data.msg))
  },
  async (error: any) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      if (!isRedirectingToLogin && router.currentRoute.value.path !== '/login') {
        isRedirectingToLogin = true
        try {
          await ElMessageBox.alert('登录已过期，请重新登录', '会话已过期', {
            type: 'warning',
            confirmButtonText: '前往登录',
            callback: () => {
              isRedirectingToLogin = false
              router.push('/login')
            }
          })
        } catch {
          isRedirectingToLogin = false
          router.push('/login')
        }
      }
      return Promise.reject(new Error('登录已过期'))
    }
    const msg = error.response?.data?.msg
    if (msg) {
      ElMessage.error(msg)
    } else if (error.message && !error.message.includes('Network Error')) {
      ElMessage.error(error.message || '请求失败')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

export default axiosInstance

export function get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return axiosInstance.get(url, config) as Promise<T>
}

export function post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return axiosInstance.post(url, data, config) as Promise<T>
}

export function put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
  return axiosInstance.put(url, data, config) as Promise<T>
}

export function del<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return axiosInstance.delete(url, config) as Promise<T>
}
