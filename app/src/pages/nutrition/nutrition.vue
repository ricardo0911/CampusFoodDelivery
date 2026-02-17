<template>
  <view class="page">
    <!-- 今日概览 -->
    <view class="overview-card">
      <view class="overview-header">
        <text class="overview-title">📊 今日营养摄入</text>
        <text class="overview-date">{{ today }}</text>
      </view>
      
      <!-- 热量环形图 -->
      <view class="calorie-ring">
        <view class="ring-bg">
          <view class="ring-progress" :style="{ '--progress': caloriePercentage + '%' }"></view>
        </view>
        <view class="ring-content">
          <text class="calorie-value">{{ report.consumedCalories || 0 }}</text>
          <text class="calorie-unit">千卡</text>
          <text class="calorie-target">目标 {{ report.targetCalories || 2000 }}</text>
        </view>
      </view>
      
      <text class="status-text" :class="statusClass">{{ report.statusText || '今日还未进餐' }}</text>
    </view>

    <!-- 营养详情 -->
    <view class="nutrition-detail">
      <view class="detail-title">营养成分</view>
      
      <view class="nutrient-grid">
        <view class="nutrient-item">
          <view class="nutrient-icon protein">🥩</view>
          <text class="nutrient-value">{{ report.protein || 0 }}g</text>
          <text class="nutrient-label">蛋白质</text>
        </view>
        <view class="nutrient-item">
          <view class="nutrient-icon carbs">🍚</view>
          <text class="nutrient-value">{{ report.carbs || 0 }}g</text>
          <text class="nutrient-label">碳水</text>
        </view>
        <view class="nutrient-item">
          <view class="nutrient-icon fat">🥑</view>
          <text class="nutrient-value">{{ report.fat || 0 }}g</text>
          <text class="nutrient-label">脂肪</text>
        </view>
        <view class="nutrient-item">
          <view class="nutrient-icon fiber">🥬</view>
          <text class="nutrient-value">{{ report.fiber || 0 }}g</text>
          <text class="nutrient-label">纤维</text>
        </view>
      </view>
    </view>

    <!-- 健康建议 -->
    <view class="advice-card" v-if="report.advice">
      <view class="advice-icon">💡</view>
      <text class="advice-text">{{ report.advice }}</text>
    </view>

    <!-- 周报入口 -->
    <view class="weekly-entry" @click="showWeeklyReport">
      <view class="weekly-icon">📅</view>
      <view class="weekly-info">
        <text class="weekly-title">查看周报</text>
        <text class="weekly-sub">了解一周的饮食情况</text>
      </view>
      <text class="weekly-arrow">›</text>
    </view>

    <!-- 健康目标设置 -->
    <view class="goal-setting">
      <view class="goal-title">健康目标</view>
      <view class="goal-options">
        <view 
          class="goal-option" 
          :class="{ active: healthGoal === 1 }"
          @click="setGoal(1)"
        >
          <text class="goal-icon">🔥</text>
          <text class="goal-name">减脂</text>
        </view>
        <view 
          class="goal-option" 
          :class="{ active: healthGoal === 2 }"
          @click="setGoal(2)"
        >
          <text class="goal-icon">💪</text>
          <text class="goal-name">增肌</text>
        </view>
        <view 
          class="goal-option" 
          :class="{ active: healthGoal === 3 }"
          @click="setGoal(3)"
        >
          <text class="goal-icon">⚖️</text>
          <text class="goal-name">均衡</text>
        </view>
      </view>
    </view>

    <!-- 周报弹窗 -->
    <view class="modal" v-if="showWeekly" @click.self="showWeekly = false">
      <view class="modal-content">
        <view class="modal-header">
          <text class="modal-title">📅 本周营养报告</text>
          <text class="modal-close" @click="showWeekly = false">×</text>
        </view>
        
        <view class="weekly-summary">
          <text class="summary-text">{{ weeklyReport.summary || '暂无数据' }}</text>
        </view>
        
        <view class="weekly-stats">
          <view class="stat-item">
            <text class="stat-value">{{ weeklyReport.orderCount || 0 }}</text>
            <text class="stat-label">订餐次数</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ weeklyReport.avgCalories || 0 }}</text>
            <text class="stat-label">日均热量</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ weeklyReport.achievedDays || 0 }}</text>
            <text class="stat-label">达标天数</text>
          </view>
        </view>
        
        <view class="weekly-chart">
          <view 
            class="chart-bar" 
            v-for="day in weeklyReport.dailyData || []" 
            :key="day.date"
          >
            <view 
              class="bar-fill" 
              :style="{ height: (day.calories / (report.targetCalories || 2000) * 100) + '%' }"
              :class="{ exceeded: day.status === 2, achieved: day.status === 1 }"
            ></view>
            <text class="bar-label">{{ day.dayOfWeek }}</text>
          </view>
        </view>
        
        <view class="suggestions" v-if="weeklyReport.suggestions">
          <view class="suggestion-item" v-for="(s, i) in weeklyReport.suggestions" :key="i">
            <text class="suggestion-icon">💡</text>
            <text class="suggestion-text">{{ s }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, put } from '@/utils/request'

