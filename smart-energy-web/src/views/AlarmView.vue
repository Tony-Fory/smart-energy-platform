<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAlarms, getAlarm, ackAlarm, type AlarmRecordVO, type AlarmQuery } from '../api/alarm'
import { hasPermission } from '../api/auth'

const severityOptions = ['INFO', 'WARNING', 'CRITICAL']
const statusOptions = [
  { value: 0, label: '未处理' },
  { value: 1, label: '已确认' },
  { value: 2, label: '已恢复' },
]

const query = reactive<AlarmQuery>({ page: 1, pageSize: 10, deviceCode: '', status: undefined, severity: '' })
const tableData = ref<AlarmRecordVO[]>([])
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detail = ref<AlarmRecordVO | null>(null)

async function fetchList() {
  loading.value = true
  try {
    const params: AlarmQuery = { page: query.page, pageSize: query.pageSize }
    if (query.deviceCode) params.deviceCode = query.deviceCode
    if (query.severity) params.severity = query.severity
    if (query.status !== undefined && query.status !== null) params.status = Number(query.status)
    const res = await getAlarms(params)
    if (res.data.code === 0) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) { console.error('Failed to load alarms', e) }
  finally { loading.value = false }
}

function handleSearch() { query.page = 1; fetchList() }
function handleReset() { query.page = 1; query.deviceCode = ''; query.severity = ''; query.status = undefined; fetchList() }
function handlePageChange(p: number) { query.page = p; fetchList() }
function handleSizeChange(s: number) { query.pageSize = s; query.page = 1; fetchList() }

async function openDetail(row: AlarmRecordVO) {
  try {
    const res = await getAlarm(row.id)
    if (res.data.code === 0) detail.value = res.data.data
    detailVisible.value = true
  } catch (e) { console.error('Failed to load alarm detail', e) }
}

async function handleAck(row: AlarmRecordVO) {
  try {
    const res = await ackAlarm(row.id)
    if (res.data.code === 0) {
      ElMessage.success('告警已确认')
      fetchList()
    } else {
      ElMessage.error(res.data.message || '确认失败')
    }
  } catch (e: unknown) {
    const err = e as { response?: { data?: { message?: string } } }
    ElMessage.error(err?.response?.data?.message || '确认失败')
  }
}

function severityTag(s: string) {
  const m: Record<string, string> = { INFO: 'info', WARNING: 'warning', CRITICAL: 'danger' }
  return m[s] || 'info'
}

function alarmStatusTag(s: number) {
  switch (s) {
    case 0: return { type: 'danger' as const, text: '未处理' }
    case 1: return { type: 'warning' as const, text: '已确认' }
    case 2: return { type: 'success' as const, text: '已恢复' }
    default: return { type: 'info' as const, text: '未知' }
  }
}

onMounted(() => fetchList())
</script>

<template>
  <div class="alarm-page">
    <h2 class="page-title">告警中心</h2>

    <el-card shadow="hover" class="search-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="6" :md="5">
          <el-input v-model="query.deviceCode" placeholder="设备编号" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="12" :sm="5" :md="4">
          <el-select v-model="query.severity" placeholder="严重级别" clearable>
            <el-option v-for="s in severityOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="5" :md="4">
          <el-select v-model="query.status" placeholder="状态" clearable>
            <el-option v-for="s in statusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="5">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <template #header><span>告警记录</span></template>
      <el-table :data="tableData" v-loading="loading" stripe size="small">
        <el-table-column prop="deviceCode" label="设备编号" width="130" />
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column prop="metric" label="指标" width="80" />
        <el-table-column label="实际值" width="90">
          <template #default="{ row }">{{ row.actualValue }}</template>
        </el-table-column>
        <el-table-column label="阈值" width="80">
          <template #default="{ row }">{{ row.threshold }}</template>
        </el-table-column>
        <el-table-column label="严重级别" width="90">
          <template #default="{ row }">
            <el-tag :type="severityTag(row.severity)" size="small" effect="dark">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="alarmStatusTag(row.status).type" size="small" effect="dark">
              {{ alarmStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alarmTime" label="告警时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" size="small" @click="openDetail(row)">详情</el-button>
            <el-button
              v-if="hasPermission('ALARM_ACK') && row.status === 0"
              type="success" link size="small" @click="handleAck(row)"
            >确认</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page" v-model:page-size="query.pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper" background
          @current-change="handlePageChange" @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="告警详情" width="500px" destroy-on-close>
      <el-descriptions v-if="detail" :column="2" border size="small">
        <el-descriptions-item label="设备编号">{{ detail.deviceCode }}</el-descriptions-item>
        <el-descriptions-item label="规则名称">{{ detail.ruleName }}</el-descriptions-item>
        <el-descriptions-item label="指标">{{ detail.metric }}</el-descriptions-item>
        <el-descriptions-item label="实际值">{{ detail.actualValue }}</el-descriptions-item>
        <el-descriptions-item label="阈值">{{ detail.threshold }}</el-descriptions-item>
        <el-descriptions-item label="严重级别">
          <el-tag :type="severityTag(detail.severity)" size="small">{{ detail.severity }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="alarmStatusTag(detail.status).type" size="small">{{ alarmStatusTag(detail.status).text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="告警时间">{{ detail.alarmTime }}</el-descriptions-item>
        <el-descriptions-item label="恢复时间" :span="2">{{ detail.recoverTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.alarm-page { padding: 20px; }
.page-title { margin: 0 0 20px 0; font-size: 22px; font-weight: 600; color: #303133; }
.search-card { margin-bottom: 16px; }
.table-card { min-height: 400px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
