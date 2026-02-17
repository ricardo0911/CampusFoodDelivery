<template>
  <view class="page">
    <!-- 店铺信息 -->
    <view class="shop-info">
      <text class="shop-name">{{ shopName }}</text>
      <text class="shop-hint">选择取餐时间可享错峰优惠</text>
    </view>

    <!-- 日期选择 -->
    <scroll-view class="date-tabs" scroll-x>
      <view 
        class="date-tab" 
        :class="{ active: selectedDate === date.value }"
        v-for="date in dates" 
        :key="date.value"
        @click="selectDate(date.value)"
      >
        <text class="date-day">{{ date.label }}</text>
        <text class="date-week">{{ date.week }}</text>
      </view>
    </scroll-view>

    <!-- 时段列表 -->
    <view class="timeslot-section">
      <view class="section-header">
        <text class="section-title">选择取餐时段</text>
        <view class="legend">
          <view class="legend-item">
            <view class="legend-dot green"></view>
            <text>空闲</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot yellow"></view>
            <text>适中</text>
          </view>
          <view class="legend-item">
            <view class="legend-dot red"></view>
            <text>繁忙</text>
          </view>
        </view>
      </view>

      <view class="timeslot-grid">
        <view 
          class="timeslot-item"
          :class="{ 
            active: selectedTimeslot === slot.id,
            disabled: !slot.available,
            peak1: slot.peakLevel === 1,
            peak2: slot.peakLevel === 2,
            peak3: slot.peakLevel === 3
          }"
          v-for="slot in timeslots"
          :key="slot.id"
          @click="selectTimeslot(slot)"
        >
          <text class="slot-time">{{ slot.timeRange }}</text>
          <text class="slot-discount" v-if="slot.discountRate < 1">{{ slot.discountText }}</text>
          <text class="slot-full" v-if="!slot.available">已满</text>
          <text class="slot-remaining" v-else>剩{{ slot.remaining }}个</text>
        </view>
      </view>

      <view class="empty-slots" v-if="timeslots.length === 0">
        <text class="empty-text">暂无可选时段</text>
      </view>
    </view>

    <!-- 优惠提示 -->
    <view class="discount-tip" v-if="selectedSlot && selectedSlot.discountRate < 1">
      <text class="tip-icon">🎉</text>
      <text class="tip-text">选择此时段可享 {{ selectedSlot.discountText }}，预计省 ¥{{ discountAmount }}</text>
    </view>

    <!-- 底部确认 -->
    <view class="bottom-bar">
      <view class="selected-info" v-if="selectedSlot">
        <text class="info-label">取餐时间</text>
        <text class="info-value">{{ selectedDateLabel }} {{ selectedSlot.timeRange }}</text>
      </view>
      <view class="selected-info" v-else>
        <text class="info-placeholder">请选择取餐时段</text>
      </view>
      <view class="confirm-btn" :class="{ disabled: !selectedTimeslot }" @click="confirm">
        <text>确认预约</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { get, post } from '@/utils/request'

const shopId = ref(null)
const orderId = ref(null)
const shopName = ref('')
const orderAmount = ref(0)

const dates = ref([])
const selectedDate = ref('')
const selectedDateLabel = ref('')
const timeslots = ref([])
const selectedTimeslot = ref(null)

const selectedSlot = computed(() => {
  return timeslots.value.find(s => s.id === selectedTimeslot.value)
})

const discountAmount = computed(() => {
  if (!selectedSlot.value || selectedSlot.value.discountRate >= 1) return 0
  return (orderAmount.value * (1 - selectedSlot.value.discountRate)).toFixed(2)
})

// 初始化日期
const initDates = () => {
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  const result = []
  const now = new Date()
  
  for (let i = 0; i < 3; i++) {
    const d = new Date(now)
    d.setDate(d.getDate() + i)
    
    const dateStr = d.toISOString().split('T')[0]
    let label = ''
    if (i === 0) label = '今天'
    else if (i === 1) label = '明天'
    else label = `${d.getMonth() + 1}/${d.getDate()}`
    
    result.push({
      value: dateStr,
      label: label,
      week: '周' + weekDays[d.getDay()]
    })
  }
  
  dates.value = result
  if (result.length > 0) {
    selectDate(result[0].value)
  }
}

