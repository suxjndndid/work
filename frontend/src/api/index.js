import request from './request'

// ========== SSE 流式请求工具 ==========
/**
 * 通用 SSE 流式请求函数（基于 fetch + ReadableStream）
 * @param {string} url - 请求路径（不含 /api 前缀）
 * @param {object} options - { method, body }
 * @param {function} onMessage - 每收到一个 token 的回调 (token: string)
 * @param {function} onDone - 流式完成回调
 * @param {function} onError - 错误回调 (error: Error)
 */
export async function fetchSSE(url, options = {}, onMessage, onDone, onError) {
  const token = localStorage.getItem('token')
  const method = options.method || 'POST'
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = token

  // 解析 SSE data 字段：后端用 JSON 编码了 token（保留换行符等特殊字符）
  function parseToken(data) {
    if (!data || data === '[DONE]') return data
    try {
      return JSON.parse(data)
    } catch {
      return data
    }
  }

  try {
    const response = await fetch('/api' + url, {
      method,
      headers,
      body: options.body ? JSON.stringify(options.body) : null,
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      // SSE 格式解析：按双换行分割事件
      const parts = buffer.split('\n\n')
      buffer = parts.pop() // 保留未完成的部分

      for (const part of parts) {
        const lines = part.split('\n')
        let eventName = ''
        let dataLines = []
        for (const line of lines) {
          if (line.startsWith('event:')) {
            eventName = line.slice(6).trim()
          } else if (line.startsWith('data:')) {
            dataLines.push(line.slice(5))
          }
        }
        const data = dataLines.join('\n')
        // 跳过 id 事件（教案生成时发送的记录ID）
        if (eventName === 'id') continue
        if (data === '[DONE]') {
          onDone && onDone()
          return
        }
        if (data) {
          onMessage && onMessage(parseToken(data))
        }
      }
    }
    // 处理 buffer 中剩余的数据
    if (buffer.trim()) {
      const lines = buffer.split('\n')
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const data = line.slice(5)
          if (data === '[DONE]') { onDone && onDone(); return }
          if (data) onMessage && onMessage(parseToken(data))
        }
      }
    }
    onDone && onDone()
  } catch (error) {
    onError && onError(error)
  }
}

// ========== Auth ==========
export const login = (data) => request.post('/auth/login', data)
export const logout = () => request.post('/auth/logout')
export const getUserInfo = () => request.get('/auth/info')

// ========== Course ==========
export const getCourseList = (params) => request.get('/course/list', { params })
export const getCourse = (id) => request.get(`/course/${id}`)
export const createCourse = (data) => request.post('/course', data)
export const updateCourse = (id, data) => request.put(`/course/${id}`, data)
export const deleteCourse = (id) => request.delete(`/course/${id}`)

// ========== Lesson Plan ==========
export const getLessonPlanList = (params) => request.get('/lesson-plan/list', { params })
export const getLessonPlan = (id) => request.get(`/lesson-plan/${id}`)
// generate 和 regenerate 改为流式，通过 fetchSSE 调用
export const streamGenerateLessonPlan = (data, onMessage, onDone, onError) =>
  fetchSSE('/lesson-plan/generate', { body: data }, onMessage, onDone, onError)
export const streamRegenerateLessonPlan = (id, onMessage, onDone, onError) =>
  fetchSSE(`/lesson-plan/${id}/regenerate`, {}, onMessage, onDone, onError)
export const updateLessonPlan = (id, data) => request.put(`/lesson-plan/${id}`, data)
export const deleteLessonPlan = (id) => request.delete(`/lesson-plan/${id}`)
export const getLessonPlanVersions = (id) => request.get(`/lesson-plan/${id}/versions`)
export const getTemplates = () => request.get('/lesson-plan/templates')
export const extractKeywords = (id) => request.post(`/lesson-plan/${id}/keywords`, null, { timeout: 300000 })
export const generateImage = (id, keywords) => request.post(`/lesson-plan/${id}/generate-image`, { keywords }, { timeout: 300000 })

// ========== Exercise ==========
export const getExerciseList = (params) => request.get('/exercise/list', { params })
export const generateExercise = (data) => request.post('/exercise/generate', data, { timeout: 300000 })
export const updateExercise = (id, data) => request.put(`/exercise/${id}`, data)
export const deleteExercise = (id) => request.delete(`/exercise/${id}`)

// ========== Student ==========
export const getStudentList = (params) => request.get('/student/list', { params })
export const getStudent = (id) => request.get(`/student/${id}`)
export const createStudent = (data) => request.post('/student', data)
export const updateStudent = (id, data) => request.put(`/student/${id}`, data)
export const deleteStudent = (id) => request.delete(`/student/${id}`)

// ========== Score ==========
export const getScoreList = (params) => request.get('/score/list', { params })
export const createScore = (data) => request.post('/score', data)
export const batchCreateScore = (data) => request.post('/score/batch', data)
export const deleteScore = (id) => request.delete(`/score/${id}`)

// ========== Knowledge Point ==========
export const getKnowledgePointList = (courseId) => request.get('/knowledge-point/list', { params: { courseId } })
export const createKnowledgePoint = (data) => request.post('/knowledge-point', data)
export const updateKnowledgePoint = (id, data) => request.put(`/knowledge-point/${id}`, data)
export const deleteKnowledgePoint = (id) => request.delete(`/knowledge-point/${id}`)

// ========== Analytics (流式) ==========
export const getClassAnalytics = (courseId) => request.get(`/analytics/class/${courseId}`)
export const getStudentAnalytics = (studentId) => request.get(`/analytics/student/${studentId}`)
export const streamClassAiReport = (courseId, onMessage, onDone, onError) =>
  fetchSSE(`/analytics/ai-report/${courseId}`, {}, onMessage, onDone, onError)
export const streamStudentAiReport = (studentId, onMessage, onDone, onError) =>
  fetchSSE(`/analytics/ai-student-report/${studentId}`, {}, onMessage, onDone, onError)
export const streamStudentRecommend = (studentId, onMessage, onDone, onError) =>
  fetchSSE(`/analytics/recommend/${studentId}`, {}, onMessage, onDone, onError)

// ========== Prep Plan (流式) ==========
export const streamGeneratePrepPlan = (data, onMessage, onDone, onError) =>
  fetchSSE('/prep-plan/generate', { body: data }, onMessage, onDone, onError)
export const getPrepPlanList = (params) => request.get('/prep-plan/list', { params })
export const getPrepPlan = (id) => request.get(`/prep-plan/${id}`)
