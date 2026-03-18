<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCourseList, createCourse, updateCourse, deleteCourse } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ current: 1, size: 10, keyword: '' })
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = reactive({ id: null, name: '', description: '', subject: '', grade: '' })

async function loadList() {
  loading.value = true
  try {
    const res = await getCourseList(query)
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || list.value.length
  } catch {}
  loading.value = false
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', description: '', subject: '', grade: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, { id: row.id, name: row.name, description: row.description, subject: row.subject, grade: row.grade })
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.name) { ElMessage.warning('请输入课程名称'); return }
  try {
    if (isEdit.value) {
      await updateCourse(form.id, form)
    } else {
      await createCourse(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadList()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除课程「${row.name}」？`, '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>

<template>
  <div>
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>新建课程</el-button>
    </div>

    <div class="filter-bar">
      <el-input v-model="query.keyword" placeholder="搜索课程名称" clearable style="width:240px" @clear="loadList" @keyup.enter="loadList" />
      <el-button type="primary" @click="loadList"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="subject" label="学科" width="100" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px;">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新建课程'" width="500px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="课程名称"><el-input v-model="form.name" placeholder="例：高一数学" /></el-form-item>
        <el-form-item label="学科"><el-input v-model="form.subject" placeholder="例：数学" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="form.grade" placeholder="例：高一" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
