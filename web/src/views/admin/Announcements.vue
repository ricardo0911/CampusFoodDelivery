<template>
  <div class="page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>公告管理</span>
          <el-button type="primary" @click="showDialog()">新建公告</el-button>
        </div>
      </template>

      <el-table :data="announcements" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="target" label="目标" width="100">
          <template #default="{ row }">
            {{ getTargetName(row.target) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="success" v-if="row.status === 0" @click="publish(row)">发布</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '新建公告'" width="600px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type">
            <el-option :value="1" label="系统公告" />
            <el-option :value="2" label="活动通知" />
            <el-option :value="3" label="维护通知" />
          </el-select>
        </el-form-item>
        <el-form-item label="目标">
          <el-select v-model="form.target">
            <el-option :value="0" label="全部" />
            <el-option :value="1" label="用户" />
            <el-option :value="2" label="商家" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="弹窗显示">
          <el-switch v-model="form.isPopup" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="有效期">
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

const announcements = ref([])
const dialogVisible = ref(false)
const form = ref({})

const getTypeName = (type) => {
  const names = { 1: '系统公告', 2: '活动通知', 3: '维护通知' }
  return names[type] || '未知'
}

const getTypeTag = (type) => {
  const tags = { 1: 'primary', 2: 'success', 3: 'warning' }
  return tags[type] || 'info'
}

const getTargetName = (target) => {
  const names = { 0: '全部', 1: '用户', 2: '商家' }
  return names[target] || '未知'
}

const loadData = async () => {
  try {
    const res = await request.get('/admin/announcement/list')
    announcements.value = res.data || []
  } catch (e) {
    console.error(e)
  }
}

const showDialog = (row = null) => {
  if (row) {
    form.value = { ...row, dateRange: row.startTime && row.endTime ? [row.startTime, row.endTime] : null }
  } else {
    form.value = { type: 1, target: 0, isPopup: 0 }
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
      await request.put(`/admin/announcement/${data.id}`, data)
    } else {
      await request.post('/admin/announcement/create', data)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

const publish = async (row) => {
  try {
    await request.post(`/admin/announcement/${row.id}/publish`)
    ElMessage.success('发布成功')
    loadData()
  } catch (e) {
    ElMessage.error('发布失败')
  }
}

const remove = async (row) => {
  try {
    await ElMessageBox.confirm('确定删除该公告？', '提示')
    await request.delete(`/admin/announcement/${row.id}`)
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
