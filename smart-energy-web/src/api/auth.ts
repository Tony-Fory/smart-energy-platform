import request from '../utils/request'

const TOKEN_KEY = 'smart_energy_token'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
}

export function login(data: LoginRequest) {
  return request.post<{ code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

/** 从 localStorage 获取 Token */
export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/** 保存 Token 到 localStorage 并设置 Axios 默认头 */
export function setAuthToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
  request.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

/** 清除 Token */
export function clearAuthToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  delete request.defaults.headers.common['Authorization']
}

/** 是否已登录（有 Token 即认为已登录） */
export function isAuthenticated(): boolean {
  return getAuthToken() !== null
}

/** 初始化：从 localStorage 恢复 Token 到 Axios 默认头 */
export function initAuth(): void {
  const token = getAuthToken()
  if (token) {
    request.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }
}
