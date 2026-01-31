<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>活动管理</span>
          <el-button type="primary" @click="showDialog()">新建活动</el-button>
        </div>
      </template>

      <el-table :data="activities" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="活动名称" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="200">
          <template #default="{ row }">
            {{ formatDate(row.startTime) }} - {{ formatDate(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250">
          <template #default="{ row }">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="success" v-if="row.status === 0" @click="enable(row)">启用</el-button>
            <el-button size="small" type="warning" v-if="row.status === 1" @click="disable(row)">停用</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑活动' : '新建活动'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option :value="1" label="满减活动" />
            <el-option :value="2" label="折扣活动" />
            <el-option :value="3" label="免配送费" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="横幅图">
          <el-input v-model="form.banner" placeholder="图片URL" />
        </el-form-item>
        <el-form-item label="活动时间">
          <el-date-picker v-model="form.dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../../api/request'

const activities = ref([])
const dialogVisible = ref(false)
const form = ref({})

const getTypeName = (type) => {
  const names = { 1: '满减活动', 2: '折扣活动', 3: '免配送费' }
  return names[type] || '未知'
}

const getTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'warning' }
  return tags[type] || 'info'
}

const getStatusName = (status) => {
  const names = { 0: '草稿', 1: '进行中', 2: '已结束' }
  return names[status] || '未知'
}

const getStatusTag = (status) => {
  const tags = { 0: 'info', 1: 'success', 2: 'danger' }
  return tags[status] || 'info'
}

const formatDate = (date) => {
  if (!date) return '-'
  return new Date(date).toLocaleDateString()
}

const loadData = async () => {
  try {
    const res = await request.get('/admin/activity/list')
    activities.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const showDialog = (row = null) => {
  if (row) {
    form.value = { ...row, dateRange: row.startTime && row.endTime ? [row.startTime, row.endTime] : null }
  } else {
    form.value = { type: 1 }
  }
  dialogVisible.value = true
}

const save = async () => {
  try {
    const data = { ...form.value }
    if (data.dateRange) {
      data.startTime = data.dateRange[0]
      data.endTime = data.dateRange[1]
    }
    delete data.dateRange

    if (data.id) {
      await request.put(`/admin/activity/${data.id}`, data)
    } else {
      await request.post('/admin/activity/create', data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const enable = async (row) => {
  try {
    await request.post(`/admin/activity/${row.id}/enable`)
    ElMessage.success('启用成功')
    loadData()
  } catch (e) {
    ElMessage.error('启用失败')
  }
}

const disable = async (row) => {
  try {
    await request.post(`/admin/activity/${row.id}/disable`)
    ElMessage.success('停用成功')
    loadData()
  } catch (e) {
    ElMessage.error('停用失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该活动？', '提示')
    await request.delete(`/admin/activity/${row.id}`)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
