import request from '../utils/request'

// ============ Types ============

export interface AlarmRuleVO {
  id: number
  deviceId: number | null
  ruleName: string
  metric: string
  operator: string
  threshold: number
  severity: string
  status: number
  createTime: string
  updateTime: string
}

export interface AlarmRuleQuery {
  page: number
  pageSize: number
  keyword?: string
  severity?: string
  status?: number
}

export interface AlarmRuleForm {
  deviceId?: number | null
  ruleName: string
  metric: string
  operator: string
  threshold: number
  severity: string
  status?: number
}

export interface AlarmRecordVO {
  id: number
  deviceId: number | null
  deviceCode: string
  ruleId: number | null
  ruleName: string
  metric: string
  actualValue: number
  threshold: number
  severity: string
  status: number
  alarmTime: string
  recoverTime: string | null
  remark: string | null
  createTime: string
  updateTime: string
}

export interface AlarmQuery {
  page: number
  pageSize: number
  deviceCode?: string
  status?: number
  severity?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
}

// ============ Alarm Rule APIs ============

export function getAlarmRules(params: AlarmRuleQuery) {
  return request.get<{ code: number; data: PageResult<AlarmRuleVO> }>('/alarm/rules', { params })
}

export function getAlarmRule(id: number) {
  return request.get<{ code: number; data: AlarmRuleVO }>(`/alarm/rules/${id}`)
}

export function createAlarmRule(data: AlarmRuleForm) {
  return request.post<{ code: number; data: AlarmRuleVO }>('/alarm/rules', data)
}

export function updateAlarmRule(id: number, data: AlarmRuleForm) {
  return request.put<{ code: number; data: AlarmRuleVO }>(`/alarm/rules/${id}`, data)
}

export function deleteAlarmRule(id: number) {
  return request.delete<{ code: number; data: null }>(`/alarm/rules/${id}`)
}

// ============ Alarm Record APIs ============

export function getAlarms(params: AlarmQuery) {
  return request.get<{ code: number; data: PageResult<AlarmRecordVO> }>('/alarms', { params })
}

export function getAlarm(id: number) {
  return request.get<{ code: number; data: AlarmRecordVO }>(`/alarms/${id}`)
}

export function ackAlarm(id: number) {
  return request.put<{ code: number; message: string; data: null }>(`/alarms/${id}/ack`)
}
