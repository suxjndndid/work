import request from './request'

// Auth
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getUserInfo = () => request.get('/auth/info')

// Course
export const getCourseList = (params) => request.get('/course/list', { params })
export const getCourse = (id) => request.get(`/course/${id}`)
export const createCourse = (data) => request.post('/course', data)
export const updateCourse = (id, data) => request.put(`/course/${id}`, data)
export const deleteCourse = (id) => request.delete(`/course/${id}`)

// Lesson Plan
export const getLessonPlanList = (params) => request.get('/lesson-plan/list', { params })
export const getLessonPlan = (id) => request.get(`/lesson-plan/${id}`)
export const generateLessonPlan = (data) => request.post('/lesson-plan/generate', data, { timeout: 300000 })
export const regenerateLessonPlan = (id) => request.post(`/lesson-plan/${id}/regenerate`, null, { timeout: 300000 })
export const updateLessonPlan = (id, data) => request.put(`/lesson-plan/${id}`, data)
export const deleteLessonPlan = (id) => request.delete(`/lesson-plan/${id}`)
export const getLessonPlanVersions = (id) => request.get(`/lesson-plan/${id}/versions`)
export const getTemplates = () => request.get('/lesson-plan/templates')
export const extractKeywords = (id) => request.post(`/lesson-plan/${id}/keywords`, null, { timeout: 300000 })
export const generateImage = (id, keyword) => request.post(`/lesson-plan/${id}/generate-image`, { keyword }, { timeout: 300000 })

// Exercise
export const getExerciseList = (params) => request.get('/exercise/list', { params })
export const generateExercise = (data) => request.post('/exercise/generate', data, { timeout: 300000 })
export const updateExercise = (id, data) => request.put(`/exercise/${id}`, data)
export const deleteExercise = (id) => request.delete(`/exercise/${id}`)

// Student
export const getStudentList = (params) => request.get('/student/list', { params })
export const getStudent = (id) => request.get(`/student/${id}`)
export const createStudent = (data) => request.post('/student', data)
export const updateStudent = (id, data) => request.put(`/student/${id}`, data)
export const deleteStudent = (id) => request.delete(`/student/${id}`)

// Score
export const getScoreList = (params) => request.get('/score/list', { params })
export const createScore = (data) => request.post('/score', data)
export const batchCreateScore = (data) => request.post('/score/batch', data)
export const deleteScore = (id) => request.delete(`/score/${id}`)

// Knowledge Point
export const getKnowledgePointList = (courseId) => request.get('/knowledge-point/list', { params: { courseId } })
export const createKnowledgePoint = (data) => request.post('/knowledge-point', data)
export const updateKnowledgePoint = (id, data) => request.put(`/knowledge-point/${id}`, data)
export const deleteKnowledgePoint = (id) => request.delete(`/knowledge-point/${id}`)

// Analytics
export const getClassAnalytics = (courseId) => request.get(`/analytics/class/${courseId}`)
export const getStudentAnalytics = (studentId) => request.get(`/analytics/student/${studentId}`)
export const getClassAiReport = (courseId) => request.post(`/analytics/ai-report/${courseId}`, null, { timeout: 300000 })
export const getStudentAiReport = (studentId) => request.post(`/analytics/ai-student-report/${studentId}`, null, { timeout: 300000 })
export const getStudentRecommend = (studentId) => request.post(`/analytics/recommend/${studentId}`, null, { timeout: 300000 })

// Prep Plan
export const generatePrepPlan = (data) => request.post('/prep-plan/generate', data, { timeout: 300000 })
export const getPrepPlanList = (params) => request.get('/prep-plan/list', { params })
export const getPrepPlan = (id) => request.get(`/prep-plan/${id}`)
