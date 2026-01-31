<template>
  <div class="page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.total || 0 }}</div>
          <div class="stat-label">总投诉数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card pending">
          <div class="stat-value">{{ stats.pending || 0 }}</div>
          <div class="stat-label">待处理</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card processing">
          <div class="stat-value">{{ stats.processing || 0 }}</div>
          <div class="stat-label">处理中</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card resolved">
          <div class="stat-value">{{ stats.resolved || 0 }}</div>
          <div class="stat-label">已解决</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>投诉列表</span>
          <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadData">
            <el-option :value="0" label="待处理" />
            <el-option :value="1" label="处理中" />
            <el-option :value="2" label="已解决" />
          </el-select>
        </div>
      </template>

      <el-table :data="complaints" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" width="180" />
        <el-table-column prop="userName" label="投诉人" width="120" />
        <el-table-column prop="shopName" label="店铺" width="150" />
        <el-table-column prop="reason" label="投诉原因" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showDetail(row)">查看</el-button>
            <el-button size="small" type="primary" v-if="row.status < 2" @click="showHandle(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="投诉详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="投诉人">{{ detail.userName }}</el-descriptions-item>
        <el-descriptions-item label="店铺">{{ detail.shopName }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ getStatusName(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="投诉原因" :span="2">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="处理结果" :span="2" v-if="detail.result">{{ detail.result }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理投诉" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理结果">
          <el-input v-model="handleForm.result" type="textarea" :rows="4" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="warning" @click="handleRefund">同意退款</el-button>
        <el-button type="primary" @click="handleSubmit">提交处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const complaints = ref([])
const stats = ref({})
const page = ref(1)
const total = ref(0)
const statusFilter = ref(null)
const detailVisible = ref(false)
const handleVisible = ref(false)
const detail = ref({})
const handleForm = ref({})
const currentId = ref(null)

const getStatusName = (status) => {
  const names = { 0: '待处理', 1: '处理中', 2: '已解决' }
  return names[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'danger', 1: 'warning', 2: 'success' }
  return tags[status] || 'info'
}

const loadData = async () => {
  try {
    const params = { page: page.value, size: 10 }
    if (statusFilter.value !== null) params.status = statusFilter.value

    const res = await request.get('/admin/complaint/list', { params })
    complaints.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (e) {
    console.error(e)
  }
}

const loadStats = async () => {
  try {
    const res = await request.get('/admin/complaint/stats')
    stats.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const showDetail = async (row) => {
  try {
    const res = await request.get(`/admin/complaint/${row.id}`)
    detail.value = res.data || {}
    detailVisible.value = true
  } catch (e) {
    console.error(e)
  }
}

const showHandle = (row) => {
  currentId.value = row.id
  handleForm.value = { result: '' }
  handleVisible.value = true
}

const handleSubmit = async () => {
  if (!handleForm.value.result) {
    ElMessage.warning('请输入处理结果')
    return
  }
  try {
    await request.post(`/admin/complaint/${currentId.value}/handle`, handleForm.value)
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    ElMessage.error('处理失败')
  }
}

const handleRefund = async () => {
  try {
    await ElMessageBox.confirm('确定同意退款？', '提示')
    await request.post(`/admin/complaint/${currentId.value}/refund`)
    ElMessage.success('退款处理成功')
    handleVisible.value = false
    loadData()
    loadStats()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('处理失败')
  }
}

onMounted(() => {
  loadData()
  loadStats()
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; }
.stat-value { font-size: 32px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 5px; }
.stat-card.pending .stat-value { color: #E6A23C; }
.stat-card.processing .stat-value { color: #409EFF; }
.stat-card.resolved .stat-value { color: #67C23A; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>
