import axios from 'axios'
import { clearAuthToken } from '../api/auth'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 10000,
})

// ---- request interceptor: auto-attach token ----
request.interceptors.request.use(
  (config) => {
    // Token is set by auth.ts via request.defaults.headers.common
    // No additional action needed here — the default header is inherited
    return config
  },
  (error) => Promise.reject(error),
)

// ---- response interceptor: handle 401 ----
let isRedirectingToLogin = false

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isRedirectingToLogin) {
      isRedirectingToLogin = true
      clearAuthToken()
      // avoid redirect loop when already on /login
      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
      // Reset after a short delay to allow navigation
      setTimeout(() => {
        isRedirectingToLogin = false
      }, 1000)
    }
    return Promise.reject(error)
  },
)

export default request
