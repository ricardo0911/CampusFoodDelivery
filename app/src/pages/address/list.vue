<template>
  <view class="address-page">
    <!-- 空状态 -->
    <view v-if="addressList.length === 0 && !loading" class="empty-state">
      <text class="empty-icon">📍</text>
      <text class="empty-text">暂无收货地址</text>
      <text class="empty-subtext">添加地址，让美食更快送达</text>
    </view>

    <!-- 地址列表 -->
    <view v-else class="address-list">
      <view
        v-for="item in addressList"
        :key="item.id"
        class="address-card"
        :class="{ 'default': item.isDefault }"
      >
        <view class="address-header">
          <view class="user-info">
            <text class="name">{{ item.name }}</text>
            <text class="phone">{{ item.phone }}</text>
            <view v-if="item.isDefault" class="default-tag">默认</view>
          </view>
        </view>
        <view class="address-detail">
          <text class="location-icon">📍</text>
          <text class="address-text">{{ item.address }}</text>
        </view>
        <view class="address-actions">
          <view class="action-left">
            <view
              class="default-btn"
              :class="{ 'active': item.isDefault }"
              @click="setDefault(item)"
            >
              <view class="checkbox">
                <text v-if="item.isDefault" class="check-icon">✓</text>
              </view>
              <text class="default-text">设为默认</text>
            </view>
          </view>
          <view class="action-right">
            <view class="action-btn edit" @click="editAddress(item)">
              <text class="action-icon">✏️</text>
              <text>编辑</text>
            </view>
            <view class="action-btn delete" @click="deleteAddress(item)">
              <text class="action-icon">🗑️</text>
              <text>删除</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 底部添加按钮 -->
    <view class="bottom-bar">
      <view class="add-btn" @click="addAddress">
        <text class="add-icon">+</text>
        <text>新建地址</text>
      </view>
    </view>

    <!-- 添加/编辑地址弹窗 -->
    <uni-popup ref="addressPopup" type="bottom">
      <view class="popup-content">
        <view class="popup-header">
          <text class="popup-title">{{ isEdit ? '编辑地址' : '添加地址' }}</text>
          <text class="popup-close" @click="closePopup">✕</text>
        </view>
        <view class="form-list">
          <view class="form-item">
            <text class="form-label">联系人</text>
            <input
              v-model="formData.name"
              class="form-input"
              placeholder="请输入联系人姓名"
              maxlength="20"
            />
          </view>
          <view class="form-item">
            <text class="form-label">手机号</text>
            <input
              v-model="formData.phone"
              class="form-input"
              placeholder="请输入手机号码"
              type="number"
              maxlength="11"
            />
          </view>
          <view class="form-item">
            <text class="form-label">详细地址</text>
            <textarea
              v-model="formData.address"
              class="form-textarea"
              placeholder="请输入详细地址（如：宿舍楼、房间号等）"
              maxlength="100"
            />
          </view>
          <view class="form-item checkbox-item">
            <text class="form-label">设为默认地址</text>
            <switch
              v-model="formData.isDefault"
              color="#667eea"
              @change="onDefaultChange"
            />
          </view>
        </view>
        <view class="popup-footer">
          <view class="submit-btn" @click="submitAddress">
            <text>{{ isEdit ? '保存' : '添加' }}</text>
          </view>
        </view>
      </view>
    </uni-popup>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get, post, put, del } from '@/utils/request'

const addressList = ref([])
const loading = ref(false)
const addressPopup = ref(null)
const isEdit = ref(false)
const editingId = ref(null)

const formData = ref({
  name: '',
  phone: '',
  address: '',
  isDefault: false
})

