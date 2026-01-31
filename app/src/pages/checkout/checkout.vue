<template>
  <view class="page">
    <!-- 优惠券提示 -->
    <view class="coupon-banner" v-if="couponAmount > 0">
      <text class="coupon-icon">¥</text>
      <text class="coupon-text">{{ couponAmount }}元优惠券 <text class="coupon-time">11:22:56</text>后失效</text>
    </view>
    
    <!-- 收货地址 -->
    <view class="address-card" @click="selectAddress">
      <view class="address-left">
        <view class="address-tags">
          <text class="tag default">默认</text>
          <text class="tag school">学校</text>
        </view>
        <text class="address-name">{{ address.name }}</text>
      </view>
      <view class="address-right">
        <text class="address-detail">{{ address.detail }}</text>
        <text class="address-phone">{{ address.phone }}</text>
      </view>
      <text class="address-arrow">›</text>
    </view>
    
    <!-- 商品列表 -->
    <view class="order-section" v-for="(group, shopId) in groupedCart" :key="shopId">
      <view class="shop-header">
        <text class="shop-tag">外卖</text>
        <text class="shop-name">{{ group.shopName }}</text>
      </view>
      
      <view class="order-item" v-for="item in group.items" :key="item.id">
        <image class="item-image" :src="item.dishImage || '/static/default-dish.jpg'" mode="aspectFill" />
        <view class="item-info">
          <text class="item-name">{{ item.dishName }}</text>
          <text class="item-spec" v-if="item.spec">{{ item.spec }}</text>
          <view class="item-price-row">
            <text class="item-price">¥{{ item.unitPrice }}</text>
            <text class="item-origin" v-if="item.originalPrice">¥{{ item.originalPrice }}</text>
          </view>
        </view>
        <text class="item-quantity">×{{ item.quantity }}</text>
      </view>
      
      <!-- 配送信息 -->
      <view class="delivery-section">
        <view class="delivery-row" @click="selectDeliveryTime">
          <text class="delivery-label">配送</text>
          <view class="delivery-value">
            <text class="delivery-tag">准时保+</text>
          </view>
          <view class="delivery-time">
            <text>{{ deliveryTimeText }}</text>
            <text class="time-highlight">{{ deliveryTimeDetail }}</text>
            <text class="arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 预约时间选择弹窗 -->
    <view class="schedule-popup" v-if="showSchedulePopup" @click.self="showSchedulePopup = false">
      <view class="schedule-content">
        <view class="schedule-header">
          <text class="schedule-title">选择送达时间</text>
          <text class="schedule-close" @click="showSchedulePopup = false">×</text>
        </view>
        <view class="schedule-tabs">
          <view
            class="schedule-tab"
            :class="{ active: scheduleTab === 'now' }"
            @click="scheduleTab = 'now'"
          >
            <text>立即送出</text>
          </view>
          <view
            class="schedule-tab"
            :class="{ active: scheduleTab === 'schedule' }"
            @click="scheduleTab = 'schedule'"
          >
            <text>预约送达</text>
          </view>
        </view>
        <view class="schedule-body" v-if="scheduleTab === 'schedule'">
          <scroll-view class="date-list" scroll-x>
            <view
              class="date-item"
              :class="{ active: selectedDate === date.value }"
              v-for="date in dateOptions"
              :key="date.value"
              @click="selectedDate = date.value"
            >
              <text class="date-label">{{ date.label }}</text>
              <text class="date-week">{{ date.week }}</text>
            </view>
          </scroll-view>
          <scroll-view class="time-list" scroll-y>
            <view
              class="time-item"
              :class="{ active: selectedTime === time }"
              v-for="time in timeOptions"
              :key="time"
              @click="selectedTime = time"
            >
              <text>{{ time }}</text>
            </view>
          </scroll-view>
        </view>
        <view class="schedule-footer">
          <view class="schedule-confirm-btn" @click="confirmSchedule">确定</view>
        </view>
      </view>
    </view>
    
    <!-- 其他选项 -->
    <view class="options-card">
      <view class="option-row" @click="selectOutOfStock">
        <text class="option-label">如遇缺货</text>
        <text class="option-value">{{ outOfStockOption }}</text>
        <text class="option-arrow">›</text>
      </view>
      <view class="option-row" @click="selectTableware">
        <text class="option-label">餐具数量</text>
        <text class="option-value">{{ tablewareOption }}</text>
        <text class="option-arrow">›</text>
      </view>
      <view class="option-row" @click="addRemark">
        <text class="option-label">备注<text class="option-hint">(一次备注，多次使用)</text></text>
        <text class="option-value">{{ remark || '请选择口味偏好' }}</text>
        <text class="option-arrow">›</text>
      </view>
    </view>
    
    <!-- 底部支付栏 -->
    <view class="pay-bar">
      <view class="pay-btn" @click="submitOrder">
        <text class="pay-text">立即支付</text>
        <text class="pay-amount">¥{{ totalAmount }}</text>
        <text class="pay-discount" v-if="discountAmount > 0">共减¥{{ discountAmount }}</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const address = ref({
  name: '海都学院1号楼东门',
  detail: '校园配送点',
  phone: '192****4337'
})

