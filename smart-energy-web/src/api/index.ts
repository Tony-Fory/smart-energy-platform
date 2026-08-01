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
