<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon user"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.userCount || 0 }}</div>
            <div class="stat-label">用户总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon merchant"><el-icon><OfficeBuilding /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.merchantCount || 0 }}</div>
            <div class="stat-label">商家总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon order"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.orderCount || 0 }}</div>
            <div class="stat-label">订单总数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon today"><el-icon><Calendar /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.todayOrders || 0 }}</div>
            <div class="stat-label">今日订单</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="chart-card" style="margin-top: 20px;">
      <template #header><span>欢迎使用校园订餐系统管理后台</span></template>
      <p>您可以在此管理用户、商家、订单等数据。</p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../api/request'

const stats = ref({})

onMounted(async () => {
  try {
    const res = await request.get('/admin/dashboard')
    stats.value = res.data
  } catch (e) { console.error(e) }
})
</script>

<style scoped>
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon {
  width: 60px; height: 60px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: white; margin-right: 15px;
}
.stat-icon.user { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.merchant { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.order { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.today { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
</style>
