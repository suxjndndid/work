<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getCourseList, getStudentList, getClassAnalytics, getStudentAnalytics, getClassAiReport, getStudentAiReport, getStudentRecommend } from '../../api'
import MdRender from '../../components/MdRender.vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('class')
const courses = ref([])
const students = ref([])
const selectedCourseId = ref('')
const selectedStudentId = ref('')

// class
const classData = ref(null)
const classReport = ref('')
const loadingClass = ref(false)
const generatingClassReport = ref(false)

// student
const studentData = ref(null)
const studentReport = ref('')
const recommendation = ref('')
const loadingStudent = ref(false)
const generatingStudentReport = ref(false)
const generatingRecommend = ref(false)

async function loadCourses() {
  try {
    const res = await getCourseList({ current: 1, size: 100 })
    courses.value = res.data?.records || res.data || []
  } catch {}
}

async function loadStudents() {
  try {
    const res = await getStudentList({ current: 1, size: 200 })
    students.value = res.data?.records || res.data || []
  } catch {}
}

async function loadClassData() {
  if (!selectedCourseId.value) return
  loadingClass.value = true
  classReport.value = ''
  try {
    const res = await getClassAnalytics(selectedCourseId.value)
    classData.value = res.data
  } catch {}
  loadingClass.value = false
}

async function handleClassAiReport() {
  if (!selectedCourseId.value) { ElMessage.warning('请先选择课程'); return }
  generatingClassReport.value = true
  try {
    const res = await getClassAiReport(selectedCourseId.value)
    classReport.value = res.data || ''
    ElMessage.success('分析报告生成成功')
  } catch {}
  generatingClassReport.value = false
}

async function loadStudentData() {
  if (!selectedStudentId.value) return
  loadingStudent.value = true
  studentReport.value = ''
  recommendation.value = ''
  try {
    const res = await getStudentAnalytics(selectedStudentId.value)
    studentData.value = res.data
  } catch {}
  loadingStudent.value = false
}

async function handleStudentAiReport() {
  if (!selectedStudentId.value) { ElMessage.warning('请先选择学生'); return }
  generatingStudentReport.value = true
  try {
    const res = await getStudentAiReport(selectedStudentId.value)
    studentReport.value = res.data || ''
    ElMessage.success('学生分析报告生成成功')
  } catch {}
  generatingStudentReport.value = false
}

async function handleRecommend() {
  if (!selectedStudentId.value) { ElMessage.warning('请先选择学生'); return }
  generatingRecommend.value = true
  try {
    const res = await getStudentRecommend(selectedStudentId.value)
    recommendation.value = res.data || ''
    ElMessage.success('个性化推荐生成成功')
  } catch {}
  generatingRecommend.value = false
}

onMounted(() => { loadCourses(); loadStudents() })
</script>