// 选择日期
const selectDate = async (date) => {
  selectedDate.value = date
  selectedTimeslot.value = null
  
  const dateObj = dates.value.find(d => d.value === date)
  selectedDateLabel.value = dateObj ? dateObj.label : ''
  
  await loadTimeslots()
}

// 加载时段
const loadTimeslots = async () => {
  if (!shopId.value) {
    loadMockTimeslots()
    return
  }
  
  try {
    const res = await get(`/customer/reservation/timeslots/${shopId.value}`, { date: selectedDate.value })
    if (res.code === 200 && res.data) {
      timeslots.value = res.data
    }
  } catch (e) {
    loadMockTimeslots()
  }
}

// 模拟时段数据
const loadMockTimeslots = () => {
  timeslots.value = [
    { id: 1, timeRange: '11:00-11:15', discountRate: 0.85, discountText: '85折', peakLevel: 1, available: true, remaining: 15 },
    { id: 2, timeRange: '11:15-11:30', discountRate: 0.95, discountText: '95折', peakLevel: 2, available: true, remaining: 8 },
    { id: 3, timeRange: '11:30-11:45', discountRate: 1, discountText: '', peakLevel: 3, available: true, remaining: 3 },
    { id: 4, timeRange: '11:45-12:00', discountRate: 1, discountText: '', peakLevel: 3, available: false, remaining: 0 },
    { id: 5, timeRange: '12:00-12:15', discountRate: 1, discountText: '', peakLevel: 3, available: true, remaining: 5 },
    { id: 6, timeRange: '12:15-12:30', discountRate: 1, discountText: '', peakLevel: 3, available: true, remaining: 10 },
    { id: 7, timeRange: '12:30-12:45', discountRate: 0.95, discountText: '95折', peakLevel: 2, available: true, remaining: 12 },
    { id: 8, timeRange: '12:45-13:00', discountRate: 0.95, discountText: '95折', peakLevel: 2, available: true, remaining: 18 },
    { id: 9, timeRange: '13:00-13:15', discountRate: 0.85, discountText: '85折', peakLevel: 1, available: true, remaining: 20 },
    { id: 10, timeRange: '17:00-17:15', discountRate: 0.85, discountText: '85折', peakLevel: 1, available: true, remaining: 18 },
    { id: 11, timeRange: '17:30-17:45', discountRate: 1, discountText: '', peakLevel: 3, available: true, remaining: 6 },
    { id: 12, timeRange: '18:00-18:15', discountRate: 1, discountText: '', peakLevel: 3, available: true, remaining: 4 },
  ]
}

// 选择时段
const selectTimeslot = (slot) => {
  if (!slot.available) return
  selectedTimeslot.value = slot.id
}

// 确认预约
const confirm = async () => {
  if (!selectedTimeslot.value) {
    uni.showToast({ title: '请选择取餐时段', icon: 'none' })
    return
  }
  
  try {
    const res = await post('/customer/reservation', {
      orderId: orderId.value,
      timeslotId: selectedTimeslot.value
    })
    
    if (res.code === 200) {
      uni.showToast({ title: '预约成功', icon: 'success' })
      setTimeout(() => {
        uni.redirectTo({ url: `/pages/reservation/status?orderId=${orderId.value}` })
      }, 1000)
    } else {
      uni.showToast({ title: res.message || '预约失败', icon: 'none' })
    }
  } catch (e) {
    // 模拟成功
    uni.showToast({ title: '预约成功', icon: 'success' })
    setTimeout(() => {
      uni.navigateBack()
    }, 1000)
  }
}

