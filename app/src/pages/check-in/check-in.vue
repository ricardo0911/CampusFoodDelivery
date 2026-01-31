<template>
  <view class="page">
    <!-- 自定义导航栏 -->
    <view class="nav-bar">
      <view class="nav-back" @click="goBack">
        <view class="back-icon"></view>
      </view>
      <text class="nav-title">每日签到</text>
      <view class="nav-placeholder"></view>
    </view>

    <!-- 签到头部 -->
    <view class="header">
      <view class="header-bg"></view>
      <view class="header-content">
        <view class="points-card">
          <view class="points-info">
            <text class="points-label">我的积分</text>
            <text class="points-value">{{ userPoints }}</text>
          </view>
          <view class="continuous-info">
            <text class="continuous-label">连续签到</text>
            <text class="continuous-value">{{ continuousDays }}天</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 签到按钮 -->
    <view class="sign-section">
      <view
        class="sign-btn"
        :class="{ signed: signedToday }"
        @click="handleSign"
      >
        <view class="sign-icon">
          <view v-if="signedToday" class="check-icon"></view>
          <view v-else class="gift-icon"></view>
        </view>
        <text class="sign-text">{{ signedToday ? '已签到' : '立即签到' }}</text>
        <text class="sign-points" v-if="!signedToday">+{{ todayPoints }}积分</text>
      </view>
    </view>

    <!-- 签到日历 -->
    <view class="calendar-section">
      <view class="section-header">
        <text class="section-title">{{ currentYear }}年{{ currentMonth }}月</text>
        <view class="month-nav">
          <view class="nav-btn" @click="prevMonth">
            <view class="arrow-left"></view>
          </view>
          <view class="nav-btn" @click="nextMonth">
            <view class="arrow-right"></view>
          </view>
        </view>
      </view>

      <view class="calendar">
        <view class="weekdays">
          <text class="weekday" v-for="day in weekdays" :key="day">{{ day }}</text>
        </view>
        <view class="days">
          <view
            class="day"
            v-for="(day, index) in calendarDays"
            :key="index"
            :class="{
              empty: !day.date,
              signed: day.signed,
              today: day.isToday
            }"
          >
            <text class="day-num">{{ day.date || '' }}</text>
            <view v-if="day.signed" class="signed-dot"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 签到规则 -->
    <view class="rules-section">
      <view class="section-header">
        <text class="section-title">签到规则</text>
      </view>
      <view class="rules-list">
        <view class="rule-item" v-for="(rule, index) in rules" :key="index">
          <view class="rule-icon">{{ index + 1 }}</view>
          <text class="rule-text">{{ rule }}</text>
        </view>
      </view>
    </view>

    <!-- 签到成功弹窗 -->
    <view class="modal" v-if="showModal" @click="closeModal">
      <view class="modal-content" @click.stop>
        <view class="modal-icon">
          <view class="success-icon"></view>
        </view>
        <text class="modal-title">签到成功</text>
        <text class="modal-points">+{{ earnedPoints }}积分</text>
        <text class="modal-tip">连续签到{{ continuousDays }}天</text>
        <view class="modal-btn" @click="closeModal">知道了</view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const userPoints = ref(128)
const continuousDays = ref(0)
const signedToday = ref(false)
const todayPoints = ref(10)
const earnedPoints = ref(0)
const showModal = ref(false)
const monthRecords = ref([])

const currentYear = ref(new Date().getFullYear())
const currentMonth = ref(new Date().getMonth() + 1)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']
const rules = ref([
  '每日签到可获得10积分',
  '连续签到3天额外奖励10积分',
  '连续签到7天额外奖励20积分',
  '积分可用于兑换优惠券'
])

