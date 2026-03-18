<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCourseList, getLessonPlanList, getExerciseList, getStudentList } from '../api'

const router = useRouter()
const stats = ref({ courses: 0, lessonPlans: 0, exercises: 0, students: 0 })
const recentPlans = ref([])

onMounted(async () => {
  try {
    const [c, l, e, s] = await Promise.all([
      getCourseList({ current: 1, size: 1 }).catch(() => ({ data: { total: 0 } })),
      getLessonPlanList({ current: 1, size: 5 }).catch(() => ({ data: { total: 0, records: [] } })),
      getExerciseList({ current: 1, size: 1 }).catch(() => ({ data: { total: 0 } })),
      getStudentList({ current: 1, size: 1 }).catch(() => ({ data: { total: 0 } })),
    ])
    stats.value = {
      courses: c.data?.total || 0,
      lessonPlans: l.data?.total || 0,
      exercises: e.data?.total || 0,
      students: s.data?.total || 0,
    }
    recentPlans.value = l.data?.records || []
  } catch {}
})

const quickActions = [
  { title: 'AI生成教案', desc: '输入主题，一键生成完整教案', icon: 'Document', path: '/lesson-plan', color: 'var(--clay-primary)' },
  { title: 'AI生成习题', desc: '按题型和难度智能出题', icon: 'EditPen', path: '/exercise', color: 'var(--clay-secondary-dark)' },
  { title: '学情分析', desc: 'AI分析班级学习情况', icon: 'DataAnalysis', path: '/analytics', color: 'var(--clay-tertiary)' },
  { title: '备课方案', desc: '整合生成完整备课方案', icon: 'Files', path: '/prep-plan', color: 'var(--clay-primary-dark)' },
]

const statCards = [
  { label: '课程数', key: 'courses', icon: 'Notebook', bg: 'linear-gradient(135deg, #8AB6BD, #B5D4D8)' },
  { label: '教案数', key: 'lessonPlans', icon: 'Document', bg: 'linear-gradient(135deg, #F9D8C5, #FBE8DC)' },
  { label: '习题数', key: 'exercises', icon: 'EditPen', bg: 'linear-gradient(135deg, #657B7F, #8A9EA2)' },
  { label: '学生数', key: 'students', icon: 'User', bg: 'linear-gradient(135deg, #8AB6BD, #657B7F)' },
]
</script>

<template>
  <div class="dashboard">
    <!-- Welcome -->
    <div class="welcome-banner">
      <div>
        <h2>欢迎使用 AI 备课助手</h2>
        <p>智能教案生成 · 学情分析 · 个性化推荐</p>
      </div>
      <el-button type="primary" size="large" @click="router.push('/lesson-plan')" round>
        <el-icon><MagicStick /></el-icon>&nbsp;开始备课
      </el-button>
    </div>

    <!-- Stats -->
    <div class="stat-grid">
      <div v-for="s in statCards" :key="s.key" class="stat-item" :style="{ background: s.bg }">
        <el-icon :size="32" color="rgba(255,255,255,0.85)"><component :is="s.icon" /></el-icon>
        <div class="stat-num">{{ stats[s.key] }}</div>
        <div class="stat-label">{{ s.label }}</div>
      </div>
    </div>

    <!-- Quick actions -->
    <h3 class="section-title">快捷操作</h3>
    <div class="action-grid">
      <div v-for="a in quickActions" :key="a.title" class="action-card clay-card" @click="router.push(a.path)">
        <el-icon :size="36" :color="a.color"><component :is="a.icon" /></el-icon>
        <h4>{{ a.title }}</h4>
        <p>{{ a.desc }}</p>
      </div>
    </div>

    <!-- Recent lesson plans -->
    <h3 class="section-title" v-if="recentPlans.length">最近教案</h3>
    <div v-if="recentPlans.length" class="recent-list">
      <div v-for="plan in recentPlans" :key="plan.id" class="recent-item clay-card" @click="router.push(`/lesson-plan/${plan.id}`)">
        <div class="recent-info">
          <strong>{{ plan.topic || plan.title || '未命名教案' }}</strong>
          <span class="recent-meta">{{ plan.subject }} · {{ plan.grade }}</span>
        </div>
        <el-tag size="small" :type="plan.content ? 'success' : 'info'">
          {{ plan.content ? '已生成' : '待生成' }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { max-width: 1200px; margin: 0 auto; }

.welcome-banner {
  background: linear-gradient(135deg, var(--clay-primary) 0%, var(--clay-tertiary) 100%);
  border-radius: var(--clay-radius-lg);
  padding: 32px 40px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 28px;
  box-shadow: var(--clay-shadow);
  color: #fff;
}
.welcome-banner h2 { font-size: 24px; font-weight: 700; margin-bottom: 6px; color: #fff; }
.welcome-banner p { opacity: 0.85; font-size: 14px; }

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}
.stat-item {
  border-radius: var(--clay-radius);
  padding: 24px;
  color: #fff;
  box-shadow: var(--clay-shadow);
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.stat-num { font-size: 32px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.85; }

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--clay-tertiary-dark);
  margin-bottom: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 32px;
}
.action-card {
  cursor: pointer;
  text-align: center;
  padding: 28px 20px;
}
.action-card:hover { transform: translateY(-4px); }
.action-card h4 { margin: 12px 0 6px; font-size: 16px; color: var(--clay-tertiary-dark); }
.action-card p { font-size: 13px; color: var(--clay-text-light); }

.recent-list { display: flex; flex-direction: column; gap: 12px; }
.recent-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
}
.recent-item:hover { transform: translateX(4px); }
.recent-info { display: flex; flex-direction: column; gap: 4px; }
.recent-info strong { font-size: 15px; color: var(--clay-tertiary-dark); }
.recent-meta { font-size: 12px; color: var(--clay-text-light); }
</style>
