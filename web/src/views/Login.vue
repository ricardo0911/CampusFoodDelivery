<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">校园订餐系统</h1>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" prefix-icon="User" placeholder="商家账号" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" prefix-icon="Lock" type="password" placeholder="密码" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登 录</el-button>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import request from '../api/request'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const formRef = ref()
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  loading.value = true
  try {
    const userType = 'merchant'
    const res = await request.post('/auth/login', { ...form, userType })
    userStore.setLogin(res.data, userType)
    ElMessage.success('登录成功')
    router.push('/merchant')
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: 
    linear-gradient(135deg, rgba(255, 107, 53, 0.9) 0%, rgba(255, 154, 86, 0.85) 50%, rgba(255, 195, 113, 0.9) 100%),
    url('https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=1920&q=80') center/cover no-repeat;
  position: relative;
}
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 255, 255, 0.15) 0%, transparent 40%);
  pointer-events: none;
}
.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.25);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #ff6b35;
  font-size: 28px;
  font-weight: 700;
}
.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  margin-top: 10px;
  background: linear-gradient(135deg, #ff6b35 0%, #ff9a56 100%);
  border: none;
  border-radius: 8px;
  transition: all 0.3s ease;
}
.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 53, 0.4);
}
</style>
