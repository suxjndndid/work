# 项目模块分工表

> 项目: 智能教案生成与学情分析系统
> 日期: 2026-03-18
> 说明: 各成员自行领取模块，每人负责该模块的前端+后端+数据库完整闭环

---

## 模块总览

| 模块编号 | 模块名称 | 难度 | 负责人 |
|:---------|:---------|:-----|:-------|
| M1 | 用户认证模块 | ★☆☆ | |
| M2 | 课程管理模块 | ★☆☆ | |
| M3 | 学生管理模块 | ★☆☆ | |
| M4 | 成绩管理模块 | ★★☆ | |
| M5 | 知识点管理模块 | ★★☆ | |
| M6 | 教案管理模块 (含AI生成) | ★★★ | |
| M7 | 练习题管理模块 (含AI生成) | ★★★ | |
| M8 | 学情分析模块 (含AI报告) | ★★★ | |
| M9 | 备课方案模块 (含AI整合) | ★★★ | |
| M10 | 公共基础与部署 | ★★☆ | |

---

## 各模块详细说明

### M1 - 用户认证模块

**功能**: 登录/登出/获取当前用户信息

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `sys_user` 表 | 字段: id, username, password, real_name, role, avatar, email, status |
| 后端 Entity | `module/user/entity/User.java` | 继承 BaseEntity |
| 后端 Controller | `module/auth/controller/AuthController.java` | login/logout/info |
| 后端 Service | `module/auth/service/impl/AuthServiceImpl.java` | 密码校验(明文), Sa-Token |
| 后端配置 | `config/SaTokenConfig.java` | 拦截器, 路由放行 |
| 前端页面 | `views/Login.vue` | 登录表单 |
| 前端状态 | `store/user.js` | Pinia 用户状态 |
| 前端请求 | `api/request.js` | Axios 拦截器(Token注入) |
| API | `POST /api/auth/login`, `POST /api/auth/logout`, `GET /api/auth/info` | |

---

### M2 - 课程管理模块

**功能**: 课程的增删改查, 按当前教师过滤

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `course` 表 | 字段: id, user_id, name, subject, grade, semester, description |
| 后端 Entity | `module/course/entity/Course.java` | 继承 BaseEntity |
| 后端 Controller | `module/course/controller/CourseController.java` | CRUD, 按userId过滤 |
| 前端页面 | `views/course/CourseList.vue` | 课程列表+新建/编辑对话框 |
| API | `GET /api/course/list`, `POST /api/course`, `PUT /api/course/{id}`, `DELETE /api/course/{id}` | |

---

### M3 - 学生管理模块

**功能**: 学生信息增删改查

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `student` 表 | 字段: id, student_no, name, gender(0女1男), class_name, grade |
| 后端 Entity | `module/analytics/entity/Student.java` | 继承 BaseEntity |
| 后端 Controller | `module/analytics/controller/StudentController.java` | CRUD + 分页搜索 |
| 前端页面 | `views/student/StudentList.vue` | 学生列表+新建/编辑对话框 |
| API | `GET /api/student/list`, `POST /api/student`, `PUT /api/student/{id}`, `DELETE /api/student/{id}` | |

---

### M4 - 成绩管理模块

**功能**: 成绩录入/查询/删除, 支持批量JSON导入

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `score` 表 | 字段: id, student_id, course_id, exam_name, exam_type, total_score, score, exam_date |
| 后端 Entity | `module/analytics/entity/Score.java` | 不继承BaseEntity |
| 后端 Controller | `module/analytics/controller/ScoreController.java` | CRUD + batch |
| 前端页面 | `views/score/ScoreList.vue` | 成绩列表+录入+批量导入 |
| API | `GET /api/score/list`, `POST /api/score`, `POST /api/score/batch`, `DELETE /api/score/{id}` | |

---

### M5 - 知识点管理模块