<template>
  <div>
    <div class="page-header">
      <h2>学情分析</h2>
    </div>

    <el-tabs v-model="activeTab" class="clay-card" style="padding:0;">
      <!-- Class analytics -->
      <el-tab-pane label="班级学情分析" name="class">
        <div style="padding:24px;">
          <div style="display:flex;gap:12px;margin-bottom:24px;">
            <el-select v-model="selectedCourseId" placeholder="选择课程" style="width:240px" @change="loadClassData">
              <el-option v-for="c in courses" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
            <el-button type="primary" :loading="generatingClassReport" @click="handleClassAiReport" :disabled="!selectedCourseId">
              <el-icon><MagicStick /></el-icon>&nbsp;AI 分析报告
            </el-button>
          </div>

          <!-- Stats cards -->
          <div v-if="classData" style="display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:24px;">
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">班级人数</div>
              <div style="font-size:28px;font-weight:700;">{{ classData.studentCount || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">平均分</div>
              <div style="font-size:28px;font-weight:700;">{{ classData.avgScore?.toFixed(1) || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">最高分</div>
              <div style="font-size:28px;font-weight:700;">{{ classData.maxScore || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">及格率</div>
              <div style="font-size:28px;font-weight:700;">{{ classData.passRate != null ? classData.passRate.toFixed(1) + '%' : '-' }}</div>
            </div>
          </div>

          <!-- Score distribution -->
          <div v-if="classData?.scoreDistribution" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">成绩分布</h4>
            <div style="display:flex;gap:8px;align-items:end;height:120px;">
              <div v-for="(count, range) in classData.scoreDistribution" :key="range" style="flex:1;display:flex;flex-direction:column;align-items:center;gap:4px;">
                <div :style="{ height: Math.max(count * 20, 8) + 'px', width: '100%', background: 'linear-gradient(180deg, var(--clay-primary), var(--clay-primary-dark))', borderRadius: '6px 6px 0 0', transition: 'height 0.5s' }"></div>
                <span style="font-size:11px;color:var(--clay-text-light);">{{ range }}</span>
                <span style="font-size:12px;font-weight:600;">{{ count }}</span>
              </div>
            </div>
          </div>

          <!-- Knowledge mastery -->
          <div v-if="classData?.knowledgeMastery" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">知识点掌握率</h4>
            <div v-for="(rate, name) in classData.knowledgeMastery" :key="name" style="margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                <span style="font-size:13px;">{{ name }}</span>
                <span style="font-size:13px;font-weight:600;">{{ (rate * 100).toFixed(0) }}%</span>
              </div>
              <el-progress :percentage="rate * 100" :stroke-width="10" :color="rate >= 0.8 ? 'var(--clay-primary)' : rate >= 0.6 ? 'var(--clay-secondary-dark)' : '#DB8282'" :show-text="false" style="border-radius:5px;" />
            </div>
          </div>

          <!-- AI Report -->
          <el-card v-if="classReport">
            <template #header><span style="font-weight:600;">AI 分析报告</span></template>
            <MdRender :content="classReport" />
          </el-card>
        </div>
      </el-tab-pane>

      <!-- Student analytics -->
      <el-tab-pane label="学生个人分析" name="student">
        <div style="padding:24px;">
          <div style="display:flex;gap:12px;margin-bottom:24px;">
            <el-select v-model="selectedStudentId" placeholder="选择学生" filterable style="width:240px" @change="loadStudentData">
              <el-option v-for="s in students" :key="s.id" :label="`${s.name} (${s.studentNo || s.id})`" :value="s.id" />
            </el-select>
            <el-button type="primary" :loading="generatingStudentReport" @click="handleStudentAiReport" :disabled="!selectedStudentId">
              <el-icon><MagicStick /></el-icon>&nbsp;AI 分析
            </el-button>
            <el-button type="warning" :loading="generatingRecommend" @click="handleRecommend" :disabled="!selectedStudentId">
              <el-icon><Promotion /></el-icon>&nbsp;个性化推荐
            </el-button>
          </div>

          <!-- Student stats -->
          <div v-if="studentData" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:24px;">
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">平均分</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.avgScore?.toFixed(1) || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">考试次数</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.examCount || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">进步趋势</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.trend || '-' }}</div>
            </div>
          </div>

          <!-- Score history -->
          <div v-if="studentData?.scores?.length" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">成绩记录</h4>
            <el-table :data="studentData.scores" stripe size="small">
              <el-table-column prop="examName" label="考试" />
              <el-table-column prop="courseName" label="课程" />
              <el-table-column prop="score" label="成绩" width="80">
                <template #default="{ row }">
                  <span :style="{ color: row.score >= 60 ? 'var(--clay-primary-dark)' : '#B54747', fontWeight: 600 }">{{ row.score }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <!-- AI Student Report -->
          <el-card v-if="studentReport" style="margin-bottom:20px;">
            <template #header><span style="font-weight:600;">AI 学生分析</span></template>
            <MdRender :content="studentReport" />
          </el-card>

          <!-- Recommendation -->
          <el-card v-if="recommendation">
            <template #header>
              <div style="display:flex;align-items:center;gap:8px;">
                <el-icon color="var(--clay-primary)"><Promotion /></el-icon>
                <span style="font-weight:600;">个性化资源推荐</span>
              </div>
            </template>
            <MdRender :content="recommendation" />
          </el-card>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
