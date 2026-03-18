<script setup>
import { ref, onMounted } from 'vue'
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

// toggle: 'analysis' or 'recommend'
const activeAiView = ref('analysis')

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
  activeAiView.value = 'analysis'
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
  activeAiView.value = 'recommend'
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
              <div style="font-size:28px;font-weight:700;">{{ classData.averageScore != null ? Number(classData.averageScore).toFixed(1) : '-' }}</div>
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

          <!-- Exam trends -->
          <div v-if="classData?.examTrends?.length" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">各次考试平均分趋势</h4>
            <div v-for="t in classData.examTrends" :key="t.examName" style="margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                <span style="font-size:13px;">{{ t.examName }}</span>
                <span style="font-size:13px;font-weight:600;">{{ Number(t.averageScore).toFixed(1) }}分</span>
              </div>
              <el-progress :percentage="Number(t.averageScore)" :stroke-width="10" :color="t.averageScore >= 80 ? 'var(--clay-primary)' : t.averageScore >= 60 ? 'var(--clay-secondary-dark)' : '#DB8282'" :show-text="false" />
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
          </div>

          <!-- Student stats -->
          <div v-if="studentData" style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-bottom:24px;">
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">平均分</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.averageScore != null ? Number(studentData.averageScore).toFixed(1) : '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">考试次数</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.examCount || '-' }}</div>
            </div>
            <div class="clay-stat-card">
              <div style="font-size:12px;opacity:0.8;">知识点数</div>
              <div style="font-size:28px;font-weight:700;">{{ studentData.knowledgeMasteries?.length || '-' }}</div>
            </div>
          </div>

          <!-- Score history -->
          <div v-if="studentData?.examScores?.length" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">成绩记录</h4>
            <el-table :data="studentData.examScores" stripe size="small">
              <el-table-column prop="examName" label="考试" />
              <el-table-column prop="score" label="成绩" width="80">
                <template #default="{ row }">
                  <span :style="{ color: row.score >= 60 ? 'var(--clay-primary-dark)' : '#B54747', fontWeight: 600 }">{{ row.score }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="totalScore" label="满分" width="80" />
              <el-table-column prop="examDate" label="日期" width="120" />
            </el-table>
          </div>

          <!-- Knowledge mastery -->
          <div v-if="studentData?.knowledgeMasteries?.length" class="clay-card" style="margin-bottom:24px;">
            <h4 style="margin-bottom:16px;color:var(--clay-tertiary-dark);">知识点掌握度</h4>
            <div v-for="km in studentData.knowledgeMasteries" :key="km.knowledgePointName" style="margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;margin-bottom:4px;">
                <span style="font-size:13px;">{{ km.knowledgePointName }}</span>
                <span style="font-size:13px;font-weight:600;">{{ Number(km.masteryLevel).toFixed(0) }}%</span>
              </div>
              <el-progress :percentage="Number(km.masteryLevel)" :stroke-width="10" :color="km.masteryLevel >= 80 ? 'var(--clay-primary)' : km.masteryLevel >= 60 ? 'var(--clay-secondary-dark)' : '#DB8282'" :show-text="false" />
            </div>
          </div>

          <!-- Toggle buttons -->
          <div style="display:flex;gap:12px;margin-bottom:20px;">
            <el-button
              :type="activeAiView === 'analysis' ? 'primary' : 'default'"
              :loading="generatingStudentReport"
              @click="activeAiView === 'analysis' && !studentReport ? handleStudentAiReport() : (activeAiView = 'analysis')"
              :disabled="!selectedStudentId"
            >
              <el-icon><MagicStick /></el-icon>&nbsp;AI 学情分析
            </el-button>
            <el-button
              :type="activeAiView === 'recommend' ? 'warning' : 'default'"
              :loading="generatingRecommend"
              @click="activeAiView === 'recommend' && !recommendation ? handleRecommend() : (activeAiView = 'recommend')"
              :disabled="!selectedStudentId"
            >
              <el-icon><Promotion /></el-icon>&nbsp;个性化推荐
            </el-button>
          </div>

          <!-- AI Analysis card (v-show preserves content) -->
          <div v-show="activeAiView === 'analysis'">
            <el-card>
              <template #header>
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <span style="font-weight:600;">AI 学生分析报告</span>
                  <el-button v-if="studentReport" type="primary" size="small" :loading="generatingStudentReport" @click="handleStudentAiReport">
                    <el-icon><Refresh /></el-icon>&nbsp;重新生成
                  </el-button>
                </div>
              </template>
              <div v-if="generatingStudentReport" style="text-align:center;padding:40px;">
                <el-icon class="is-loading" :size="32" color="var(--clay-primary)"><Loading /></el-icon>
                <p style="margin-top:12px;color:var(--clay-text-light);">AI 正在分析学情数据...</p>
              </div>
              <MdRender v-else-if="studentReport" :content="studentReport" />
              <el-empty v-else description="点击上方按钮生成 AI 学情分析报告" :image-size="80" />
            </el-card>
          </div>

          <!-- Recommendation card (v-show preserves content) -->
          <div v-show="activeAiView === 'recommend'">
            <el-card>
              <template #header>
                <div style="display:flex;justify-content:space-between;align-items:center;">
                  <div style="display:flex;align-items:center;gap:8px;">
                    <el-icon color="var(--clay-primary)"><Promotion /></el-icon>
                    <span style="font-weight:600;">个性化资源推荐</span>
                  </div>
                  <el-button v-if="recommendation" type="warning" size="small" :loading="generatingRecommend" @click="handleRecommend">
                    <el-icon><Refresh /></el-icon>&nbsp;重新生成
                  </el-button>
                </div>
              </template>
              <div v-if="generatingRecommend" style="text-align:center;padding:40px;">
                <el-icon class="is-loading" :size="32" color="var(--clay-primary)"><Loading /></el-icon>
                <p style="margin-top:12px;color:var(--clay-text-light);">AI 正在生成个性化推荐...</p>
              </div>
              <MdRender v-else-if="recommendation" :content="recommendation" />
              <el-empty v-else description="点击上方按钮生成个性化学习资源推荐" :image-size="80" />
            </el-card>
          </div>

        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>