**功能**: 按课程管理知识点(树形结构), 关联知识点掌握度

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `knowledge_point` 表 | 字段: id, course_id, name, parent_id, sort_order, description |
| 数据库 | `knowledge_point_mastery` 表 | 字段: id, student_id, knowledge_point_id, mastery_level, assessment_count |
| 后端 Entity | `module/analytics/entity/KnowledgePoint.java` | 继承 BaseEntity |
| 后端 Entity | `module/analytics/entity/KnowledgePointMastery.java` | 不继承BaseEntity |
| 后端 Controller | `module/analytics/controller/KnowledgePointController.java` | CRUD |
| 前端页面 | `views/knowledge/KnowledgePointList.vue` | 左侧课程选择 + 右侧知识点列表 |
| API | `GET /api/knowledge-point/list?courseId=`, `POST /api/knowledge-point`, `PUT /api/knowledge-point/{id}`, `DELETE /api/knowledge-point/{id}` | |

---

### M6 - 教案管理模块 (含AI生成) ★核心

**功能**: 教案CRUD + AI生成教案(含≥3互动环节) + 关键词提取 + AI生成图片 + 版本管理 + 互动模板

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `lesson_plan` 表 | 含 content(AI生成), status, version 等20+字段 |
| 数据库 | `lesson_plan_version` 表 | 版本历史 |
| 后端 Entity | `module/lessonplan/entity/LessonPlan.java` | 继承 BaseEntity |
| 后端 Entity | `module/lessonplan/entity/LessonPlanVersion.java` | 不继承BaseEntity |
| 后端 Controller | `module/lessonplan/controller/LessonPlanController.java` | 所有教案接口 |
| 后端 Service | `module/lessonplan/service/impl/LessonPlanServiceImpl.java` | 生成/重新生成/版本管理 |
| AI Service | `module/ai/service/LessonPlanAiService.java` | LangChain4j @SystemMessage 教案Prompt |
| AI Service | `module/ai/service/ImageAiService.java` | 关键词提取 + 图片生成Prompt |
| 前端页面 | `views/lessonplan/LessonPlanList.vue` | 教案列表 + AI生成对话框 + 模板弹窗 |
| 前端页面 | `views/lessonplan/LessonPlanDetail.vue` | 教案详情/编辑/Markdown渲染/关键词/版本 |
| 前端组件 | `components/MdRender.vue` | Markdown渲染(使用marked库, 支持表格) |
| API | 见wiki "教案 /api/lesson-plan" 节 | 10个接口 |

---

### M7 - 练习题管理模块 (含AI生成) ★核心

**功能**: AI按题型/难度生成结构化JSON习题, 手动编辑/删除

| 层次 | 文件 | 说明 |
|------|------|------|
| 数据库 | `exercise` 表 | 字段: question_content, options(JSON), answer, explanation, question_type, difficulty |
| 后端 Entity | `module/exercise/entity/Exercise.java` | 继承 BaseEntity |
| 后端 Controller | `module/exercise/controller/ExerciseController.java` | CRUD + generate |
| 后端 Service | `module/exercise/service/impl/ExerciseServiceImpl.java` | AI生成 + JSON解析 |
| AI Service | `module/ai/service/ExerciseAiService.java` | 习题生成Prompt |
| 前端页面 | `views/exercise/ExerciseList.vue` | 习题列表 + AI生成对话框 + 编辑对话框 |
| API | `GET /api/exercise/list`, `POST /api/exercise/generate`, `PUT /api/exercise/{id}`, `DELETE /api/exercise/{id}` | |

---

### M8 - 学情分析模块 (含AI报告) ★核心

**功能**: 班级统计(均分/中位数/及格率/分数段) + AI班级报告 + 学生个人分析 + AI个性化推荐