const report = ref({})
const weeklyReport = ref({})
const healthGoal = ref(3)
const showWeekly = ref(false)

const today = computed(() => {
  const d = new Date()
  return `${d.getMonth() + 1}月${d.getDate()}日`
})

const caloriePercentage = computed(() => {
  if (!report.value.targetCalories) return 0
  return Math.min(100, (report.value.consumedCalories || 0) / report.value.targetCalories * 100)
})

const statusClass = computed(() => {
  if (report.value.status === 2) return 'exceeded'
  if (report.value.status === 1) return 'achieved'
  return ''
})

// 加载今日报告
const loadTodayReport = async () => {
  try {
    const res = await get('/customer/nutrition/today')
    if (res.code === 200 && res.data) {
      report.value = res.data
    }
  } catch (e) {
    // 模拟数据
    report.value = {
      consumedCalories: 1200,
      targetCalories: 2000,
      protein: 45,
      carbs: 150,
      fat: 40,
      fiber: 12,
      mealCount: 2,
      caloriePercentage: 60,
      status: 0,
      statusText: '还需摄入 800 千卡',
      advice: '继续保持均衡饮食！'
    }
  }
}

// 加载周报
const loadWeeklyReport = async () => {
  try {
    const res = await get('/customer/nutrition/weekly')
    if (res.code === 200 && res.data) {
      weeklyReport.value = res.data
    }
  } catch (e) {
    // 模拟数据
    weeklyReport.value = {
      orderCount: 8,
      avgCalories: 1800,
      achievedDays: 4,
      summary: '本周共订餐 8 次，4 天达标',
      suggestions: ['继续保持良好的饮食习惯！'],
      dailyData: [
        { date: '01-13', dayOfWeek: '一', calories: 1800, status: 1 },
        { date: '01-14', dayOfWeek: '二', calories: 2200, status: 2 },
        { date: '01-15', dayOfWeek: '三', calories: 1600, status: 0 },
        { date: '01-16', dayOfWeek: '四', calories: 1900, status: 1 },
        { date: '01-17', dayOfWeek: '五', calories: 2100, status: 2 },
        { date: '01-18', dayOfWeek: '六', calories: 1200, status: 0 },
      ]
    }
  }
}

// 加载用户画像
const loadProfile = async () => {
  try {
    const res = await get('/customer/nutrition/profile')
    if (res.code === 200 && res.data) {
      healthGoal.value = res.data.healthGoal || 3
    }
  } catch (e) {}
}

// 设置健康目标
const setGoal = async (goal) => {
  healthGoal.value = goal
  const targets = { 1: 1500, 2: 2500, 3: 2000 }
  try {
    await put('/customer/nutrition/health-goal', {
      healthGoal: goal,
      dailyCalorieTarget: targets[goal]
    })
    uni.showToast({ title: '目标已更新', icon: 'success' })
    loadTodayReport()
  } catch (e) {
    uni.showToast({ title: '设置失败', icon: 'none' })
  }
}

// 显示周报
const showWeeklyReport = async () => {
  await loadWeeklyReport()
  showWeekly.value = true
}

onMounted(() => {
  loadTodayReport()
  loadProfile()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: linear-gradient(180deg, #e8f5e9 0%, #f5f5f5 100%);
  padding: 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
}

/* 概览卡片 */
.overview-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  text-align: center;
  box-shadow: 0 8rpx 30rpx rgba(0,0,0,0.05);
}

.overview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30rpx;
}

.overview-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.overview-date {
  font-size: 26rpx;
  color: #999;
}

