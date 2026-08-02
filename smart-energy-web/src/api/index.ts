import request from '../utils/request'

// ============ Dashboard ============

export interface DashboardOverview {
  deviceCount: number
  onlineCount: number
  totalPower: number
  todayEnergy: number
}

export interface DeviceStatus {
  deviceCode: string
  voltage: number
  current: number
  power: number
  energy: number
  online: boolean
  updateTime: string
}

export interface PowerPoint {
  collectTime: string
  totalPower: number
}

export interface PowerTrend {
  list: PowerPoint[]
}

export function getDashboardOverview() {
  return request.get<{ code: number; data: DashboardOverview }>('/dashboard/overview')
}

export function getDeviceStatusList() {
  return request.get<{ code: number; data: DeviceStatus[] }>('/dashboard/device-status')
}

export function getPowerTrend() {
  return request.get<{ code: number; data: PowerTrend }>('/dashboard/power-trend')
}

// ============ Device Management ============

export interface DeviceVO {
  id: number
  deviceCode: string
  deviceName: string
  deviceType: string
  location: string
  status: number
  createTime: string
  updateTime: string
}

export interface PageResult<T> {
  records: T[]
  total: number
}

export interface DeviceQuery {
  page: number
  pageSize: number
  keyword?: string
  deviceType?: string
  status?: number
}

export function listDevices(params: DeviceQuery) {
  return request.get<{ code: number; data: PageResult<DeviceVO> }>('/devices', { params })
}

export function getDevice(id: number) {
  return request.get<{ code: number; data: DeviceVO }>(`/devices/${id}`)
}

export function createDevice(data: {
  deviceCode: string
  deviceName: string
  deviceType: string
  location?: string
  status?: number
}) {
  return request.post<{ code: number; data: DeviceVO }>('/devices', data)
}

export function updateDevice(id: number, data: {
  deviceCode: string
  deviceName: string
  deviceType: string
  location?: string
  status?: number
}) {
  return request.put<{ code: number; data: DeviceVO }>(`/devices/${id}`, data)
}

export function deleteDevice(id: number) {
  return request.delete<{ code: number; data: null }>(`/devices/${id}`)
}