| 层次 | 文件 | 说明 |
|------|------|------|
| 后端 DTO | `module/analytics/dto/ClassAnalyticsDTO.java` | 班级统计数据结构 |
| 后端 DTO | `module/analytics/dto/StudentAnalyticsDTO.java` | 学生个人数据结构 |
| 后端 Controller | `module/analytics/controller/AnalyticsController.java` | 统计+AI报告+推荐 |
| 后端 Service | `module/analytics/service/impl/AnalyticsServiceImpl.java` | 统计计算 + AI调用 |
| AI Service | `module/ai/service/AnalyticsAiService.java` | 班级分析 + 学生分析 + 资源推荐Prompt |
| 前端页面 | `views/analytics/AnalyticsOverview.vue` | Tab(班级/学生) + 统计卡片 + AI报告 |
| API | `GET /api/analytics/class/{courseId}`, `GET /api/analytics/student/{studentId}`, `POST /api/analytics/ai-report/{courseId}`, `POST /api/analytics/ai-student-report/{studentId}`, `POST /api/analytics/recommend/{studentId}` | |

---

### M9 - 备课方案模块 (含AI整合) ★核心

**功能**: 整合教案+习题+学情, AI生成完整备课方案

| 层次 | 文件 | 说明 |
|------|------|------|
| 后端 Controller | `module/prepplan/controller/PrepPlanController.java` | 生成方案 |
| 后端 Service | `module/prepplan/service/impl/PrepPlanServiceImpl.java` | 聚合数据 + ChatModel直接调用 |
| 前端页面 | `views/prepplan/PrepPlanList.vue` | 选择课程+教案 → 生成方案 |
| API | `POST /api/prep-plan/generate`, `GET /api/prep-plan/list` | |

---

### M10 - 公共基础与部署

**功能**: 统一响应/异常处理/CORS/Docker配置/数据库初始化/日志

| 层次 | 文件 | 说明 |
|------|------|------|
| 公共类 | `common/Result.java`, `common/BaseEntity.java` | 统一响应, 基础实体 |
| 异常处理 | `common/exception/GlobalExceptionHandler.java` | 全局异常捕获 |
| 跨域配置 | `config/CorsConfig.java` | CORS |
| AI配置 | `config/LangChainConfig.java` | LangChain4j Bean注册, 超时300s |
| 数据库 | `docs/schema.sql` | 建表脚本 |
| 数据库 | `docs/seed-data.sql` | 种子数据 |
| 初始化 | `init/DataInitializer.java` | 启动时自动插入默认数据 |
| 日志 | `logback-spring.xml` | DEBUG级别, 输出到/app/logs |
| Docker | `docs/docker/docker-compose.yml` | 开发环境编排 |
| Docker | `docs/docker/Dockerfile.backend` | 后端镜像 |
| 前端路由 | `frontend/src/router/index.js` | Vue Router |
| 前端布局 | `frontend/src/layout/AppLayout.vue` | 侧边栏+头部 |
| 前端样式 | `frontend/src/style.css` | 粘土风格全局CSS |
| 前端API | `frontend/src/api/request.js` + `index.js` | Axios封装 |

---

## 建议分配方案（4人组）

| 成员 | 模块 | 说明 |
|------|------|------|
| 成员A | M1 + M10 | 基础框架搭建、认证、部署、配置 |
| 成员B | M2 + M3 + M4 + M5 | 基础数据CRUD（课程/学生/成绩/知识点） |
| 成员C | M6 + M7 | AI核心功能（教案生成 + 习题生成） |
| 成员D | M8 + M9 | AI分析功能（学情分析 + 备课方案） |

---

## 注意事项

1. **前后端字段名对照**: 后端使用驼峰(如 `questionContent`), 前端必须用相同驼峰名
2. **AI功能需要API Key**: 在 `.env` 文件配置 `AI_API_KEY`
3. **所有命令在容器执行**: `docker exec lesson-plan-backend xxx` / `docker exec lesson-plan-frontend xxx`
4. **修改后端代码需重新构建**: `cd docs/docker && docker compose up -d --build backend`
5. **前端代码支持热加载**: 修改即时生效, 无需重启
