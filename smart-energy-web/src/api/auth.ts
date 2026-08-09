import request from '../utils/request'

const TOKEN_KEY = 'smart_energy_token'
const PERMISSIONS_KEY = 'smart_energy_permissions'
const ROLE_KEY = 'smart_energy_role'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  roleCode: string
}

export interface MeResponse {
  userId: number
  username: string
  roleCode: string
  permissions: string[]
}

export function login(data: LoginRequest) {
  return request.post<{ code: number; message: string; data: LoginResponse }>('/auth/login', data)
}

export function getMe() {
  return request.get<{ code: number; data: MeResponse }>('/auth/me')
}

/** 从 localStorage 获取 Token */
export function getAuthToken(): string | null {
  return localStorage.getItem(TOKEN_KEY)
}

/** 获取缓存权限列表 */
export function getCachedPermissions(): string[] {
  try {
    return JSON.parse(localStorage.getItem(PERMISSIONS_KEY) || '[]')
  } catch {
    return []
  }
}

/** 获取缓存角色 */
export function getCachedRole(): string {
  return localStorage.getItem(ROLE_KEY) || ''
}

/** 检查是否有指定权限 */
export function hasPermission(code: string): boolean {
  return getCachedPermissions().includes(code)
}

/** 保存 Token + 权限到 localStorage 并设置 Axios 默认头 */
export function setAuthInfo(token: string, roleCode: string, permissions: string[]): void {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(ROLE_KEY, roleCode)
  localStorage.setItem(PERMISSIONS_KEY, JSON.stringify(permissions))
  request.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

/** 保存 Token 到 localStorage 并设置 Axios 默认头（向后兼容） */
export function setAuthToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
  request.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

/** 清除 Token 和权限 */
export function clearAuthToken(): void {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ROLE_KEY)
  localStorage.removeItem(PERMISSIONS_KEY)
  delete request.defaults.headers.common['Authorization']
}

/** 是否已登录 */
export function isAuthenticated(): boolean {
  return getAuthToken() !== null
}

/** 初始化：从 localStorage 恢复 Token */
export function initAuth(): void {
  const token = getAuthToken()
  if (token) {
    request.defaults.headers.common['Authorization'] = `Bearer ${token}`
  }
}
