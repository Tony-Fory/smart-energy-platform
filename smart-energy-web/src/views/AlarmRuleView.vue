<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAlarmRules,
  createAlarmRule,
  updateAlarmRule,
  deleteAlarmRule,
  type AlarmRuleVO,
  type AlarmRuleQuery,
  type AlarmRuleForm,
} from '../api/alarm'
import { hasPermission } from '../api/auth'

const metricOptions = ['POWER', 'VOLTAGE', 'CURRENT', 'ENERGY']
const operatorOptions = ['GT', 'GTE', 'LT', 'LTE']
const severityOptions = ['INFO', 'WARNING', 'CRITICAL']

const query = reactive<AlarmRuleQuery>({ page: 1, pageSize: 10, keyword: '', severity: '', status: undefined })
const tableData = ref<AlarmRuleVO[]>([])
const total = ref(0)
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增规则')
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive<AlarmRuleForm>({
  deviceId: null,
  ruleName: '',
  metric: 'POWER',
  operator: 'GT',
  threshold: 0,
  severity: 'WARNING',
  status: 1,
})
const formRules = {
  ruleName: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  metric: [{ required: true, message: '请选择指标', trigger: 'change' }],
  operator: [{ required: true, message: '请选择运算符', trigger: 'change' }],
  threshold: [{ required: true, message: '请输入阈值', trigger: 'blur' }],
  severity: [{ required: true, message: '请选择严重级别', trigger: 'change' }],
}

async function fetchList() {
  loading.value = true
  try {
    const params: AlarmRuleQuery = { page: query.page, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.severity) params.severity = query.severity
    if (query.status !== undefined && query.status !== null) params.status = Number(query.status)
    const res = await getAlarmRules(params)
    if (res.data.code === 0) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) {
    console.error('Failed to load alarm rules', e)
  } finally { loading.value = false }
}

function handleSearch() { query.page = 1; fetchList() }
function handleReset() { query.page = 1; query.keyword = ''; query.severity = ''; query.status = undefined; fetchList() }
function handlePageChange(p: number) { query.page = p; fetchList() }
function handleSizeChange(s: number) { query.pageSize = s; query.page = 1; fetchList() }

function openCreate() {
  editingId.value = null; dialogTitle.value = '新增规则'
  form.deviceId = null; form.ruleName = ''; form.metric = 'POWER'; form.operator = 'GT'
  form.threshold = 0; form.severity = 'WARNING'; form.status = 1
  dialogVisible.value = true
}

function openEdit(row: AlarmRuleVO) {
  editingId.value = row.id; dialogTitle.value = '编辑规则'
  form.deviceId = row.deviceId; form.ruleName = row.ruleName; form.metric = row.metric
  form.operator = row.operator; form.threshold = row.threshold
  form.severity = row.severity; form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    const data: AlarmRuleForm = {
      deviceId: form.deviceId || null,
      ruleName: form.ruleName, metric: form.metric, operator: form.operator,
      threshold: Number(form.threshold), severity: form.severity, status: form.status,
    }
    let res
    if (editingId.value) {
      res = await updateAlarmRule(editingId.value, data)
    } else {
      res = await createAlarmRule(data)
    }
    if (res.data.code === 0) {
      ElMessage.success(editingId.value ? '规则更新成功' : '规则创建成功')
      dialogVisible.value = false; fetchList()
    }
  } catch (e) { console.error('Failed to save alarm rule', e) }
}

async function handleDelete(row: AlarmRuleVO) {
  try {
    await ElMessageBox.confirm(`确定删除规则「${row.ruleName}」？`, '删除确认', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning',
    })
    const res = await deleteAlarmRule(row.id)
    if (res.data.code === 0) {
      ElMessage.success('规则已删除')
      if (tableData.value.length === 1 && query.page > 1) query.page--
      fetchList()
    }
  } catch (e) { if (e !== 'cancel') console.error('Failed to delete rule', e) }
}

function severityTag(s: string) {
  const m: Record<string, string> = { INFO: 'info', WARNING: 'warning', CRITICAL: 'danger' }
  return m[s] || 'info'
}
function statusTag(s: number) {
  return s === 1 ? { type: 'success' as const, text: '启用' } : { type: 'info' as const, text: '停用' }
}

onMounted(() => fetchList())
</script>

<template>
  <div class="alarm-rule-page">
    <h2 class="page-title">告警规则</h2>

    <el-card shadow="hover" class="search-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="6" :md="5">
          <el-input v-model="query.keyword" placeholder="规则名称" clearable @keyup.enter="handleSearch" />
        </el-col>
        <el-col :xs="12" :sm="5" :md="4">
          <el-select v-model="query.severity" placeholder="严重级别" clearable>
            <el-option v-for="s in severityOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-col>
        <el-col :xs="12" :sm="5" :md="3">
          <el-select v-model="query.status" placeholder="状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="5">
          <el-button type="primary" :icon="Search" @click="handleSearch">查询</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-col>
      </el-row>
    </el-card>

    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>规则列表</span>
          <el-button v-if="hasPermission('ALARM_RULE_CREATE')" type="primary" :icon="Plus" size="small" @click="openCreate">新增规则</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe size="small">
        <el-table-column prop="ruleName" label="规则名称" min-width="140" />
        <el-table-column label="设备ID" width="90">
          <template #default="{ row }">{{ row.deviceId ?? '全局' }}</template>
        </el-table-column>
        <el-table-column prop="metric" label="指标" width="90" />
        <el-table-column prop="operator" label="运算符" width="70" />
        <el-table-column prop="threshold" label="阈值" width="80" />
        <el-table-column label="严重级别" width="90">
          <template #default="{ row }">
            <el-tag :type="severityTag(row.severity)" size="small" effect="dark">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="70">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status).type" size="small" effect="dark">{{ statusTag(row.status).text }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('ALARM_RULE_UPDATE')" type="primary" link :icon="Edit" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('ALARM_RULE_DELETE')" type="danger" link :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="规则名称" prop="ruleName">
          <el-input v-model="form.ruleName" placeholder="请输入规则名称" maxlength="100" />
        </el-form-item>
        <el-form-item label="设备ID">
          <el-input v-model="form.deviceId" placeholder="留空表示全局规则" />
        </el-form-item>
        <el-form-item label="指标" prop="metric">
          <el-select v-model="form.metric" placeholder="请选择指标">
            <el-option v-for="m in metricOptions" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
        <el-form-item label="运算符" prop="operator">
          <el-select v-model="form.operator" placeholder="请选择运算符">
            <el-option v-for="o in operatorOptions" :key="o" :label="o" :value="o" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值" prop="threshold">
          <el-input-number v-model="form.threshold" :min="0" :precision="1" style="width:100%" />
        </el-form-item>
        <el-form-item label="严重级别" prop="severity">
          <el-select v-model="form.severity" placeholder="请选择级别">
            <el-option v-for="s in severityOptions" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.alarm-rule-page { padding: 20px; }
.page-title { margin: 0 0 20px 0; font-size: 22px; font-weight: 600; color: #303133; }
.search-card { margin-bottom: 16px; }
.table-card { min-height: 400px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
