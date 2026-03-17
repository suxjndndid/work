# 智能教案生成与学情分析系统 - 项目 Wiki

> 最后更新: 2026-03-17 | 维护者: 项目组

## 目录

- [项目概述](#项目概述)
- [技术架构](#技术架构)
- [核心功能清单](#核心功能清单)
- [目录结构](#目录结构)
- [数据库设计](#数据库设计)
- [API 接口文档](#api-接口文档)
- [LangChain4j 集成说明](#langchain4j-集成说明)
- [Docker 容器化部署](#docker-容器化部署)
- [开发指南](#开发指南)
- [参考文档与链接](#参考文档与链接)

---

## 项目概述

面向教师的 AI 备课助手，覆盖**教案设计 → 多媒体资源生成 → 练习题生成 → 学情分析 → 个性化推荐**全流程。

**核心目标：**
1. 10 分钟内生成包含 3 个互动环节的完整教案
2. 学情分析准确率不低于 85%
3. 备课效率提升 60% 以上

---

## 技术架构

```
┌─────────────────────────────────────────────────┐
│                  Vue 3 前端                       │
│        Element Plus + ECharts + Axios            │
│              (Vite 开发服务器)                     │
└──────────────────────┬──────────────────────────┘
                       │ HTTP REST API
┌──────────────────────▼──────────────────────────┐
│               Spring Boot 4.0.3 后端              │
│                                                   │
│  ┌─────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │ Auth    │ │ Course    │ │ LessonPlan       │  │
│  │ 认证模块 │ │ 课程模块   │ │ 教案模块(AI生成)  │  │
│  └─────────┘ └───────────┘ └──────────────────┘  │
│  ┌─────────┐ ┌───────────┐ ┌──────────────────┐  │
│  │Analytics│ │ Exercise  │ │ PrepPlan         │  │
│  │学情分析  │ │ 习题模块   │ │ 备课方案模块      │  │
│  └─────────┘ └───────────┘ └──────────────────┘  │
│                                                   │
│  ┌────────────────────────────────────────────┐   │
│  │         AI 模块 (LangChain4j)              │   │
│  │  AiServices + RAG + Prompt Engineering     │   │
│  └──────────────────┬─────────────────────────┘   │
│                     │                             │
│  ┌──────────────────▼─────────────────────────┐   │
│  │    DashScope API (通义千问 qwen-max)        │   │
│  │    Embedding: text-embedding-v3             │   │
│  └────────────────────────────────────────────┘   │
│                                                   │
│  ┌───────────┐ ┌───────────┐ ┌───────────────┐   │
│  │MyBatis-Plus│ │ Sa-Token │ │InMemoryVector │   │
│  │  ORM      │ │  认证     │ │  向量存储      │   │
│  └─────┬─────┘ └──────────┘ └───────────────┘   │
└────────┼────────────────────────────────────────┘
         │
┌────────▼────────┐
│     MySQL 8     │
│  lesson_plan_db │
└─────────────────┘
```

### 技术栈明细

| 层次 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 后端框架 | Spring Boot | 4.0.3 | Java 17 |
| AI 框架 | LangChain4j | 1.11.0 | 纯库模式(非starter) |
| LLM | 通义千问 | qwen-max | DashScope API |
| 向量嵌入 | text-embedding-v3 | - | DashScope 提供 |
| ORM | MyBatis-Plus | 3.5.14 | spring-boot4-starter |
| 认证 | Sa-Token | 1.45.0 | spring-boot4-starter |
| 数据库 | MySQL | 8.0+ | |
| 前端框架 | Vue 3 | 3.4+ | Vite 构建 |
| UI 组件 | Element Plus | 2.9+ | |
| 图表 | ECharts | 5.5+ | vue-echarts 封装 |
| 容器化 | Docker Compose | - | 开发+生产两套编排 |

### 关键设计决策

**为什么 LangChain4j 用纯库而不是 Spring Boot Starter？**

LangChain4j 的 `spring-boot-starter` 与 Spring Boot 4.0 不兼容 ([GitHub Issue #4268](https://github.com/langchain4j/langchain4j/issues/4268))。
解决方案：使用纯 Java 库 `langchain4j` + `langchain4j-community-dashscope`，在 `LangChainConfig.java` 中手动通过 `@Configuration` + `@Bean` 注册所有组件。

---

## 核心功能清单

| # | 功能 | 说明 | 对应 API |
|---|------|------|----------|
| 1 | AI 生成完整教案 | 输入课程主题，生成含 ≥3 个互动环节的教案 | `POST /api/lesson-plan/generate` |
| 2 | 互动环节模板插入 | 提供小组讨论/角色扮演/案例分析等模板库 | `GET /api/lesson-plan/templates` |
| 3 | AI 生成图片关键词 | 从教案内容提取图片/视频制作关键词 | `POST /api/lesson-plan/{id}/keywords` |
| 4 | AI 调用绘图 API | 根据关键词生成教学示意图/流程图 | `POST /api/lesson-plan/{id}/generate-image` |
| 5 | AI 生成结构化习题 | 按题型/难度生成 JSON 格式题目+答案 | `POST /api/exercise/generate` |
| 6 | 手动管理教案与习题 | CRUD 操作 | `/api/lesson-plan/*`, `/api/exercise/*` |
| 7 | 班级学情数据录入 | 成绩、答题时长等数据管理 | `/api/score/*`, `/api/student/*` |
| 8 | AI 分析班级学情 | 输出班级均分、知识点掌握率等 | `POST /api/analytics/ai-report/{courseId}` |
| 9 | 个性化资源推荐 | 根据学情推荐预习/复习资料 | `POST /api/analytics/recommend/{studentId}` |
| 10 | 生成总体备课方案 | 整合教案+资源+习题+推荐 | `POST /api/prep-plan/generate` |

---

## 目录结构

```
work/
├── docs/
│   ├── wiki.md                 ← 本文件
│   ├── schema.sql              ← 数据库建表脚本
│   ├── 项目最新文档.txt          ← 原始需求文档
│   ├── references.md           ← 技术参考文档
│   └── docker/
│       ├── Dockerfile.backend  ← 后端镜像
│       ├── Dockerfile.frontend ← 前端镜像
│       ├── docker-compose.yml  ← 开发编排
│       ├── docker-compose.prod.yml ← 生产编排
│       └── nginx.conf          ← 前端 Nginx 配置
├── frontend/                    ← Vue 3 前端项目
├── src/main/java/org/example/work/
│   ├── WorkApplication.java
│   ├── config/
│   │   ├── LangChainConfig.java      ← 核心: LangChain4j Bean 注册
│   │   ├── MyBatisPlusConfig.java
│   │   ├── SaTokenConfig.java
│   │   └── CorsConfig.java
│   ├── common/
│   │   ├── Result.java                ← 统一响应
│   │   ├── PageResult.java
│   │   ├── BaseEntity.java
│   │   └── exception/
│   ├── module/
│   │   ├── auth/                      ← 登录认证
│   │   ├── user/                      ← 用户管理
│   │   ├── course/                    ← 课程管理
│   │   ├── lessonplan/                ← 教案管理 + AI 生成
│   │   ├── exercise/                  ← 练习题 + AI 生成
│   │   ├── analytics/                 ← 学情分析 + AI 报告
│   │   ├── prepplan/                  ← 总体备课方案
│   │   └── ai/
│   │       ├── service/
│   │       │   ├── LessonPlanAiService.java
│   │       │   ├── ExerciseAiService.java
│   │       │   ├── AnalyticsAiService.java
│   │       │   ├── ImageAiService.java
│   │       │   └── RagService.java
│   │       └── prompt/
│   └── init/
│       └── DataInitializer.java       ← 模拟数据
├── src/main/resources/
│   ├── application.yml
│   ├── rag-documents/                 ← RAG 课标文档
│   └── mapper/
└── pom.xml
```

---

## 数据库设计

详见 [schema.sql](schema.sql)

### ER 关系图 (文本)

```
sys_user 1──N course
sys_user 1──N lesson_plan
course   1──N lesson_plan
course   1──N knowledge_point
course   1──N score
course   1──N exercise

lesson_plan 1──N lesson_plan_version
lesson_plan 1──N exercise

student 1──N score
student 1──N knowledge_point_mastery

knowledge_point ──self (parent_id 树形)
knowledge_point 1──N knowledge_point_mastery
```

### 表清单

| 表名 | 说明 |
|------|------|
| sys_user | 系统用户(教师/管理员) |
| course | 课程 |
| lesson_plan | 教案(含 AI 生成内容) |
| lesson_plan_version | 教案版本历史 |
| student | 学生 |
| knowledge_point | 知识点(树形) |
| score | 成绩记录 |
| knowledge_point_mastery | 知识点掌握度 |
| exercise | 练习题(AI 生成) |

---

## API 接口文档

### 认证 `/api/auth`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/api/auth/login` | 登录 | 否 |
| POST | `/api/auth/logout` | 登出 | 是 |
| GET | `/api/auth/info` | 当前用户信息 | 是 |

**登录请求体:** `{ "username": "teacher1", "password": "123456" }`
**登录响应:** `{ "code": 200, "data": { "token": "xxx", "userId": 1, "username": "teacher1", "realName": "张老师", "role": 1 } }`

### 课程 `/api/course`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/course/list?current=1&size=10&keyword=` | 分页查询 |
| GET | `/api/course/{id}` | 详情 |
| POST | `/api/course` | 创建 |
| PUT | `/api/course/{id}` | 更新 |
| DELETE | `/api/course/{id}` | 删除 |

### 教案 `/api/lesson-plan`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/lesson-plan/list` | 分页查询 |
| GET | `/api/lesson-plan/{id}` | 详情 |
| POST | `/api/lesson-plan/generate` | **AI 生成教案** |
| POST | `/api/lesson-plan/{id}/regenerate` | 重新生成 |
| PUT | `/api/lesson-plan/{id}` | 编辑保存 |
| DELETE | `/api/lesson-plan/{id}` | 删除 |
| GET | `/api/lesson-plan/{id}/versions` | 版本历史 |
| GET | `/api/lesson-plan/templates` | 互动环节模板列表 |
| POST | `/api/lesson-plan/{id}/keywords` | AI 提取图片关键词 |
| POST | `/api/lesson-plan/{id}/generate-image` | AI 生成教学图片 |

**生成教案请求体:**
```json
{
  "courseId": 1,
  "subject": "数学",
  "grade": "高一",
  "topic": "集合的概念与表示",
  "objectives": "理解集合的含义，掌握集合的表示方法",
  "duration": 45
}
```

### 练习题 `/api/exercise`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/exercise/list` | 查询(按教案/课程筛选) |
| POST | `/api/exercise/generate` | **AI 生成习题** |
| PUT | `/api/exercise/{id}` | 编辑 |
| DELETE | `/api/exercise/{id}` | 删除 |

**生成习题请求体:**
```json
{
  "lessonPlanId": 1,
  "courseId": 1,
  "topic": "集合的概念",
  "questionType": "选择题",
  "difficulty": "中等",
  "count": 5
}
```

### 学情分析 `/api/analytics`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/analytics/class/{courseId}` | 班级统计数据 |
| GET | `/api/analytics/student/{studentId}` | 学生个人数据 |
| POST | `/api/analytics/ai-report/{courseId}` | **AI 班级分析报告** |
| POST | `/api/analytics/ai-student-report/{studentId}` | **AI 学生分析报告** |
| POST | `/api/analytics/recommend/{studentId}` | **AI 个性化资源推荐** |

### 备课方案 `/api/prep-plan`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/prep-plan/generate` | **生成总体备课方案** |
| GET | `/api/prep-plan/list` | 查询备课方案 |
| GET | `/api/prep-plan/{id}` | 方案详情 |

### 基础数据 `/api/student`, `/api/score`, `/api/knowledge-point`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/POST/PUT/DELETE | `/api/student/*` | 学生 CRUD |
| GET/POST/DELETE | `/api/score/*` | 成绩 CRUD + 批量导入 |
| GET/POST/PUT/DELETE | `/api/knowledge-point/*` | 知识点 CRUD |

---

## LangChain4j 集成说明

### AI Service 接口列表

| 接口 | 用途 | RAG |
|------|------|-----|
| `LessonPlanAiService` | 教案生成(含≥3个互动环节) | 是(课标检索) |
| `ExerciseAiService` | 练习题生成(结构化JSON) | 否 |
| `AnalyticsAiService` | 学情分析(班级+个人报告) | 否 |
| `ImageAiService` | 图片关键词提取 + 个性化资源推荐 | 否 |

### RAG 工作流

1. 启动时 `RagService.ingestDocuments()` 加载 `resources/rag-documents/*.txt`
2. 文档分割: `DocumentSplitters.recursive(300, 30)`
3. 向量化: `QwenEmbeddingModel` (text-embedding-v3)
4. 存储: `InMemoryEmbeddingStore`
5. 检索: `EmbeddingStoreContentRetriever` (top-5, minScore=0.6)

### DashScope API Key 配置

1. 注册阿里云账号 https://dashscope.aliyun.com/
2. 完成实名认证
3. 创建 API Key
4. 设置环境变量: `DASHSCOPE_API_KEY=sk-xxx`
5. 新账号有免费额度(约 100 万 tokens)

---

## Docker 容器化部署

详见 [docs/docker/](docker/) 目录。

### 快速启动(开发模式)

```bash
cd docs/docker
docker compose up -d
```

服务清单:
- MySQL: `localhost:3306`
- 后端 API: `localhost:8080`
- 前端: `localhost:5173`

### 生产部署

```bash
cd docs/docker
docker compose -f docker-compose.prod.yml up -d
```

- 前端通过 Nginx 代理, 端口 80
- 后端 API 端口 8080

---

## 开发指南

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| teacher1 | 123456 | 教师(张老师) |
| teacher2 | 123456 | 教师(李老师) |

### 环境要求

- Java 17+
- Node.js 18+
- MySQL 8.0+
- Docker & Docker Compose (容器化开发)

### 本地开发(不推荐，建议用 Docker)

```bash
# 1. 建库
mysql -u root -p < docs/schema.sql

# 2. 启动后端
export DASHSCOPE_API_KEY=sk-xxx
./mvnw spring-boot:run

# 3. 启动前端
cd frontend
npm install
npm run dev
```

---

## 参考文档与链接

详见 [references.md](references.md)
