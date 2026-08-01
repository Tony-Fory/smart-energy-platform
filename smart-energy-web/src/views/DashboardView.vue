<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import { Monitor, CircleCheck, Lightning, TrendCharts } from '@element-plus/icons-vue'
import {
  getDashboardOverview,
  getDeviceStatusList,
  getPowerTrend,
  type DashboardOverview,
  type DeviceStatus,
  type PowerPoint,
} from '../api'

// ---- state ----
const overview = ref<DashboardOverview>({
  deviceCount: 0,
  onlineCount: 0,
  totalPower: 0,
  todayEnergy: 0,
})
const deviceStatuses = ref<DeviceStatus[]>([])
const powerList = ref<PowerPoint[]>([])

let chartInstance: echarts.ECharts | null = null
let refreshTimer: ReturnType<typeof setInterval> | null = null

// ---- load data ----
async function loadOverview() {
  try {
    const res = await getDashboardOverview()
    if (res.data.code === 0) {
      overview.value = res.data.data
    }
  } catch (e) {
    console.error('Failed to load overview', e)
  }
}

async function loadDeviceStatus() {
  try {
    const res = await getDeviceStatusList()
    if (res.data.code === 0) {
      deviceStatuses.value = res.data.data
    }
  } catch (e) {
    console.error('Failed to load device status', e)
  }
}

async function loadPowerTrend() {
  try {
    const res = await getPowerTrend()
    if (res.data.code === 0 && res.data.data.list) {
      powerList.value = res.data.data.list
      renderChart()
    }
  } catch (e) {
    console.error('Failed to load power trend', e)
  }
}

async function refreshAll() {
  await Promise.all([loadOverview(), loadDeviceStatus(), loadPowerTrend()])
}

// ---- chart ----
function renderChart() {
  const el = document.getElementById('power-chart')
  if (!el) return

  if (!chartInstance) {
    chartInstance = echarts.init(el)
  }

  const times = powerList.value.map((p) => p.collectTime)
  const values = powerList.value.map((p) => p.totalPower)

  chartInstance.setOption({
    tooltip: {
      trigger: 'axis',
      formatter: (params: unknown[]) => {
        const p = params as { axisValueLabel: string; value: number }[]
        return `${p[0].axisValueLabel}<br/>功率: ${p[0].value} W`
      },
    },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: times,
      axisLabel: { rotate: 30, fontSize: 10 },
    },
    yAxis: {
      type: 'value',
      name: 'W',
      nameTextStyle: { fontSize: 11 },
    },
    series: [
      {
        type: 'line',
        data: values,
        smooth: true,
        lineStyle: { color: '#409eff', width: 2 },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64,158,255,0.35)' },
            { offset: 1, color: 'rgba(64,158,255,0.02)' },
          ]),
        },
        symbol: 'none',
      },
    ],
  })
}

function handleResize() {
  chartInstance?.resize()
}

// ---- life cycle ----
onMounted(() => {
  refreshAll()
  refreshTimer = setInterval(refreshAll, 10_000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', handleResize)
  chartInstance?.dispose()
})
</script>

<template>
  <div class="dashboard">
    <h2 class="dashboard-title">实时监控面板</h2>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <el-statistic title="设备总数" :value="overview.deviceCount">
            <template #prefix>
              <el-icon :size="20"><Monitor /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <el-statistic title="在线设备" :value="overview.onlineCount">
            <template #prefix>
              <el-icon :size="20" color="#67c23a"><CircleCheck /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <el-statistic title="总功率 (W)" :value="overview.totalPower">
            <template #prefix>
              <el-icon :size="20" color="#e6a23c"><Lightning /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="6">
        <el-card shadow="hover">
          <el-statistic title="累计能耗 (kWh)" :value="overview.todayEnergy" :precision="2">
            <template #prefix>
              <el-icon :size="20" color="#409eff"><TrendCharts /></el-icon>
            </template>
          </el-statistic>
        </el-card>
      </el-col>
    </el-row>

    <!-- 功率曲线 + 设备状态 -->
    <el-row :gutter="16" class="content-row">
      <el-col :span="24" :md="14">
        <el-card shadow="hover">
          <template #header>功率趋势 (最近 1 小时)</template>
          <div id="power-chart" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="24" :md="10">
        <el-card shadow="hover">
          <template #header>设备实时状态</template>
          <el-table :data="deviceStatuses" size="small" max-height="360" stripe>
            <el-table-column prop="deviceCode" label="设备编号" width="130" />
            <el-table-column prop="power" label="功率(W)" width="100" />
            <el-table-column label="在线" width="70">
              <template #default="{ row }">
                <el-tag :type="row.online ? 'success' : 'info'" size="small" effect="dark">
                  {{ row.online ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" min-width="160" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.dashboard {
  padding: 20px;
}

.dashboard-title {
  margin: 0 0 20px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.stat-row {
  margin-bottom: 16px;
}

.stat-row .el-card {
  text-align: center;
}

.content-row .el-card {
  height: 100%;
}

.chart-container {
  width: 100%;
  height: 340px;
}
</style>
