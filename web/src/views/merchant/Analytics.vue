<template>
  <div class="analytics">
    <!-- 时间筛选 -->
    <el-card class="filter-card">
      <el-radio-group v-model="timeRange" @change="loadData">
        <el-radio-button label="today">今日</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
        <el-radio-button label="custom">自定义</el-radio-button>
      </el-radio-group>
      <el-date-picker
        v-if="timeRange === 'custom'"
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        style="margin-left: 20px;"
        @change="loadData"
      />
    </el-card>

    <!-- 核心指标 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon revenue"><el-icon><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ overview.totalRevenue || 0 }}</div>
            <div class="stat-label">总营业额</div>
            <div class="stat-trend" :class="overview.revenueGrowth >= 0 ? 'up' : 'down'">
              {{ overview.revenueGrowth >= 0 ? '+' : '' }}{{ overview.revenueGrowth || 0 }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon orders"><el-icon><Document /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.totalOrders || 0 }}</div>
            <div class="stat-label">订单数</div>
            <div class="stat-trend" :class="overview.orderGrowth >= 0 ? 'up' : 'down'">
              {{ overview.orderGrowth >= 0 ? '+' : '' }}{{ overview.orderGrowth || 0 }}%
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon avg"><el-icon><TrendCharts /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ overview.avgOrderAmount || 0 }}</div>
            <div class="stat-label">客单价</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-icon customers"><el-icon><User /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ overview.customerCount || 0 }}</div>
            <div class="stat-label">顾客数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="16">
        <el-card>
          <template #header><span>销售趋势</span></template>
          <div ref="trendChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header><span>菜品销量TOP10</span></template>
          <div ref="dishChartRef" style="height: 350px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header><span>订单时段分布</span></template>
          <div ref="hourChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>菜品分类销售占比</span></template>
          <div ref="categoryChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 菜品销售明细 -->
    <el-card style="margin-top: 20px;">
      <template #header><span>菜品销售明细</span></template>
      <el-table :data="dishSales" v-loading="loading">
        <el-table-column prop="dishName" label="菜品名称" />
        <el-table-column prop="categoryName" label="分类" />
        <el-table-column prop="salesCount" label="销量" sortable />
        <el-table-column prop="salesAmount" label="销售额" sortable>
          <template #default="{ row }">¥{{ row.salesAmount }}</template>
        </el-table-column>
        <el-table-column prop="avgRating" label="平均评分">
          <template #default="{ row }">
            <el-rate v-model="row.avgRating" disabled show-score text-color="#ff9900" />
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../../api/request'

const timeRange = ref('week')
const dateRange = ref([])
const overview = ref({})
const dishSales = ref([])
const loading = ref(false)

const trendChartRef = ref(null)
const dishChartRef = ref(null)
const hourChartRef = ref(null)
const categoryChartRef = ref(null)

let trendChart = null
let dishChart = null
let hourChart = null
let categoryChart = null

const loadData = async () => {
  loading.value = true
  try {
    const params = { timeRange: timeRange.value }
    if (timeRange.value === 'custom' && dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }

    const [overviewRes, trendRes, dishRes, hourRes, categoryRes] = await Promise.all([
      request.get('/merchant/analytics/overview', { params }),
      request.get('/merchant/analytics/trend', { params }),
      request.get('/merchant/analytics/dish-sales', { params }),
      request.get('/merchant/analytics/hour-distribution', { params }),
      request.get('/merchant/analytics/category-sales', { params })
    ])

    overview.value = overviewRes.data || {}
    dishSales.value = dishRes.data || []

    renderTrendChart(trendRes.data || [])
    renderDishChart(dishRes.data?.slice(0, 10) || [])
    renderHourChart(hourRes.data || [])
    renderCategoryChart(categoryRes.data || [])
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}

const renderTrendChart = (data) => {
  if (!trendChart) {
    trendChart = echarts.init(trendChartRef.value)
  }
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['销售额', '订单数'] },
    xAxis: { type: 'category', data: data.map(d => d.date) },
    yAxis: [
      { type: 'value', name: '销售额(元)' },
      { type: 'value', name: '订单数' }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: data.map(d => d.revenue),
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: data.map(d => d.orders),
        itemStyle: { color: '#67C23A' }
      }
    ]
  })
}

const renderDishChart = (data) => {
  if (!dishChart) {
    dishChart = echarts.init(dishChartRef.value)
  }
  dishChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: data.map(d => d.dishName).reverse(),
      axisLabel: { width: 80, overflow: 'truncate' }
    },
    series: [{
      type: 'bar',
      data: data.map(d => d.salesCount).reverse(),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
          { offset: 0, color: '#83bff6' },
          { offset: 1, color: '#188df0' }
        ])
      }
    }]
  })
}

const renderHourChart = (data) => {
  if (!hourChart) {
    hourChart = echarts.init(hourChartRef.value)
  }
  const hours = Array.from({ length: 24 }, (_, i) => `${i}:00`)
  hourChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', name: '订单数' },
    series: [{
      type: 'line',
      data: data,
      smooth: true,
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.1)' }
        ])
      },
      itemStyle: { color: '#409EFF' }
    }]
  })
}

const renderCategoryChart = (data) => {
  if (!categoryChart) {
    categoryChart = echarts.init(categoryChartRef.value)
  }
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { orient: 'vertical', left: 'left' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: data.map(d => ({ name: d.categoryName, value: d.salesAmount }))
    }]
  })
}

const handleResize = () => {
  trendChart?.resize()
  dishChart?.resize()
  hourChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  nextTick(() => {
    loadData()
    window.addEventListener('resize', handleResize)
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  dishChart?.dispose()
  hourChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.filter-card { display: flex; align-items: center; }
.stat-card { display: flex; align-items: center; padding: 20px; }
.stat-icon {
  width: 60px; height: 60px;
  border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  font-size: 28px; color: white; margin-right: 15px;
}
.stat-icon.revenue { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-icon.orders { background: linear-gradient(135deg, #f093fb, #f5576c); }
.stat-icon.avg { background: linear-gradient(135deg, #4facfe, #00f2fe); }
.stat-icon.customers { background: linear-gradient(135deg, #43e97b, #38f9d7); }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
.stat-label { font-size: 14px; color: #999; }
.stat-trend { font-size: 12px; margin-top: 4px; }
.stat-trend.up { color: #67C23A; }
.stat-trend.down { color: #F56C6C; }
</style>