const cartList = ref([])
const couponAmount = ref(7)
const discountAmount = ref(9.5)
const outOfStockOption = ref('缺货时与我电话沟通')
const tablewareOption = ref('商家按餐量提供')
const remark = ref('')

// 预约配送相关
const showSchedulePopup = ref(false)
const scheduleTab = ref('now')
const selectedDate = ref('')
const selectedTime = ref('')
const isScheduled = ref(false)
const scheduledDateTime = ref('')

const deliveryTimeText = computed(() => {
  if (isScheduled.value) {
    return '预约送达'
  }
  return '立即送出'
})

const deliveryTimeDetail = computed(() => {
  if (isScheduled.value && scheduledDateTime.value) {
    return scheduledDateTime.value
  }
  return '大约30分钟送达'
})

const dateOptions = computed(() => {
  const options = []
  const today = new Date()
  const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

  for (let i = 0; i < 7; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    const month = date.getMonth() + 1
    const day = date.getDate()
    options.push({
      value: `${month}-${day}`,
      label: i === 0 ? '今天' : i === 1 ? '明天' : `${month}月${day}日`,
      week: weekDays[date.getDay()]
    })
  }
  return options
})

const timeOptions = computed(() => {
  const options = []
  const now = new Date()
  const currentHour = now.getHours()
  const isToday = selectedDate.value === dateOptions.value[0]?.value

  for (let h = 8; h <= 21; h++) {
    if (isToday && h <= currentHour) continue
    options.push(`${h}:00-${h}:30`)
    options.push(`${h}:30-${h + 1}:00`)
  }
  return options
})

const selectDeliveryTime = () => {
  showSchedulePopup.value = true
  if (!selectedDate.value && dateOptions.value.length > 0) {
    selectedDate.value = dateOptions.value[0].value
  }
  if (!selectedTime.value && timeOptions.value.length > 0) {
    selectedTime.value = timeOptions.value[0]
  }
}

const confirmSchedule = () => {
  if (scheduleTab.value === 'now') {
    isScheduled.value = false
    scheduledDateTime.value = ''
  } else {
    if (!selectedDate.value || !selectedTime.value) {
      uni.showToast({ title: '请选择送达时间', icon: 'none' })
      return
    }
    isScheduled.value = true
    const dateLabel = dateOptions.value.find(d => d.value === selectedDate.value)?.label || selectedDate.value
    scheduledDateTime.value = `${dateLabel} ${selectedTime.value}`
  }
  showSchedulePopup.value = false
}

const groupedCart = computed(() => {
  const groups = {}
  cartList.value.forEach(item => {
    const shopId = item.shopId || 'default'
    if (!groups[shopId]) {
      groups[shopId] = {
        shopName: item.shopName || '美味餐厅',
        deliveryTime: 30,
        items: []
      }
    }
    groups[shopId].items.push(item)
  })
  return groups
})

const totalAmount = computed(() => {
  const itemTotal = cartList.value.reduce((sum, item) => sum + item.unitPrice * item.quantity, 0)
  return (itemTotal - discountAmount.value).toFixed(2)
})

