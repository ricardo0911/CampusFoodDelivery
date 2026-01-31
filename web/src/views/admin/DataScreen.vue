<template>
  <div class="data-screen">
    <div class="screen-header">
      <h1>校园订餐系统数据大屏</h1>
      <div class="header-time">{{ currentTime }}</div>
    </div>

    <div class="screen-content">
      <!-- 左侧 -->
      <div class="left-panel">
        <div class="panel-card">
          <div class="card-title">实时数据</div>
          <div class="realtime-stats">
            <div class="stat-item">
              <div class="stat-value">{{ realtime.todayOrders || 0 }}</div>
              <div class="stat-label">今日订单</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">¥{{ realtime.todayRevenue || 0 }}</div>
              <div class="stat-label">今日营业额</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ realtime.todayNewUsers || 0 }}</div>
              <div class="stat-label">新增用户</div>
            </div>
            <div class="stat-item">
              <div class="stat-value">{{ realtime.pendingOrders || 0 }}</div>
              <div class="stat-label">待处理订单</div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="card-title">订单状态分布</div>
          <div class="order-stats">
            <div class="order-stat" v-for="(value, key) in todayStats.orderStats" :key="key">
              <div class="stat-bar">
                <div class="bar-fill" :style="{ width: getBarWidth(value) + '%' }"></div>
              </div>
              <div class="stat-info">
                <span class="stat-name">{{ getStatusName(key) }}</span>
                <span class="stat-count">{{ value }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 中间 -->
      <div class="center-panel">
        <div class="panel-card large">
          <div class="card-title">销售趋势</div>
          <div class="chart-container" ref="trendChart"></div>
        </div>

        <div class="summary-row">
          <div class="summary-item">
            <div class="summary-icon users"></div>
            <div class="summary-info">
              <div class="summary-value">{{ todayStats.totalUsers || 0 }}</div>
              <div class="summary-label">总用户数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon merchants"></div>
            <div class="summary-info">
              <div class="summary-value">{{ todayStats.totalMerchants || 0 }}</div>
              <div class="summary-label">商家数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon shops"></div>
            <div class="summary-info">
              <div class="summary-value">{{ todayStats.totalShops || 0 }}</div>
              <div class="summary-label">店铺数</div>
            </div>
          </div>
          <div class="summary-item">
            <div class="summary-icon orders"></div>
            <div class="summary-info">
              <div class="summary-value">{{ todayStats.totalOrders || 0 }}</div>
              <div class="summary-label">总订单数</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧 -->
      <div class="right-panel">
        <div class="panel-card">
          <div class="card-title">店铺订单排行</div>
          <div class="rank-list">
            <div class="rank-item" v-for="(shop, index) in shopDistribution" :key="shop.shopId">
              <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
              <div class="rank-name">{{ shop.shopName }}</div>
              <div class="rank-value">{{ shop.orderCount }}单</div>
            </div>
          </div>
        </div>

        <div class="panel-card">
          <div class="card-title">最近7天趋势</div>
          <div class="mini-chart" ref="miniChart"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

const currentTime = ref('')
const realtime = ref({})
const todayStats = ref({})
const shopDistribution = ref([])
const trendData = ref([])

let trendChartInstance = null
let miniChartInstance = null
let timer = null

const trendChart = ref(null)
const miniChart = ref(null)

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN')
}

