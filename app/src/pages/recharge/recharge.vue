<template>
  <view class="recharge-page">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-bg">
        <view class="balance-label">当前余额</view>
        <view class="balance-amount">
          <text class="currency">¥</text>
          <text class="amount">{{ balance }}</text>
        </view>
        <view class="balance-hint">余额可用于下单支付</view>
      </view>
    </view>

    <!-- 充值金额选择 -->
    <view class="section">
      <view class="section-title">选择充值金额</view>
      <view class="amount-grid">
        <view
          v-for="item in presetAmounts"
          :key="item"
          class="amount-item"
          :class="{ active: selectedAmount === item && !isCustom }"
          @click="selectAmount(item)"
        >
          <text class="amount-currency">¥</text>
          <text class="amount-value">{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 自定义金额 -->
    <view class="section">
      <view class="section-title">自定义金额</view>
      <view class="custom-amount">
        <text class="custom-currency">¥</text>
        <input
          v-model="customAmount"
          class="custom-input"
          type="digit"
          placeholder="请输入充值金额"
          @focus="onCustomFocus"
          @input="onCustomInput"
        />
      </view>
    </view>

    <!-- 支付方式 -->
    <view class="section">
      <view class="section-title">支付方式</view>
      <view class="payment-list">
        <view
          v-for="item in paymentMethods"
          :key="item.id"
          class="payment-item"
          :class="{ active: selectedPayment === item.id }"
          @click="selectPayment(item.id)"
        >
          <view class="payment-left">
            <text class="payment-icon">{{ item.icon }}</text>
            <view class="payment-info">
              <text class="payment-name">{{ item.name }}</text>
              <text v-if="item.desc" class="payment-desc">{{ item.desc }}</text>
            </view>
          </view>
          <view class="payment-check">
            <view v-if="selectedPayment === item.id" class="check-circle">
              <text class="check-icon">✓</text>
            </view>
            <view v-else class="uncheck-circle"></view>
          </view>
        </view>
      </view>
    </view>

    <!-- 充值按钮 -->
    <view class="bottom-bar">
      <view class="recharge-info">
        <text class="recharge-label">充值金额：</text>
        <text class="recharge-value">¥{{ finalAmount }}</text>
      </view>
      <view
        class="recharge-btn"
        :class="{ disabled: !canRecharge }"
        @click="handleRecharge"
      >
        <text>立即充值</text>
      </view>
    </view>

    <!-- 充值确认弹窗 -->
    <uni-popup ref="confirmPopup" type="center">
      <view class="confirm-popup">
        <view class="confirm-header">
          <text class="confirm-title">确认充值</text>
        </view>
        <view class="confirm-content">
          <text class="confirm-amount">¥{{ finalAmount }}</text>
          <text class="confirm-desc">充值到账户余额</text>
        </view>
        <view class="confirm-actions">
          <view class="confirm-btn cancel" @click="closeConfirm">取消</view>
          <view class="confirm-btn confirm" @click="confirmRecharge">确认支付</view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { post, get } from '@/utils/request'

const balance = ref('0.00')
const selectedAmount = ref(50)
const customAmount = ref('')
const isCustom = ref(false)
const selectedPayment = ref('wechat')
const confirmPopup = ref(null)

const presetAmounts = [10, 20, 50, 100]

const paymentMethods = [
  { id: 'wechat', name: '微信支付', icon: '💚', desc: '推荐使用' },
  { id: 'alipay', name: '支付宝', icon: '💙', desc: '' }
]

// 最终充值金额
const finalAmount = computed(() => {
  if (isCustom.value && customAmount.value) {
    return parseFloat(customAmount.value).toFixed(2)
  }
  return selectedAmount.value.toFixed(2)
})

// 是否可以充值
const canRecharge = computed(() => {
  const amount = parseFloat(finalAmount.value)
  return amount > 0 && amount <= 10000
})

// 获取余额
const fetchBalance = async () => {
  try {
    const res = await get('/customer/user/balance')
    if (res.code === 200) {
      balance.value = parseFloat(res.data || 0).toFixed(2)
    }
  } catch (error) {
    console.error('获取余额失败:', error)
  }
}

// 选择预设金额
const selectAmount = (amount) => {
  selectedAmount.value = amount
  isCustom.value = false
  customAmount.value = ''
}

// 自定义金额输入
const onCustomInput = (e) => {
  let value = e.detail.value
  // 限制只能输入数字和小数点
  value = value.replace(/[^\d.]/g, '')
  // 限制小数点后两位
  const parts = value.split('.')
  if (parts.length > 2) {
    value = parts[0] + '.' + parts[1]
  }
  if (parts[1] && parts[1].length > 2) {
    value = parts[0] + '.' + parts[1].substring(0, 2)
  }
  // 限制最大值
  if (parseFloat(value) > 10000) {
    value = '10000'
  }
  customAmount.value = value
}

// 自定义金额聚焦
const onCustomFocus = () => {
  isCustom.value = true
  selectedAmount.value = null
}

// 选择支付方式
const selectPayment = (id) => {
  selectedPayment.value = id
}

// 处理充值
const handleRecharge = () => {
  if (!canRecharge.value) {
    if (parseFloat(finalAmount.value) > 10000) {
      uni.showToast({ title: '单次充值不能超过10000元', icon: 'none' })
    }
    return
  }
  confirmPopup.value?.open()
}

// 关闭确认弹窗
const closeConfirm = () => {
  confirmPopup.value?.close()
}

// 确认充值
const confirmRecharge = async () => {
  closeConfirm()

  try {
    uni.showLoading({ title: '支付中...' })

    // 模拟支付延迟
    await new Promise(resolve => setTimeout(resolve, 1500))

    const res = await post('/customer/user/recharge', {
      amount: parseFloat(finalAmount.value),
      paymentMethod: selectedPayment.value
    })

    if (res.code === 200) {
      uni.showToast({
        title: '充值成功',
        icon: 'success',
        duration: 2000
      })

      // 更新余额
      balance.value = parseFloat(res.data?.balance || 0).toFixed(2)

      // 重置选择
      selectedAmount.value = 50
      isCustom.value = false
      customAmount.value = ''

      // 延迟返回
      setTimeout(() => {
        uni.navigateBack()
      }, 2000)
    }
  } catch (error) {
    console.error('充值失败:', error)
    uni.showToast({ title: '充值失败，请重试', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

onMounted(() => {
  fetchBalance()
})
</script>

<style lang="scss" scoped>
.recharge-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 200rpx;
}

// 余额卡片
.balance-card {
  padding: 30rpx;

  .balance-bg {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 32rpx;
    padding: 50rpx 40rpx;
    color: #fff;
    box-shadow: 0 8rpx 32rpx rgba(102, 126, 234, 0.3);
  }

  .balance-label {
    font-size: 28rpx;
    opacity: 0.9;
    margin-bottom: 20rpx;
  }

  .balance-amount {
    display: flex;
    align-items: baseline;
    margin-bottom: 16rpx;

    .currency {
      font-size: 40rpx;
      font-weight: 500;
      margin-right: 8rpx;
    }

    .amount {
      font-size: 72rpx;
      font-weight: 700;
    }
  }

  .balance-hint {
    font-size: 24rpx;
    opacity: 0.8;
  }
}

// 区块样式
.section {
  background: #fff;
  margin: 20rpx;
  border-radius: 24rpx;
  padding: 30rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 30rpx;
  font-weight: 600;
  color: #333;
  margin-bottom: 24rpx;
}

// 金额网格
.amount-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20rpx;
}

.amount-item {
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 30rpx 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  .amount-currency {
    font-size: 24rpx;
    color: #667eea;
    font-weight: 500;
  }

  .amount-value {
    font-size: 36rpx;
    color: #667eea;
    font-weight: 600;
  }

  &.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-color: #667eea;

    .amount-currency,
    .amount-value {
      color: #fff;
    }
  }
}

// 自定义金额
.custom-amount {
  display: flex;
  align-items: center;
  background: #f8f9fa;
  border-radius: 16rpx;
  padding: 24rpx 30rpx;
  border: 2rpx solid transparent;

  &:focus-within {
    border-color: #667eea;
    background: #fff;
  }

  .custom-currency {
    font-size: 32rpx;
    color: #333;
    font-weight: 600;
    margin-right: 16rpx;
  }

  .custom-input {
    flex: 1;
    font-size: 32rpx;
    color: #333;
    height: 60rpx;
  }
}

// 支付方式
.payment-list {
  display: flex;
  flex-direction: column;
  gap: 20rpx;
}

.payment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24rpx;
  background: #f8f9fa;
  border-radius: 16rpx;
  border: 2rpx solid transparent;
  transition: all 0.3s;

  &.active {
    background: #f0f4ff;
    border-color: #667eea;
  }
}

.payment-left {
  display: flex;
  align-items: center;
  gap: 20rpx;

  .payment-icon {
    font-size: 48rpx;
  }

  .payment-info {
    display: flex;
    flex-direction: column;

    .payment-name {
      font-size: 30rpx;
      color: #333;
      font-weight: 500;
    }

    .payment-desc {
      font-size: 24rpx;
      color: #999;
      margin-top: 4rpx;
    }
  }
}

.payment-check {
  .uncheck-circle {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid #ccc;
    border-radius: 50%;
  }

  .check-circle {
    width: 40rpx;
    height: 40rpx;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .check-icon {
      color: #fff;
      font-size: 24rpx;
      font-weight: bold;
    }
  }
}

// 底部栏
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #fff;
  padding: 20rpx 30rpx 40rpx;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.recharge-info {
  display: flex;
  align-items: baseline;
  margin-bottom: 20rpx;

  .recharge-label {
    font-size: 28rpx;
    color: #666;
  }

  .recharge-value {
    font-size: 40rpx;
    color: #667eea;
    font-weight: 700;
  }
}

.recharge-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);

  &.disabled {
    background: #ccc;
    box-shadow: none;
  }
}

// 确认弹窗
.confirm-popup {
  background: #fff;
  border-radius: 32rpx;
  width: 560rpx;
  overflow: hidden;
}

.confirm-header {
  padding: 40rpx 40rpx 20rpx;
  text-align: center;

  .confirm-title {
    font-size: 34rpx;
    font-weight: 600;
    color: #333;
  }
}

.confirm-content {
  padding: 30rpx 40rpx;
  text-align: center;

  .confirm-amount {
    font-size: 56rpx;
    font-weight: 700;
    color: #667eea;
    display: block;
    margin-bottom: 12rpx;
  }

  .confirm-desc {
    font-size: 28rpx;
    color: #999;
  }
}

.confirm-actions {
  display: flex;
  border-top: 1rpx solid #f0f0f0;

  .confirm-btn {
    flex: 1;
    height: 100rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 30rpx;

    &.cancel {
      color: #666;
      border-right: 1rpx solid #f0f0f0;
    }

    &.confirm {
      color: #667eea;
      font-weight: 600;
    }
  }
}
</style>
