<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as echarts from 'echarts'
import { Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { listDevices, type DeviceVO } from '../api'
import {
  queryHistory,
  METRIC_UNITS,
  METRICS,
  INTERVALS,
  type HistoryQueryParams,
  type DataPoint,
} from '../api/history'

const devices = ref<DeviceVO[]>([])
const deviceCode = ref('')
const metric = ref<string>('POWER')
const interval = ref<string>('RAW')
const dateRange = ref<[Date, Date] | null>(null)
const loading = ref(false)
const points = ref<DataPoint[]>([])
const count = ref(0)
const currentQuery = ref('')

let chartInstance: echarts.ECharts | null = null

// ---- load devices ----
async function loadDevices() {
  try {
    const res = await listDevices({ page: 1, pageSize: 100 })
    if (res.data.code === 0) {
      devices.value = res.data.data.records
      if (devices.value.length > 0 && !deviceCode.value) {
        deviceCode.value = devices.value[0].deviceCode
      }
    }
  } catch (e) {
    console.error('Failed to load devices', e)
  }
}

// ---- validation ----
function validateParams(params: HistoryQueryParams): string | null {
  if (!params.deviceCode) return '请选择设备'
  if (!params.startTime || !params.endTime) return '请选择时间范围'
  const start = new Date(params.startTime).getTime()
  const end = new Date(params.endTime).getTime()
  const now = Date.now()
  if (start > now) return '开始时间不能是未来时间'
  if (end > now) return '结束时间不能是未来时间'
  if (start >= end) return '开始时间必须早于结束时间'
  const days = (end - start) / 86400000
  if (params.interval === 'RAW' && days > 7) return 'RAW 模式最大查询范围为 7 天'
  if (params.interval !== 'RAW' && days > 90) return '聚合模式最大查询范围为 90 天'
  return null
}

// ---- query ----
async function handleQuery() {
  const start = dateRange.value?.[0]
  const end = dateRange.value?.[1]
  const params: HistoryQueryParams = {
    deviceCode: deviceCode.value,
    metric: metric.value,
    startTime: start ? formatLocal(start) : '',
    endTime: end ? formatLocal(end) : '',
    interval: interval.value,
    limit: 500,
  }
  const err = validateParams(params)
  if (err) { ElMessage.warning(err); return }

  loading.value = true
  try {
    const res = await queryHistory(params)
    if (res.data.code === 0) {
      points.value = res.data.data.list
      count.value = points.value.length
      currentQuery.value = `${deviceCode.value} / ${metric.value} / ${interval.value}`
      renderChart()
    } else {
      ElMessage.error(res.data.code ? '查询失败' : '查询失败')
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '查询失败')
  } finally { loading.value = false }
}

function formatLocal(d: Date): string {
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// ---- chart ----
function renderChart() {
  const el = document.getElementById('history-chart')
  if (!el) return
  if (!chartInstance) chartInstance = echarts.init(el)

  const times = points.value.map((p) => p.timestamp)
  const values = points.value.map((p) => p.value)
  const unit = METRIC_UNITS[metric.value] || ''

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (p: unknown) => {
        const items = p as { axisValueLabel: string; value: number }[]
        return `${items[0].axisValueLabel}<br/>${metric.value}: ${items[0].value} ${unit}`
      },
    },
    grid: { left: 60, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: times, axisLabel: { rotate: 30, fontSize: 10 } },
    yAxis: { type: 'value', name: unit, nameTextStyle: { fontSize: 11 } },
    series: [{
      type: 'line', data: values, smooth: true,
      lineStyle: { color: '#409eff', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.35)' },
          { offset: 1, color: 'rgba(64,158,255,0.02)' },
        ]),
      },
      symbol: 'none',
    }],
  })
}

function handleResize() { chartInstance?.resize() }

function handleIntervalChange() {
  // Reset validation state; date range limit is checked in validateParams
}

watch(metric, () => { if (points.value.length) renderChart() })

onMounted(() => {
  loadDevices()
  window.addEventListener('resize', handleResize)
})
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<template>
  <div class="history-page">
    <h2 class="page-title">历史数据</h2>

    <!-- Query bar -->
    <el-card shadow="hover" class="query-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="12" :md="4">
          <el-select v-model="deviceCode" placeholder="设备" style="width:100%">
            <el-option v-for="d in devices" :key="d.deviceCode" :label="d.deviceName || d.deviceCode" :value="d.deviceCode" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="6" :md="3">
          <el-select v-model="metric" placeholder="指标" style="width:100%">
            <el-option v-for="m in METRICS" :key="m" :label="m" :value="m" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="6" :md="3">
          <el-select v-model="interval" placeholder="粒度" style="width:100%" @change="handleIntervalChange">
            <el-option v-for="i in INTERVALS" :key="i" :label="i" :value="i" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="12" :md="8">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            :default-time="[new Date(2000, 1, 1, 0, 0, 0), new Date(2000, 1, 1, 23, 59, 59)]"
            style="width:100%"
          />
        </el-col>
        <el-col :xs="24" :sm="6" :md="3">
          <el-button type="primary" :icon="Search" :loading="loading" @click="handleQuery">查询</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- Chart -->
    <el-card shadow="hover" class="chart-card" v-loading="loading">
      <template #header>
        <div class="card-header">
          <span v-if="currentQuery">查询: {{ currentQuery }} · {{ count }} 条数据</span>
          <span v-else>请选择条件后点击查询</span>
        </div>
      </template>
      <div v-if="!loading && points.length === 0" class="empty-hint">
        <p v-if="currentQuery">暂无数据</p>
        <p v-else>选择设备、指标、时间范围后查询</p>
      </div>
      <div id="history-chart" class="chart-container"></div>
    </el-card>
  </div>
</template>

<style scoped>
.history-page { padding: 20px; }
.page-title { margin: 0 0 20px 0; font-size: 22px; font-weight: 600; color: #303133; }
.query-card { margin-bottom: 16px; }
.chart-card { min-height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; font-size: 14px; color: #606266; }
.chart-container { width: 100%; height: 440px; }
.empty-hint { display: flex; align-items: center; justify-content: center; height: 300px; color: #909399; }
</style>
