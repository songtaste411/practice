import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

export interface ApiBody<T> {
  code: number
  message: string
  traceId: string
  data: T
}

const http = axios.create({ timeout: 15000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('clinic_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    const body = error.response?.data as ApiBody<unknown> | undefined
    const trace = body?.traceId ? `（追踪号 ${body.traceId}）` : ''
    const message = body?.message || '网络或服务器出错了'
    if (error.response?.status === 401 && !String(error.config?.url || '').includes('/api/auth/login')) {
      localStorage.removeItem('clinic_token')
      localStorage.removeItem('clinic_name')
      ElMessage.warning('登录已失效，请重新登录')
      router.push('/login')
    } else {
      ElMessage.error(`${message}${trace}`)
    }
    return Promise.reject(error)
  }
)

export async function api<T>(
  method: 'get' | 'post' | 'put',
  url: string,
  data?: unknown,
  headers?: Record<string, string>,
  timeoutMs?: number,
): Promise<T> {
  const response = await http.request<ApiBody<T>>({ method, url, data, headers, timeout: timeoutMs })
  return response.data.data
}

export default http