const calendarDays = computed(() => {
  const year = currentYear.value
  const month = currentMonth.value
  const firstDay = new Date(year, month - 1, 1).getDay()
  const daysInMonth = new Date(year, month, 0).getDate()
  const today = new Date()

  const days = []

  // 填充空白
  for (let i = 0; i < firstDay; i++) {
    days.push({ date: null })
  }

  // 填充日期
  for (let i = 1; i <= daysInMonth; i++) {
    const dateStr = `${year}-${String(month).padStart(2, '0')}-${String(i).padStart(2, '0')}`
    const signed = monthRecords.value.some(r => r.checkInDate === dateStr)
    const isToday = today.getFullYear() === year &&
                    today.getMonth() + 1 === month &&
                    today.getDate() === i

    days.push({ date: i, signed, isToday })
  }

  return days
})

const goBack = () => {
  uni.navigateBack()
}

const loadStatus = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) return

    const res = await uni.request({
      url: 'http://localhost:8080/api/customer/check-in/status',
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      const data = res.data.data
      signedToday.value = data.signedToday
      continuousDays.value = data.continuousDays
      monthRecords.value = data.monthRecords || []
    }
  } catch (e) {
    console.error(e)
  }
}

const handleSign = async () => {
  if (signedToday.value) {
    uni.showToast({ title: '今天已签到', icon: 'none' })
    return
  }

  try {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.navigateTo({ url: '/pages/login/login' })
      return
    }

    const res = await uni.request({
      url: 'http://localhost:8080/api/customer/check-in/sign',
      method: 'POST',
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      const data = res.data.data
      earnedPoints.value = data.points
      continuousDays.value = data.continuousDays
      signedToday.value = true
      userPoints.value += data.points
      showModal.value = true
      loadStatus()
    } else {
      uni.showToast({ title: res.data.message || '签到失败', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '网络错误', icon: 'none' })
  }
}

const prevMonth = () => {
  if (currentMonth.value === 1) {
    currentMonth.value = 12
    currentYear.value--
  } else {
    currentMonth.value--
  }
  loadHistory()
}

const nextMonth = () => {
  if (currentMonth.value === 12) {
    currentMonth.value = 1
    currentYear.value++
  } else {
    currentMonth.value++
  }
  loadHistory()
}

const loadHistory = async () => {
  try {
    const token = uni.getStorageSync('token')
    if (!token) return

    const res = await uni.request({
      url: `http://localhost:8080/api/customer/check-in/history?year=${currentYear.value}&month=${currentMonth.value}`,
      method: 'GET',
      header: { 'Authorization': `Bearer ${token}` }
    })

    if (res.data.code === 200) {
      monthRecords.value = res.data.data || []
    }
  } catch (e) {
    console.error(e)
  }
}

const closeModal = () => {
  showModal.value = false
}

onMounted(() => {
  loadStatus()
})
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
}

.nav-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  padding-top: var(--status-bar-height);
}

.nav-back {
  width: 60rpx;
  height: 60rpx;
  display: flex;
  align-items: center;
  justify-content: center;
}

.back-icon {
  width: 20rpx;
  height: 20rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(45deg);
}

.nav-title {
  font-size: 34rpx;
  font-weight: 600;
  color: #fff;
}

.nav-placeholder {
  width: 60rpx;
}

.header {
  position: relative;
  padding-bottom: 60rpx;
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 0 0 40rpx 40rpx;
}

.header-content {
  position: relative;
  padding: 30rpx;
}

.points-card {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.1);
}

.points-info, .continuous-info {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-label, .continuous-label {
  font-size: 26rpx;
  color: #999;
  margin-bottom: 12rpx;
}

.points-value {
  font-size: 56rpx;
  font-weight: 700;
  color: #ff6b35;
}

.continuous-value {
  font-size: 40rpx;
  font-weight: 600;
  color: #333;
}

.sign-section {
  padding: 0 30rpx;
  margin-top: -30rpx;
}

.sign-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 24rpx;
  padding: 40rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 107, 53, 0.3);
}