const loadCart = () => {
  const localCart = uni.getStorageSync('cartList')
  if (localCart && localCart.length > 0) {
    cartList.value = localCart.filter(item => item.selected !== false)
  }
}

const selectAddress = () => {
  uni.showToast({ title: '选择地址功能开发中', icon: 'none' })
}

const selectOutOfStock = () => {
  uni.showActionSheet({
    itemList: ['缺货时与我电话沟通', '缺货时取消订单', '缺货时自动换货'],
    success: (res) => {
      outOfStockOption.value = ['缺货时与我电话沟通', '缺货时取消订单', '缺货时自动换货'][res.tapIndex]
    }
  })
}

const selectTableware = () => {
  uni.showActionSheet({
    itemList: ['商家按餐量提供', '不需要餐具', '1套', '2套', '3套'],
    success: (res) => {
      tablewareOption.value = ['商家按餐量提供', '不需要餐具', '1套', '2套', '3套'][res.tapIndex]
    }
  })
}

const addRemark = () => {
  uni.showActionSheet({
    itemList: ['不要辣', '少辣', '中辣', '多辣', '不要香菜', '不要葱'],
    success: (res) => {
      remark.value = ['不要辣', '少辣', '中辣', '多辣', '不要香菜', '不要葱'][res.tapIndex]
    }
  })
}

const submitOrder = () => {
  uni.showModal({
    title: '确认支付',
    content: `订单金额: ¥${totalAmount.value}`,
    success: (res) => {
      if (res.confirm) {
        uni.showLoading({ title: '支付中...' })
        setTimeout(() => {
          uni.hideLoading()
          uni.showToast({ title: '支付成功！', icon: 'success' })
          uni.removeStorageSync('cartList')
          setTimeout(() => {
            uni.switchTab({ url: '/pages/order/order' })
          }, 1500)
        }, 1000)
      }
    }
  })
}

onMounted(loadCart)
</script>

<style scoped>
.page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

/* 优惠券提示 */
.coupon-banner {
  display: flex;
  align-items: center;
  padding: 20rpx 30rpx;
  background: #fff8f0;
}

.coupon-icon {
  width: 40rpx;
  height: 40rpx;
  background: #ff4d4f;
  color: #fff;
  font-size: 24rpx;
  border-radius: 8rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12rpx;
}

.coupon-text {
  font-size: 26rpx;
  color: #ff4d4f;
}

.coupon-time {
  font-weight: bold;
}

/* 收货地址 */
.address-card {
  display: flex;
  align-items: center;
  padding: 30rpx;
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}

.address-left {
  margin-right: 20rpx;
}

.address-tags {
  display: flex;
  margin-bottom: 10rpx;
}

.tag {
  font-size: 20rpx;
  padding: 4rpx 10rpx;
  border-radius: 4rpx;
  margin-right: 10rpx;
}

.tag.default {
  background: #ff4d4f;
  color: #fff;
}

.tag.school {
  background: #e6f7ff;
  color: #1890ff;
}

