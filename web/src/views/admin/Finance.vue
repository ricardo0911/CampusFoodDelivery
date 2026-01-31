<template>
  <div class="page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value">¥{{ overview.totalRevenue || 0 }}</div>
          <div class="stat-label">总营业额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card commission">
          <div class="stat-value">¥{{ overview.totalCommission || 0 }}</div>
          <div class="stat-label">总佣金</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card month">
          <div class="stat-value">¥{{ overview.monthRevenue || 0 }}</div>
          <div class="stat-label">本月营业额</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today">
          <div class="stat-value">¥{{ overview.todayRevenue || 0 }}</div>
          <div class="stat-label">今日营业额</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>佣金统计</span>
          <div class="header-actions">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" @change="loadCommission" />
            <el-button type="primary" @click="exportReport">导出报表</el-button>
          </div>
        </div>
      </template>

      <el-table :data="commissionData" stripe>
        <el-table-column prop="shopName" label="店铺名称" />
        <el-table-column prop="revenue" label="营业额">
          <template #default="{ row }">¥{{ row.revenue }}</template>
        </el-table-column>
        <el-table-column prop="commission" label="佣金">
          <template #default="{ row }">¥{{ row.commission }}</template>
        </el-table-column>
      </el-table>

      <div class="summary">
        <span>合计营业额: ¥{{ totalRevenue }}</span>
        <span>合计佣金: ¥{{ totalCommission }}</span>
      </div>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>商家结算</span>
          <el-date-picker v-model="settlementMonth" type="month" placeholder="选择月份" @change="loadSettlement" />
        </div>
      </template>

      <el-descriptions :column="3" border v-if="settlement">
        <el-descriptions-item label="结算月份">{{ settlement.month }}</el-descriptions-item>
        <el-descriptions-item label="订单数">{{ settlement.orderCount }}</el-descriptions-item>
        <el-descriptions-item label="总营业额">¥{{ settlement.totalRevenue }}</el-descriptions-item>
        <el-descriptions-item label="平台佣金">¥{{ settlement.commission }}</el-descriptions-item>
        <el-descriptions-item label="应结算金额">¥{{ settlement.settlement }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../../api/request'

const overview = ref({})
const commissionData = ref([])
const settlement = ref(null)
const dateRange = ref(null)
const settlementMonth = ref(null)

const totalRevenue = computed(() => commissionData.value.reduce((sum, item) => sum + parseFloat(item.revenue || 0), 0).toFixed(2))
const totalCommission = computed(() => commissionData.value.reduce((sum, item) => sum + parseFloat(item.commission || 0), 0).toFixed(2))

const loadOverview = async () => {
  try {
    const res = await request.get('/admin/finance/overview')
    overview.value = res.data || {}
  } catch (e) {
    console.error(e)
  }
}

const loadCommission = async () => {
  try {
    const params = {}
    if (dateRange.value) {
      params.startDate = dateRange.value[0].toISOString().split('T')[0]
      params.endDate = dateRange.value[1].toISOString().split('T')[0]
    }
    const res = await request.get('/admin/finance/commission', { params })
    commissionData.value = res.data?.shopData || []
  } catch (e) {
    console.error(e)
  }
}

const loadSettlement = async () => {
  try {
    const params = {}
    if (settlementMonth.value) {
      const date = new Date(settlementMonth.value)
      params.month = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
    }
    const res = await request.get('/admin/finance/settlement', { params })
    settlement.value = res.data || null
  } catch (e) {
    console.error(e)
  }
}

const exportReport = async () => {
  if (!dateRange.value) {
    ElMessage.warning('请选择日期范围')
    return
  }
  try {
    const startDate = dateRange.value[0].toISOString().split('T')[0]
    const endDate = dateRange.value[1].toISOString().split('T')[0]
    window.open(`/api/admin/finance/export?type=daily&startDate=${startDate}&endDate=${endDate}`)
  } catch (e) {
    ElMessage.error('导出失败')
  }
}

onMounted(() => {
  loadOverview()
  loadCommission()
  loadSettlement()
})
</script>

<style scoped>
.stats-row { margin-bottom: 20px; }
.stat-card { text-align: center; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; margin-top: 5px; }
.stat-card.commission .stat-value { color: #E6A23C; }
.stat-card.month .stat-value { color: #409EFF; }
.stat-card.today .stat-value { color: #67C23A; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.summary { margin-top: 20px; padding: 15px; background: #f5f7fa; border-radius: 4px; display: flex; gap: 30px; font-weight: bold; }
</style>