.sign-btn.signed {
  background: linear-gradient(135deg, #ccc, #ddd);
  box-shadow: none;
}

.sign-icon {
  width: 80rpx;
  height: 80rpx;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16rpx;
}

.check-icon {
  width: 30rpx;
  height: 20rpx;
  border-left: 4rpx solid #fff;
  border-bottom: 4rpx solid #fff;
  transform: rotate(-45deg);
}

.gift-icon {
  width: 36rpx;
  height: 36rpx;
  background: #fff;
  border-radius: 8rpx;
  position: relative;
}

.gift-icon::before {
  content: '';
  position: absolute;
  top: -8rpx;
  left: 50%;
  transform: translateX(-50%);
  width: 16rpx;
  height: 16rpx;
  background: #fff;
  border-radius: 50%;
}

.sign-text {
  font-size: 36rpx;
  font-weight: 600;
  color: #fff;
}

.sign-points {
  font-size: 26rpx;
  color: rgba(255, 255, 255, 0.9);
  margin-top: 8rpx;
}

.calendar-section {
  margin: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24rpx;
}

.section-title {
  font-size: 32rpx;
  font-weight: 600;
  color: #333;
}

.month-nav {
  display: flex;
  gap: 20rpx;
}

.nav-btn {
  width: 48rpx;
  height: 48rpx;
  background: #f5f5f5;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.arrow-left, .arrow-right {
  width: 12rpx;
  height: 12rpx;
  border-top: 3rpx solid #666;
  border-right: 3rpx solid #666;
}

.arrow-left {
  transform: rotate(-135deg);
  margin-left: 4rpx;
}

.arrow-right {
  transform: rotate(45deg);
  margin-right: 4rpx;
}

.weekdays {
  display: flex;
  margin-bottom: 16rpx;
}

.weekday {
  flex: 1;
  text-align: center;
  font-size: 24rpx;
  color: #999;
}

.days {
  display: flex;
  flex-wrap: wrap;
}

.day {
  width: calc(100% / 7);
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.day.empty {
  visibility: hidden;
}

.day-num {
  font-size: 28rpx;
  color: #333;
}

.day.today .day-num {
  color: #ff6b35;
  font-weight: 600;
}

.day.signed .day-num {
  color: #fff;
}

.day.signed::before {
  content: '';
  position: absolute;
  width: 60rpx;
  height: 60rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
  z-index: -1;
}

.signed-dot {
  width: 8rpx;
  height: 8rpx;
  background: #fff;
  border-radius: 50%;
  margin-top: 4rpx;
}

.rules-section {
  margin: 30rpx;
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
}

.rules-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 16rpx;
}

.rule-icon {
  width: 40rpx;
  height: 40rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22rpx;
  color: #fff;
  font-weight: 600;
}

.rule-text {
  font-size: 28rpx;
  color: #666;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
}

.modal-content {
  width: 560rpx;
  background: #fff;
  border-radius: 32rpx;
  padding: 60rpx 40rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.modal-icon {
  width: 120rpx;
  height: 120rpx;
  background: linear-gradient(135deg, #4CAF50, #8BC34A);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 30rpx;
}

.success-icon {
  width: 50rpx;
  height: 30rpx;
  border-left: 6rpx solid #fff;
  border-bottom: 6rpx solid #fff;
  transform: rotate(-45deg);
}

.modal-title {
  font-size: 40rpx;
  font-weight: 700;
  color: #333;
  margin-bottom: 16rpx;
}

.modal-points {
  font-size: 56rpx;
  font-weight: 700;
  color: #ff6b35;
  margin-bottom: 12rpx;
}

.modal-tip {
  font-size: 28rpx;
  color: #999;
  margin-bottom: 40rpx;
}

.modal-btn {
  width: 100%;
  height: 88rpx;
  background: linear-gradient(135deg, #ff6b35, #ff8f50);
  border-radius: 44rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32rpx;
  font-weight: 600;
  color: #fff;
}
</style>
