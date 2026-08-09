import request from '../utils/request'

export interface HistoryQueryParams {
  deviceCode: string
  metric: string
  startTime: string
  endTime: string
  interval: string
  limit: number
}

export interface DataPoint {
  timestamp: string
  value: number
}

export interface HistoryDataVO {
  deviceCode: string
  metric: string
  interval: string
  list: DataPoint[]
}

export function queryHistory(params: HistoryQueryParams) {
  return request.get<{ code: number; data: HistoryDataVO }>('/energy/history', { params })
}

/** metric → 单位映射 */
export const METRIC_UNITS: Record<string, string> = {
  POWER: 'W',
  VOLTAGE: 'V',
  CURRENT: 'A',
  ENERGY: 'kWh',
}

export const METRICS = ['POWER', 'VOLTAGE', 'CURRENT', 'ENERGY'] as const
export const INTERVALS = ['RAW', '1m', '5m', '15m', '1h', '1d'] as const
