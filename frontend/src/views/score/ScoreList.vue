<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getScoreList, createScore, batchCreateScore, deleteScore, getCourseList, getStudentList } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const courses = ref([])
const query = reactive({ current: 1, size: 10, courseId: '', studentId: '' })
const dialogVisible = ref(false)
const form = reactive({ studentId: '', courseId: '', score: 0, examName: '', answerDuration: 0 })
const batchDialogVisible = ref(false)
const batchText = ref('')

async function loadList() {
  loading.value = true
  try {
    const res = await getScoreList(query)
    list.value = res.data?.records || res.data || []
    total.value = res.data?.total || list.value.length
  } catch {}
  loading.value = false
}

async function loadCourses() {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    courses.value = res.data?.records || res.data || []
  } catch {}
}

function openCreate() {
  Object.assign(form, { studentId: '', courseId: '', score: 0, examName: '', answerDuration: 0 })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await createScore(form)
    ElMessage.success('录入成功')
    dialogVisible.value = false
    loadList()
  } catch {}
}

async function handleBatch() {
  try {
    const data = JSON.parse(batchText.value)
    await batchCreateScore(data)
    ElMessage.success('批量导入成功')
    batchDialogVisible.value = false
    loadList()
  } catch (e) {
    ElMessage.error('数据格式错误，请检查JSON')
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除此成绩记录？', '提示', { type: 'warning' })
  await deleteScore(row.id)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => { loadList(); loadCourses() })
</script>

<template>
  <div>
    <div class="page-header">
      <h2>成绩管理</h2>
      <div style="display:flex;gap:12px;">
        <el-button @click="batchDialogVisible = true"><el-icon><Upload /></el-icon>批量导入</el-button>
        <el-button type="primary" @click="openCreate"><el-icon><Plus /></el-icon>录入成绩</el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.courseId" placeholder="选择课程" clearable style="width:180px" @change="loadList">
        <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="loadList"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="studentName" label="学生" width="120">
          <template #default="{ row }">{{ row.studentName || row.studentId }}</template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程" width="150">
          <template #default="{ row }">{{ row.courseName || row.courseId }}</template>
        </el-table-column>
        <el-table-column prop="examName" label="考试名称" min-width="150" />
        <el-table-column prop="score" label="成绩" width="80">
          <template #default="{ row }">
            <span :style="{ color: row.score >= 60 ? 'var(--clay-primary-dark)' : '#B54747', fontWeight: 600 }">{{ row.score }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="answerDuration" label="答题时长(分)" width="110" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px;">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- Single create -->
    <el-dialog v-model="dialogVisible" title="录入成绩" width="460px" destroy-on-close>
      <el-form label-width="90px">
        <el-form-item label="学生ID"><el-input v-model="form.studentId" /></el-form-item>
        <el-form-item label="课程">
          <el-select v-model="form.courseId" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="考试名称"><el-input v-model="form.examName" /></el-form-item>
        <el-form-item label="成绩"><el-input-number v-model="form.score" :min="0" :max="100" /></el-form-item>
        <el-form-item label="答题时长(分)"><el-input-number v-model="form.answerDuration" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <!-- Batch import -->
    <el-dialog v-model="batchDialogVisible" title="批量导入成绩" width="560px" destroy-on-close>
      <p style="margin-bottom:12px;font-size:13px;color:var(--clay-text-light);">
        请输入JSON数组格式，每条包含 studentId, courseId, examName, score, answerDuration
      </p>
      <el-input v-model="batchText" type="textarea" :rows="10" placeholder='[{"studentId":1,"courseId":1,"examName":"期中考试","score":85,"answerDuration":60}]' />
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBatch">导入</el-button>
      </template>
    </el-dialog>
  </div>
</template>
