import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/',
    component: () => import('../layout/AppLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '工作台' } },
      { path: 'course', name: 'Course', component: () => import('../views/course/CourseList.vue'), meta: { title: '课程管理' } },
      { path: 'lesson-plan', name: 'LessonPlan', component: () => import('../views/lessonplan/LessonPlanList.vue'), meta: { title: '教案管理' } },
      { path: 'lesson-plan/:id', name: 'LessonPlanDetail', component: () => import('../views/lessonplan/LessonPlanDetail.vue'), meta: { title: '教案详情' } },
      { path: 'exercise', name: 'Exercise', component: () => import('../views/exercise/ExerciseList.vue'), meta: { title: '练习题管理' } },
      { path: 'student', name: 'Student', component: () => import('../views/student/StudentList.vue'), meta: { title: '学生管理' } },
      { path: 'score', name: 'Score', component: () => import('../views/score/ScoreList.vue'), meta: { title: '成绩管理' } },
      { path: 'knowledge-point', name: 'KnowledgePoint', component: () => import('../views/knowledge/KnowledgePointList.vue'), meta: { title: '知识点管理' } },
      { path: 'analytics', name: 'Analytics', component: () => import('../views/analytics/AnalyticsOverview.vue'), meta: { title: '学情分析' } },
      { path: 'prep-plan', name: 'PrepPlan', component: () => import('../views/prepplan/PrepPlanList.vue'), meta: { title: '备课方案' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title || ''} - AI备课助手`
  if (!to.meta.noAuth && !localStorage.getItem('token')) {
    next('/login')
  } else {
    next()
  }
})

export default router
