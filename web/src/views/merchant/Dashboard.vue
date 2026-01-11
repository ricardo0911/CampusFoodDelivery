<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon order"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon pending"><el-icon><Bell /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.pendingOrders || 0 }}</div>
            <div class="stat-label">待处理</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon sales"><el-icon><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.todaySales || 0 }}</div>
            <div class="stat-label">今日销售</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon dish"><el-icon><Dish /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.dishCount || 0 }}</div>
            <div class="stat-label">菜品数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card style="margin-top: 20px;">
      <template #header><span>待处理订单</span></template>
      <el-table :data="pendingList" v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="contactName" label="联系人" />
        <el-table-column prop="payAmount" label="金额">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="accept(row)">接单</el-button>
            <el-button type="danger" size="small" @click="reject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const stats = ref({ todayOrders: 0, pendingOrders: 0, todaySales: 0, dishCount: 0 })
const pendingList = ref([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/merchant/order/list', { params: { status: 1 } })
    pendingList.value = res.data
    stats.value.pendingOrders = res.data.length
  } catch (e) { console.error(e) }
  loading.value = false
}

const accept = async (row) => {
  try {
    await request.post(`/merchant/order/${row.id}/accept`, { estimatedTime: 15 })
    ElMessage.success('接单成功')
    loadData()
  } catch (e) { console.error(e) }
}

const reject = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝订单')
  try {
    await request.post(`/merchant/order/${row.id}/reject`, { reason: value })
    ElMessage.success('已拒绝')
    loadData()
  } catch (e) { console.error(e) }
}

onMounted(loadData)
</script>

<style scoped>
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon {
  width: 60px; height: 60px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: white; margin-right: 15px;
}
.stat-icon.order { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.pending { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.sales { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.dish { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
</style>