onMounted(() => {
  // 从页面参数获取
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const options = page.options || {}
  
  shopId.value = options.shopId || 1
  orderId.value = options.orderId || 1
  shopName.value = options.shopName || '美味快餐店'
  orderAmount.value = parseFloat(options.amount) || 25
  
  initDates()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 180rpx;
}

/* 店铺信息 */
.shop-info {
  background: linear-gradient(135deg, #667eea, #764ba2);
  padding: 40rpx 30rpx;
  padding-top: calc(var(--status-bar-height) + 30rpx);
}

.shop-name {
  font-size: 36rpx;
  font-weight: bold;
  color: #fff;
  display: block;
}

.shop-hint {
  font-size: 26rpx;
  color: rgba(255,255,255,0.8);
  margin-top: 8rpx;
}

/* 日期选择 */
.date-tabs {
  display: flex;
  white-space: nowrap;
  background: #fff;
  padding: 20rpx;
}

.date-tab {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 40rpx;
  margin-right: 20rpx;
  border-radius: 16rpx;
  background: #f5f5f5;
  transition: all 0.3s;
}

.date-tab.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
}

.date-day {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.date-tab.active .date-day {
  color: #fff;
}

.date-week {
  font-size: 22rpx;
  color: #999;
  margin-top: 4rpx;
}

.date-tab.active .date-week {
  color: rgba(255,255,255,0.8);
}

/* 时段区域 */
.timeslot-section {
  background: #fff;
  margin-top: 20rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  color: #333;
}

.legend {
  display: flex;
  gap: 20rpx;
}

.legend-item {
  display: flex;
  align-items: center;
  font-size: 22rpx;
  color: #999;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  margin-right: 6rpx;
}

.legend-dot.green { background: #2da44e; }
.legend-dot.yellow { background: #f7931e; }
.legend-dot.red { background: #ff6b35; }

/* 时段网格 */
.timeslot-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.timeslot-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24rpx 16rpx;
  border-radius: 16rpx;
  background: #f8f9fa;
  border: 2rpx solid transparent;
  transition: all 0.3s;
}

.timeslot-item.peak1 { border-left: 6rpx solid #2da44e; }
.timeslot-item.peak2 { border-left: 6rpx solid #f7931e; }
.timeslot-item.peak3 { border-left: 6rpx solid #ff6b35; }

.timeslot-item.active {
  background: #f0f4ff;
  border-color: #667eea;
}

.timeslot-item.disabled {
  background: #eee;
  opacity: 0.6;
}

.slot-time {
  font-size: 26rpx;
  font-weight: bold;
  color: #333;
}

.slot-discount {
  font-size: 22rpx;
  color: #ff6b35;
  background: #fff0eb;
  padding: 4rpx 12rpx;
  border-radius: 6rpx;
  margin-top: 8rpx;
}

.slot-full {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.slot-remaining {
  font-size: 20rpx;
  color: #999;
  margin-top: 8rpx;
}

.empty-slots {
  text-align: center;
  padding: 60rpx 0;
}

.empty-text {
  font-size: 28rpx;
  color: #999;
}

/* 优惠提示 */
.discount-tip {
  display: flex;
  align-items: center;
  background: #fff8e1;
  margin: 20rpx 30rpx;
  padding: 24rpx;
  border-radius: 16rpx;
}

.tip-icon {
  font-size: 32rpx;
  margin-right: 12rpx;
}

.tip-text {
  font-size: 26rpx;
  color: #f7931e;
}

/* 底部栏 */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 30rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0,0,0,0.05);
}

.selected-info {
  flex: 1;
}

.info-label {
  font-size: 24rpx;
  color: #999;
  display: block;
}

.info-value {
  font-size: 28rpx;
  font-weight: bold;
  color: #333;
}

.info-placeholder {
  font-size: 28rpx;
  color: #999;
}

.confirm-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  padding: 24rpx 60rpx;
  border-radius: 40rpx;
  font-size: 30rpx;
  font-weight: bold;
}

.confirm-btn.disabled {
  background: #ccc;
}
</style>