/* 热量环 */
.calorie-ring {
  position: relative;
  width: 300rpx;
  height: 300rpx;
  margin: 0 auto;
}

.ring-bg {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: conic-gradient(#2da44e var(--progress, 0%), #e8f5e9 0%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.ring-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  width: 220rpx;
  height: 220rpx;
  border-radius: 50%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.calorie-value {
  font-size: 56rpx;
  font-weight: bold;
  color: #2da44e;
}

.calorie-unit {
  font-size: 24rpx;
  color: #999;
}

.calorie-target {
  font-size: 22rpx;
  color: #ccc;
  margin-top: 8rpx;
}

.status-text {
  font-size: 28rpx;
  color: #666;
  margin-top: 20rpx;
}

.status-text.achieved {
  color: #2da44e;
}

.status-text.exceeded {
  color: #ff6b35;
}

/* 营养详情 */
.nutrition-detail {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: 24rpx;
}

.detail-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.nutrient-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.nutrient-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.nutrient-icon {
  width: 80rpx;
  height: 80rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36rpx;
  margin-bottom: 12rpx;
}

.nutrient-icon.protein { background: #fff0f0; }
.nutrient-icon.carbs { background: #fff8e1; }
.nutrient-icon.fat { background: #e8f5e9; }
.nutrient-icon.fiber { background: #e3f2fd; }

.nutrient-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.nutrient-label {
  font-size: 22rpx;
  color: #999;
}

/* 建议卡片 */
.advice-card {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: 24rpx;
}

.advice-icon {
  font-size: 40rpx;
  margin-right: 20rpx;
}

.advice-text {
  font-size: 26rpx;
  color: #666;
  flex: 1;
}

/* 周报入口 */
.weekly-entry {
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: 24rpx;
}

.weekly-icon {
  font-size: 40rpx;
}

.weekly-info {
  flex: 1;
  margin-left: 20rpx;
}

.weekly-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  display: block;
}

.weekly-sub {
  font-size: 24rpx;
  color: #999;
}

.weekly-arrow {
  font-size: 40rpx;
  color: #ccc;
}

/* 目标设置 */
.goal-setting {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-top: 24rpx;
}

.goal-title {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
  margin-bottom: 24rpx;
}

.goal-options {
  display: flex;
  gap: 20rpx;
}

.goal-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  transition: all 0.3s;
}

.goal-option.active {
  background: linear-gradient(135deg, #2da44e, #4caf50);
  box-shadow: 0 4rpx 16rpx rgba(45, 164, 78, 0.3);
}

.goal-icon {
  font-size: 40rpx;
  margin-bottom: 8rpx;
}

.goal-name {
  font-size: 26rpx;
  color: #666;
}

.goal-option.active .goal-name {
  color: #fff;
}

/* 弹窗 */
.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal-content {
  background: #fff;
  border-radius: 24rpx;
  width: 90%;
  max-height: 80vh;
  padding: 30rpx;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.modal-title {
  font-size: 32rpx;
  font-weight: bold;
}

.modal-close {
  font-size: 48rpx;
  color: #999;
}

.weekly-summary {
  text-align: center;
  padding: 20rpx 0;
}

.summary-text {
  font-size: 28rpx;
  color: #666;
}

.weekly-stats {
  display: flex;
  justify-content: space-around;
  padding: 30rpx 0;
  border-top: 1rpx solid #eee;
  border-bottom: 1rpx solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 40rpx;
  font-weight: bold;
  color: #2da44e;
  display: block;
}

.stat-label {
  font-size: 24rpx;
  color: #999;
}

.weekly-chart {
  display: flex;
  justify-content: space-around;
  align-items: flex-end;
  height: 200rpx;
  padding: 30rpx 0;
}

.chart-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 60rpx;
}

.bar-fill {
  width: 100%;
  background: #e8f5e9;
  border-radius: 8rpx 8rpx 0 0;
  min-height: 10rpx;
}

.bar-fill.achieved {
  background: #2da44e;
}

.bar-fill.exceeded {
  background: #ff6b35;
}

.bar-label {
  font-size: 22rpx;
  color: #999;
  margin-top: 12rpx;
}

.suggestions {
  margin-top: 24rpx;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 16rpx 0;
}

.suggestion-icon {
  margin-right: 12rpx;
}

.suggestion-text {
  font-size: 26rpx;
  color: #666;
}
</style>