const getStatusName = (key) => {
  const names = {
    pending: '待支付',
    accepted: '待接单',
    preparing: '制作中',
    delivering: '配送中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return names[key] || key
}

const getBarWidth = (value) => {
  const max = Math.max(...Object.values(todayStats.value.orderStats || {}), 1)
  return (value / max) * 100
}

const getRankClass = (index) => {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}

const loadData = async () => {
  try {
    const [realtimeRes, statsRes, mapRes, trendRes] = await Promise.all([
      request.get('/admin/dashboard/realtime'),
      request.get('/admin/dashboard/today-stats'),
      request.get('/admin/dashboard/order-map'),
      request.get('/admin/dashboard/trend?period=week')
    ])

    realtime.value = realtimeRes.data || {}
    todayStats.value = statsRes.data || {}
    shopDistribution.value = (mapRes.data?.shopDistribution || []).slice(0, 10)
    trendData.value = trendRes.data?.data || []

    renderCharts()
  } catch (e) {
    console.error(e)
  }
}

const renderCharts = () => {
  // 趋势图
  if (trendChart.value && trendData.value.length > 0) {
    if (!trendChartInstance) {
      trendChartInstance = echarts.init(trendChart.value)
    }

    const dates = trendData.value.map(d => d.date.slice(5))
    const orders = trendData.value.map(d => d.orders)
    const revenue = trendData.value.map(d => d.revenue)

    trendChartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['订单数', '营业额'], textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        data: dates,
        axisLine: { lineStyle: { color: '#fff' } },
        axisLabel: { color: '#fff' }
      },
      yAxis: [
        { type: 'value', axisLine: { lineStyle: { color: '#fff' } }, axisLabel: { color: '#fff' }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
        { type: 'value', axisLine: { lineStyle: { color: '#fff' } }, axisLabel: { color: '#fff', formatter: '¥{value}' }, splitLine: { show: false } }
      ],
      series: [
        { name: '订单数', type: 'bar', data: orders, itemStyle: { color: '#409EFF' } },
        { name: '营业额', type: 'line', yAxisIndex: 1, data: revenue, itemStyle: { color: '#67C23A' } }
      ]
    })
  }

  // 迷你图
  if (miniChart.value && trendData.value.length > 0) {
    if (!miniChartInstance) {
      miniChartInstance = echarts.init(miniChart.value)
    }

    const newUsers = trendData.value.map(d => d.newUsers)

    miniChartInstance.setOption({
      grid: { left: 0, right: 0, top: 10, bottom: 0 },
      xAxis: { type: 'category', show: false },
      yAxis: { type: 'value', show: false },
      series: [{
        type: 'line',
        data: newUsers,
        smooth: true,
        areaStyle: { color: 'rgba(64, 158, 255, 0.3)' },
        lineStyle: { color: '#409EFF' },
        itemStyle: { color: '#409EFF' }
      }]
    })
  }
}

onMounted(() => {
  updateTime()
  loadData()
  timer = setInterval(() => {
    updateTime()
    loadData()
  }, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  if (trendChartInstance) trendChartInstance.dispose()
  if (miniChartInstance) miniChartInstance.dispose()
})
</script>

<style scoped>
.data-screen {
  min-height: 100vh;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 20px;
  color: #fff;
}

.screen-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.screen-header h1 {
  font-size: 28px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-time {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.7);
}

.screen-content {
  display: flex;
  gap: 20px;
}

.left-panel, .right-panel {
  width: 300px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.center-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.panel-card {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.panel-card.large {
  flex: 1;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 15px;
  color: rgba(255, 255, 255, 0.9);
}

.realtime-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin-top: 5px;
}

.order-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-stat {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.stat-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 4px;
  transition: width 0.3s;
}

.stat-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
}

.stat-name {
  color: rgba(255, 255, 255, 0.7);
}

.stat-count {
  color: #409EFF;
}

.chart-container {
  height: 300px;
}

.summary-row {
  display: flex;
  gap: 20px;
}

.summary-item {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 15px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
}

.summary-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.summary-icon.users { background: linear-gradient(135deg, #667eea, #764ba2); }
.summary-icon.merchants { background: linear-gradient(135deg, #f093fb, #f5576c); }
.summary-icon.shops { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.summary-icon.orders { background: linear-gradient(135deg, #43e97b, #38f9d7); }

.summary-value {
  font-size: 28px;
  font-weight: 700;
}

.summary-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.6);
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.2);
}

.rank-badge.gold { background: linear-gradient(135deg, #FFD700, #FFA500); color: #333; }
.rank-badge.silver { background: linear-gradient(135deg, #C0C0C0, #A0A0A0); color: #333; }
.rank-badge.bronze { background: linear-gradient(135deg, #CD7F32, #B87333); color: #fff; }

.rank-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-value {
  font-size: 14px;
  color: #409EFF;
}

.mini-chart {
  height: 100px;
}
</style>
