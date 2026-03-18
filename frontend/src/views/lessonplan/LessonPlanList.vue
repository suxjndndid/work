<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getLessonPlanList, generateLessonPlan, deleteLessonPlan, getCourseList, getTemplates } from '../../api'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const list = ref([])
const total = ref(0)
const courses = ref([])
const templates = ref([])
const query = reactive({ current: 1, size: 10, courseId: '' })
const genDialogVisible = ref(false)
const genForm = reactive({ courseId: '', subject: '', grade: '', topic: '', objectives: '', duration: 45 })
const templateDialogVisible = ref(false)

async function loadList() {
  loading.value = true
  try {
    const res = await getLessonPlanList(query)
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

async function loadTemplates() {
  try {
    const res = await getTemplates()
    templates.value = res.data || []
  } catch {}
}

function openGenerate() {
  Object.assign(genForm, { courseId: '', subject: '', grade: '', topic: '', objectives: '', duration: 45 })
  genDialogVisible.value = true
}

async function handleGenerate() {
  if (!genForm.topic) { ElMessage.warning('请输入课程主题'); return }
  generating.value = true
  try {
    await generateLessonPlan(genForm)
    ElMessage.success('教案生成成功！')
    genDialogVisible.value = false
    loadList()
  } catch {}
  generating.value = false
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除教案「${row.topic || row.title}」？`, '提示', { type: 'warning' })
  await deleteLessonPlan(row.id)
  ElMessage.success('删除成功')
  loadList()
}

function viewDetail(row) {
  router.push(`/lesson-plan/${row.id}`)
}

function showTemplates() {
  loadTemplates()
  templateDialogVisible.value = true
}

onMounted(() => { loadList(); loadCourses() })
</script>

<template>
  <div>
    <div class="page-header">
      <h2>教案管理</h2>
      <div style="display:flex;gap:12px;">
        <el-button @click="showTemplates"><el-icon><Collection /></el-icon>互动环节模板</el-button>
        <el-button type="primary" @click="openGenerate"><el-icon><MagicStick /></el-icon>AI 生成教案</el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="query.courseId" placeholder="选择课程" clearable style="width:200px" @change="loadList">
        <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
      <el-button type="primary" @click="loadList"><el-icon><Search /></el-icon>搜索</el-button>
    </div>

    <el-card>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="topic" label="课题" min-width="180" />
        <el-table-column prop="subject" label="学科" width="90" />
        <el-table-column prop="grade" label="年级" width="90" />
        <el-table-column prop="duration" label="时长(分)" width="90" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.content ? 'success' : 'info'" size="small">{{ row.content ? '已生成' : '待生成' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display:flex;justify-content:flex-end;margin-top:16px;">
        <el-pagination v-model:current-page="query.current" v-model:page-size="query.size" :total="total" layout="total, prev, pager, next" @current-change="loadList" />
      </div>
    </el-card>

    <!-- AI Generate Dialog -->
    <el-dialog v-model="genDialogVisible" title="AI 生成教案" width="560px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="课程">
          <el-select v-model="genForm.courseId" placeholder="选择课程" style="width:100%">
            <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学科"><el-input v-model="genForm.subject" placeholder="例：数学" /></el-form-item>
        <el-form-item label="年级"><el-input v-model="genForm.grade" placeholder="例：高一" /></el-form-item>
        <el-form-item label="课题"><el-input v-model="genForm.topic" placeholder="例：集合的概念与表示" /></el-form-item>
        <el-form-item label="教学目标"><el-input v-model="genForm.objectives" type="textarea" :rows="2" placeholder="例：理解集合的含义，掌握集合的表示方法" /></el-form-item>
        <el-form-item label="课时(分)"><el-input-number v-model="genForm.duration" :min="15" :max="120" :step="5" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="genDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="generating" @click="handleGenerate">
          <el-icon><MagicStick /></el-icon>&nbsp;开始生成
        </el-button>
      </template>
    </el-dialog>

    <!-- Template Dialog -->
    <el-dialog v-model="templateDialogVisible" title="互动环节模板库" width="600px">
      <div v-if="templates.length" style="display:flex;flex-wrap:wrap;gap:12px;">
        <div v-for="t in templates" :key="t.name || t" class="clay-card" style="flex:1 1 calc(50% - 12px);min-width:240px;padding:16px;">
          <h4 style="margin-bottom:8px;color:var(--clay-tertiary-dark);">{{ t.name || t }}</h4>
          <p style="font-size:13px;color:var(--clay-text-light);">{{ t.description || '' }}</p>
        </div>
      </div>
      <el-empty v-else description="暂无模板" />
    </el-dialog>
  </div>
</template>
