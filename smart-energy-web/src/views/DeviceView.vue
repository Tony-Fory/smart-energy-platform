<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { Search, Refresh, Plus, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  listDevices,
  createDevice,
  updateDevice,
  deleteDevice,
  type DeviceVO,
  type DeviceQuery,
} from '../api'
import { hasPermission } from '../api/auth'

// ---- device type options ----
const deviceTypeOptions = [
  '智能电表',
  '智能水表',
  '智能气表',
  '光伏逆变器',
  '储能设备',
  '充电桩',
  '环境传感器',
]

// ---- search state ----
const query = reactive<DeviceQuery>({
  page: 1,
  pageSize: 10,
  keyword: '',
  deviceType: '',
  status: undefined,
})

// ---- table state ----
const tableData = ref<DeviceVO[]>([])
const total = ref(0)
const loading = ref(false)

// ---- dialog state ----
const dialogVisible = ref(false)
const dialogTitle = ref('新增设备')
const editingId = ref<number | null>(null)
const formRef = ref()
const form = reactive({
  deviceCode: '',
  deviceName: '',
  deviceType: '',
  location: '',
  status: 1,
})

const formRules = {
  deviceCode: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
  deviceName: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
  deviceType: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
}

// ---- load data ----
async function fetchList() {
  loading.value = true
  try {
    const params: DeviceQuery = {
      page: query.page,
      pageSize: query.pageSize,
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.deviceType) params.deviceType = query.deviceType
    if (query.status !== undefined && query.status !== null) params.status = Number(query.status)

    const res = await listDevices(params)
    if (res.data.code === 0) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    }
  } catch (e) {
    console.error('Failed to load devices', e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.page = 1
  fetchList()
}

function handleReset() {
  query.page = 1
  query.keyword = ''
  query.deviceType = ''
  query.status = undefined
  fetchList()
}

function handlePageChange(page: number) {
  query.page = page
  fetchList()
}

function handleSizeChange(size: number) {
  query.pageSize = size
  query.page = 1
  fetchList()
}

// ---- create / edit ----
function openCreate() {
  editingId.value = null
  dialogTitle.value = '新增设备'
  form.deviceCode = ''
  form.deviceName = ''
  form.deviceType = ''
  form.location = ''
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: DeviceVO) {
  editingId.value = row.id
  dialogTitle.value = '编辑设备'
  form.deviceCode = row.deviceCode
  form.deviceName = row.deviceName
  form.deviceType = row.deviceType
  form.location = row.location || ''
  form.status = row.status
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  try {
    const data = {
      deviceCode: form.deviceCode,
      deviceName: form.deviceName,
      deviceType: form.deviceType,
      location: form.location || undefined,
      status: form.status,
    }

    let res
    if (editingId.value) {
      res = await updateDevice(editingId.value, data)
    } else {
      res = await createDevice(data)
    }

    if (res.data.code === 0) {
      ElMessage.success(editingId.value ? '设备更新成功' : '设备创建成功')
      dialogVisible.value = false
      fetchList()
    }
  } catch (e) {
    console.error('Failed to save device', e)
  }
}

// ---- delete ----
async function handleDelete(row: DeviceVO) {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备「${row.deviceName}」(${row.deviceCode}) 吗？此操作不可恢复。`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' },
    )
    const res = await deleteDevice(row.id)
    if (res.data.code === 0) {
      ElMessage.success('设备已删除')
      // if current page becomes empty after deletion, go back
      if (tableData.value.length === 1 && query.page > 1) {
        query.page--
      }
      fetchList()
    }
  } catch (e) {
    // user cancelled or error
    if (e !== 'cancel') {
      console.error('Failed to delete device', e)
    }
  }
}

// ---- status tag ----
function getStatusTag(status: number) {
  return status === 1 ? { type: 'success' as const, text: '启用' } : { type: 'info' as const, text: '停用' }
}

// ---- life cycle ----
onMounted(() => {
  fetchList()
})
</script>

<template>
  <div class="device-page">
    <h2 class="page-title">设备管理</h2>

    <!-- search bar -->
    <el-card shadow="hover" class="search-card">
      <el-row :gutter="12" align="middle">
        <el-col :xs="24" :sm="6" :md="5">
          <el-input
            v-model="query.keyword"
            placeholder="设备名称/编号"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :xs="12" :sm="5" :md="4">
          <el-select v-model="query.deviceType" placeholder="设备类型" clearable>
            <el-option
              v-for="t in deviceTypeOptions"
              :key="t"
              :label="t"
              :value="t"
            />
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

    <!-- table -->
    <el-card shadow="hover" class="table-card">
      <template #header>
        <div class="card-header">
          <span>设备列表</span>
          <el-button v-if="hasPermission('DEVICE_CREATE')" type="primary" :icon="Plus" size="small" @click="openCreate">新增设备</el-button>
        </div>
      </template>

      <el-table :data="tableData" v-loading="loading" stripe size="small">
        <el-table-column prop="deviceCode" label="设备编号" min-width="140" />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column prop="deviceType" label="设备类型" width="120" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag
              :type="getStatusTag(row.status).type"
              size="small"
              effect="dark"
            >
              {{ getStatusTag(row.status).text }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="安装位置" min-width="140">
          <template #default="{ row }">
            {{ row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="hasPermission('DEVICE_UPDATE')" type="primary" link :icon="Edit" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="hasPermission('DEVICE_DELETE')" type="danger" link :icon="Delete" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- pagination -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- create / edit dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form
        ref="formRef"
        :model="form"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="设备编号" prop="deviceCode">
          <el-input v-model="form.deviceCode" placeholder="请输入设备编号" maxlength="64" />
        </el-form-item>
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="form.deviceName" placeholder="请输入设备名称" maxlength="128" />
        </el-form-item>
        <el-form-item label="设备类型" prop="deviceType">
          <el-select v-model="form.deviceType" placeholder="请选择设备类型">
            <el-option
              v-for="t in deviceTypeOptions"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="安装位置">
          <el-input v-model="form.location" placeholder="请输入安装位置" maxlength="255" />
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
.device-page {
  padding: 20px;
}

.page-title {
  margin: 0 0 20px 0;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
}

.search-card {
  margin-bottom: 16px;
}

.search-card .el-row .el-col {
  margin-bottom: 0;
}

.table-card {
  min-height: 400px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

/* responsive: stack search controls on mobile */
@media (max-width: 768px) {
  .search-card .el-col {
    margin-bottom: 8px;
  }
}
</style>