// 获取地址列表
const fetchAddressList = async () => {
  loading.value = true
  try {
    const res = await get('/customer/address/list')
    if (res.code === 200) {
      addressList.value = res.data || []
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 添加地址
const addAddress = () => {
  isEdit.value = false
  editingId.value = null
  formData.value = {
    name: '',
    phone: '',
    address: '',
    isDefault: false
  }
  addressPopup.value?.open()
}

// 编辑地址
const editAddress = (item) => {
  isEdit.value = true
  editingId.value = item.id
  formData.value = {
    name: item.name,
    phone: item.phone,
    address: item.address,
    isDefault: item.isDefault
  }
  addressPopup.value?.open()
}

// 关闭弹窗
const closePopup = () => {
  addressPopup.value?.close()
}

// 默认地址切换
const onDefaultChange = (e) => {
  formData.value.isDefault = e.detail.value
}

// 提交地址
const submitAddress = async () => {
  // 表单验证
  if (!formData.value.name.trim()) {
    uni.showToast({ title: '请输入联系人姓名', icon: 'none' })
    return
  }
  if (!formData.value.phone.trim()) {
    uni.showToast({ title: '请输入手机号码', icon: 'none' })
    return
  }
  if (!/^1[3-9]\d{9}$/.test(formData.value.phone)) {
    uni.showToast({ title: '请输入正确的手机号码', icon: 'none' })
    return
  }
  if (!formData.value.address.trim()) {
    uni.showToast({ title: '请输入详细地址', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '保存中...' })

    let res
    if (isEdit.value) {
      res = await put(`/customer/address/update/${editingId.value}`, formData.value)
    } else {
      res = await post('/customer/address/add', formData.value)
    }

    if (res.code === 200) {
      uni.showToast({ title: isEdit.value ? '修改成功' : '添加成功', icon: 'success' })
      closePopup()
      fetchAddressList()
    }
  } catch (error) {
    console.error('保存地址失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 设置默认地址
const setDefault = async (item) => {
  if (item.isDefault) return

  try {
    uni.showLoading({ title: '设置中...' })
    const res = await put(`/customer/address/setDefault/${item.id}`)
    if (res.code === 200) {
      uni.showToast({ title: '设置成功', icon: 'success' })
      fetchAddressList()
    }
  } catch (error) {
    console.error('设置默认地址失败:', error)
  } finally {
    uni.hideLoading()
  }
}

// 删除地址
const deleteAddress = (item) => {
  uni.showModal({
    title: '提示',
    content: '确定要删除该地址吗？',
    confirmColor: '#667eea',
    success: async (res) => {
      if (res.confirm) {
        try {
          uni.showLoading({ title: '删除中...' })
          const result = await del(`/customer/address/delete/${item.id}`)
          if (result.code === 200) {
            uni.showToast({ title: '删除成功', icon: 'success' })
            fetchAddressList()
          }
        } catch (error) {
          console.error('删除地址失败:', error)
        } finally {
          uni.hideLoading()
        }
      }
    }
  })
}

onMounted(() => {
  fetchAddressList()
})
</script>

<style lang="scss" scoped>
.address-page {
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 140rpx;
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;

  .empty-icon {
    font-size: 120rpx;
    margin-bottom: 30rpx;
  }

  .empty-text {
    font-size: 32rpx;
    color: #333;
    font-weight: 600;
    margin-bottom: 16rpx;
  }

  .empty-subtext {
    font-size: 26rpx;
    color: #999;
  }
}

// 地址列表
.address-list {
  padding: 20rpx;
}

.address-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 30rpx;
  margin-bottom: 20rpx;
  box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.06);

  &.default {
    border: 2rpx solid #667eea;
  }
}

.address-header {
  margin-bottom: 16rpx;

  .user-info {
    display: flex;
    align-items: center;
    gap: 20rpx;

    .name {
      font-size: 32rpx;
      font-weight: 600;
      color: #333;
    }

    .phone {
      font-size: 28rpx;
      color: #666;
    }

    .default-tag {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: #fff;
      font-size: 20rpx;
      padding: 4rpx 12rpx;
      border-radius: 8rpx;
    }
  }
}

.address-detail {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  margin-bottom: 24rpx;
  padding-bottom: 24rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .location-icon {
    font-size: 28rpx;
    margin-top: 2rpx;
  }

  .address-text {
    flex: 1;
    font-size: 28rpx;
    color: #555;
    line-height: 1.5;
  }
}

.address-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.action-left {
  .default-btn {
    display: flex;
    align-items: center;
    gap: 12rpx;

    .checkbox {
      width: 36rpx;
      height: 36rpx;
      border: 2rpx solid #ccc;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.3s;

      .check-icon {
        font-size: 22rpx;
        color: #fff;
      }
    }

    .default-text {
      font-size: 26rpx;
      color: #666;
    }

    &.active {
      .checkbox {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border-color: #667eea;
      }

      .default-text {
        color: #667eea;
      }
    }
  }
}

.action-right {
  display: flex;
  gap: 30rpx;

  .action-btn {
    display: flex;
    align-items: center;
    gap: 8rpx;
    font-size: 26rpx;
    color: #666;

    .action-icon {
      font-size: 28rpx;
    }

    &.delete {
      color: #ff6b6b;
    }
  }
}

// 底部添加按钮
.bottom-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 20rpx 30rpx 40rpx;
  background: #fff;
  box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.add-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
  gap: 12rpx;

  .add-icon {
    font-size: 36rpx;
    font-weight: 400;
  }
}

// 弹窗样式
.popup-content {
  background: #fff;
  border-radius: 40rpx 40rpx 0 0;
  padding-bottom: constant(safe-area-inset-bottom);
  padding-bottom: env(safe-area-inset-bottom);
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 30rpx;
  border-bottom: 1rpx solid #f0f0f0;

  .popup-title {
    font-size: 32rpx;
    font-weight: 600;
    color: #333;
  }

  .popup-close {
    font-size: 36rpx;
    color: #999;
    padding: 10rpx;
  }
}

.form-list {
  padding: 20rpx 30rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 24rpx 0;
  border-bottom: 1rpx solid #f5f5f5;

  &.checkbox-item {
    justify-content: space-between;
  }
}

.form-label {
  width: 160rpx;
  font-size: 28rpx;
  color: #333;
  font-weight: 500;
}

.form-input {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  height: 60rpx;
}

.form-textarea {
  flex: 1;
  font-size: 28rpx;
  color: #333;
  height: 120rpx;
  padding-top: 10rpx;
}

.popup-footer {
  padding: 30rpx;
}

.submit-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  height: 90rpx;
  border-radius: 45rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30rpx;
  font-weight: 600;
}
</style>