.address-name {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.address-right {
  flex: 1;
}

.address-detail {
  font-size: 26rpx;
  color: #666;
  display: block;
}

.address-phone {
  font-size: 24rpx;
  color: #999;
}

.address-arrow {
  font-size: 32rpx;
  color: #ccc;
}

/* 商品区域 */
.order-section {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
  overflow: hidden;
}

.shop-header {
  display: flex;
  align-items: center;
  padding: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.shop-tag {
  background: #ff4d4f;
  color: #fff;
  font-size: 20rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  margin-right: 12rpx;
}

.shop-name {
  font-size: 28rpx;
  font-weight: 600;
  color: #333;
}

.order-item {
  display: flex;
  align-items: center;
  padding: 20rpx 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.item-image {
  width: 120rpx;
  height: 120rpx;
  border-radius: 12rpx;
  margin-right: 20rpx;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 28rpx;
  color: #333;
  display: block;
}

.item-spec {
  font-size: 24rpx;
  color: #999;
  display: block;
  margin-top: 8rpx;
}

.item-price-row {
  margin-top: 12rpx;
}

.item-price {
  font-size: 28rpx;
  color: #ff4d4f;
  font-weight: bold;
}

.item-origin {
  font-size: 24rpx;
  color: #999;
  text-decoration: line-through;
  margin-left: 10rpx;
}

.item-quantity {
  font-size: 26rpx;
  color: #666;
}

/* 配送信息 */
.delivery-section {
  padding: 24rpx;
  border-top: 1rpx solid #f0f0f0;
}

.delivery-row {
  display: flex;
  align-items: center;
}

.delivery-label {
  font-size: 28rpx;
  color: #333;
  margin-right: 20rpx;
}

.delivery-tag {
  background: #e6f7ff;
  color: #1890ff;
  font-size: 22rpx;
  padding: 4rpx 12rpx;
  border-radius: 4rpx;
  margin-right: 20rpx;
}

.delivery-time {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  color: #666;
}

.time-highlight {
  color: #ff4d4f;
  font-weight: bold;
  display: block;
}

.arrow {
  color: #ccc;
  margin-left: 8rpx;
}

/* 选项卡片 */
.options-card {
  background: #fff;
  margin: 20rpx;
  border-radius: 16rpx;
}

.option-row {
  display: flex;
  align-items: center;
  padding: 28rpx 24rpx;
  border-bottom: 1rpx solid #f8f8f8;
}

.option-row:last-child {
  border-bottom: none;
}

.option-label {
  font-size: 28rpx;
  color: #333;
  min-width: 180rpx;
}

.option-hint {
  font-size: 22rpx;
  color: #999;
}

.option-value {
  flex: 1;
  text-align: right;
  font-size: 26rpx;
  color: #666;
}

.option-arrow {
  font-size: 28rpx;
  color: #ccc;
  margin-left: 10rpx;
}

/* 底部支付栏 */
.pay-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2rpx 10rpx rgba(0,0,0,0.05);
}

.pay-btn {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  padding: 28rpx;
  border-radius: 50rpx;
  text-align: center;
}

.pay-text {
  font-size: 32rpx;
  font-weight: bold;
}

.pay-amount {
  font-size: 36rpx;
  font-weight: bold;
  margin-left: 20rpx;
}

.pay-discount {
  font-size: 24rpx;
  display: block;
  margin-top: 8rpx;
  opacity: 0.9;
}

/* 预约时间弹窗 */
.schedule-popup {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
}

.schedule-content {
  width: 100%;
  background: #fff;
  border-radius: 24rpx 24rpx 0 0;
  max-height: 80vh;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;
}

.schedule-title {
  font-size: 32rpx;
  font-weight: bold;
  color: #333;
}

.schedule-close {
  font-size: 48rpx;
  color: #999;
  line-height: 1;
}

.schedule-tabs {
  display: flex;
  padding: 20rpx 30rpx;
  gap: 20rpx;
}

.schedule-tab {
  flex: 1;
  padding: 24rpx;
  text-align: center;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
  color: #666;
}

.schedule-tab.active {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
}

.schedule-body {
  padding: 20rpx 30rpx;
}

.date-list {
  white-space: nowrap;
  margin-bottom: 20rpx;
}

.date-item {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  padding: 20rpx 30rpx;
  margin-right: 16rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
}

.date-item.active {
  background: #fff0f0;
  border: 2rpx solid #ff4d4f;
}

.date-label {
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.date-item.active .date-label {
  color: #ff4d4f;
}

.date-week {
  font-size: 22rpx;
  color: #999;
  margin-top: 8rpx;
}

.time-list {
  height: 400rpx;
}

.time-item {
  padding: 24rpx;
  text-align: center;
  font-size: 28rpx;
  color: #333;
  border-bottom: 1rpx solid #f5f5f5;
}

.time-item.active {
  background: #fff0f0;
  color: #ff4d4f;
  font-weight: 500;
}

.schedule-footer {
  padding: 20rpx 30rpx;
  padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
}

.schedule-confirm-btn {
  background: linear-gradient(135deg, #ff4d4f, #ff7875);
  color: #fff;
  padding: 28rpx;
  border-radius: 50rpx;
  text-align: center;
  font-size: 32rpx;
  font-weight: bold;
}
</style>
