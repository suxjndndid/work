# 智能教案生成与学情分析系统

基于 Spring Boot 4 + LangChain4j + Vue 3 的智能教学辅助平台，采用 OpenAI 兼容协议，可接入任意大模型服务（OpenAI、中转站、通义千问、DeepSeek、Ollama 等），支持 AI 教案生成、习题生成、学情分析和 RAG 课标检索。

## 技术栈

| 层级 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 4.0.3 |
| AI 框架 | LangChain4j | 1.11.0 |
| AI 协议 | OpenAI 兼容接口 | 可接入任意服务 |
| ORM | MyBatis-Plus | 3.5.15 |
| 认证 | Sa-Token | 1.45.0 |
| 数据库 | MySQL | 8.0+ |
| 前端 | Vue 3 + Vite | 8.x |
| 容器化 | Docker Compose | - |

## 项目结构

```
work/
├── src/main/java/org/example/work/
│   ├── WorkApplication.java          # 启动入口
│   ├── config/
│   │   ├── LangChainConfig.java      # AI 模型配置（核心）
│   │   ├── CorsConfig.java           # 跨域配置
│   │   ├── MyBatisPlusConfig.java    # ORM 配置
│   │   └── SaTokenConfig.java        # 认证拦截配置
│   ├── common/
│   │   ├── Result.java               # 统一响应封装
│   │   └── BaseEntity.java           # 实体基类
│   ├── init/
│   │   └── DataInitializer.java      # 启动时自动初始化演示数据
│   └── module/
│       ├── auth/        # 登录认证
│       ├── user/        # 用户管理
│       ├── course/      # 课程管理
│       ├── lessonplan/  # 教案生成（AI）
│       ├── exercise/    # 习题生成（AI）
│       ├── analytics/   # 学情分析（AI）
│       ├── prepplan/    # 综合备课方案
│       └── ai/          # AI 服务层（LangChain4j）
├── src/main/resources/
│   ├── application.yml               # 主配置文件
│   └── rag-documents/                # RAG 课程标准文档
│       ├── math-curriculum-standard.txt
│       ├── chinese-curriculum-standard.txt
│       └── english-curriculum-standard.txt
├── frontend/                          # Vue 3 前端
├── docs/
│   ├── schema.sql                    # 数据库建表脚本
│   ├── wiki.md                       # 详细设计文档
│   └── docker/                       # Docker 部署文件
└── pom.xml
```

## 快速开始

### 前置条件

- JDK 17+
- Maven 3.9+
- MySQL 8.0+
- Node.js 20+（前端开发）
- **AI 模型 API Key**（必须，支持任意 OpenAI 兼容服务）

### 方式一：Docker 一键启动（推荐）

```bash
# 1. 设置环境变量
export AI_API_KEY=sk-your-actual-api-key
export AI_BASE_URL=https://api.openai.com/v1/      # 或中转站地址
export AI_CHAT_MODEL=gpt-4o-mini                    # 可选，默认 gpt-4o-mini
export AI_EMBEDDING_MODEL=text-embedding-3-small    # 可选

# 2. 启动所有服务
cd docs/docker
docker compose up -d --build

# 服务地址：
# - 前端: http://localhost:5173
# - 后端: http://localhost:8080
# - MySQL: localhost:3306
```

### 方式二：本地开发

```bash
# 1. 创建数据库
mysql -u root -p < docs/schema.sql

# 2. 设置环境变量
export AI_API_KEY=sk-your-actual-api-key
export AI_BASE_URL=https://api.openai.com/v1/      # 或中转站地址

# 3. 启动后端
mvn spring-boot:run

# 4. 启动前端（另开终端）
cd frontend
npm install
npm run dev
```

### 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | 123456 | 管理员 |
| teacher1 | 123456 | 教师（张老师） |
| teacher2 | 123456 | 教师（李老师） |

> 首次启动会自动初始化演示数据：3 门课程、30 个学生、120 条成绩记录、知识点掌握度数据。

---

## AI 模型配置详解

### 使用的接口类型

本项目使用 **OpenAI 兼容协议**（标准接口），可直接接入：

- **OpenAI 官方**（`https://api.openai.com/v1/`）
- **中转站/代理服务**（任意兼容 OpenAI 协议的地址）
- **通义千问 OpenAI 兼容模式**（`https://dashscope.aliyuncs.com/compatible-mode/v1/`）
- **DeepSeek**（`https://api.deepseek.com/v1/`）
- **Ollama 本地模型**（`http://localhost:11434/v1/`）
- **其他兼容服务**（硅基流动、零一万物、Groq 等）

### 在哪里配置

**配置文件**：[application.yml](src/main/resources/application.yml)

```yaml
ai:
  openai:
    base-url: ${AI_BASE_URL:https://api.openai.com/v1/}    # 服务地址
    api-key: ${AI_API_KEY:sk-your-api-key}                  # API Key
    chat-model-name: ${AI_CHAT_MODEL:gpt-4o-mini}           # 对话模型
    embedding-model-name: ${AI_EMBEDDING_MODEL:text-embedding-3-small}  # 向量模型
    temperature: 0.7
    max-tokens: 4096
```

所有参数都支持环境变量覆盖，优先级：环境变量 > yml 配置。

### 环境变量说明

| 环境变量 | 说明 | 示例 |
|----------|------|------|
| `AI_BASE_URL` | API 服务地址（必须以 `/` 结尾） | `https://api.openai.com/v1/` |
| `AI_API_KEY` | API 密钥 | `sk-xxxxxxxx` |
| `AI_CHAT_MODEL` | 对话模型名称 | `gpt-4o-mini` |
| `AI_EMBEDDING_MODEL` | 向量模型名称 | `text-embedding-3-small` |

### 常见服务商配置示例

**OpenAI 官方：**
```bash
export AI_BASE_URL=https://api.openai.com/v1/
export AI_API_KEY=sk-xxxxxxxx
export AI_CHAT_MODEL=gpt-4o-mini
export AI_EMBEDDING_MODEL=text-embedding-3-small
```

**中转站（以某中转为例）：**
```bash
export AI_BASE_URL=https://your-relay.com/v1/
export AI_API_KEY=sk-xxxxxxxx
export AI_CHAT_MODEL=gpt-4o-mini
export AI_EMBEDDING_MODEL=text-embedding-3-small
```

**通义千问（OpenAI 兼容模式）：**
```bash
export AI_BASE_URL=https://dashscope.aliyuncs.com/compatible-mode/v1/
export AI_API_KEY=sk-xxxxxxxx
export AI_CHAT_MODEL=qwen-max
export AI_EMBEDDING_MODEL=text-embedding-v3
```

**DeepSeek：**
```bash
export AI_BASE_URL=https://api.deepseek.com/v1/
export AI_API_KEY=sk-xxxxxxxx
export AI_CHAT_MODEL=deepseek-chat
export AI_EMBEDDING_MODEL=text-embedding-3-small  # DeepSeek 暂无 embedding，需用其他服务
```

**Ollama 本地模型（免费，无需 API Key）：**
```bash
export AI_BASE_URL=http://localhost:11434/v1/
export AI_API_KEY=ollama                          # 随意填写，Ollama 不验证
export AI_CHAT_MODEL=qwen2:7b
export AI_EMBEDDING_MODEL=nomic-embed-text
```

### 模型初始化代码

核心配置在 [LangChainConfig.java](src/main/java/org/example/work/config/LangChainConfig.java)：

```java
@Configuration
public class LangChainConfig {

    // 对话模型 - 用于生成教案、分析学情、生成习题
    @Bean
    public ChatModel chatModel() {
        return OpenAiChatModel.builder()
                .baseUrl(baseUrl)               // 可配置任意兼容服务地址
                .apiKey(apiKey)
                .modelName(chatModelName)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .build();
    }

    // 向量模型 - 用于 RAG 课标文档检索
    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(embeddingModelName)
                .build();
    }
}
```

> 只需修改 `base-url` 就能切换到不同的模型服务商，业务代码无需任何修改。

---

## 核心功能与 API

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /login | 登录，返回 Token |
| POST | /logout | 登出 |
| GET | /info | 获取当前用户信息 |

请求头携带 Token：`Authorization: <token>`

### 教案生成 `/api/lesson-plan`（AI）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /generate | AI 生成教案（含 RAG 课标检索） |
| POST | /{id}/regenerate | 重新生成 |
| POST | /{id}/keywords | 提取图片关键词 |
| GET | /templates | 获取互动环节模板 |
| GET | /page | 分页查询 |

### 习题生成 `/api/exercise`（AI）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /generate | AI 生成结构化习题（含选项、答案、解析） |
| GET | /page | 分页查询 |

### 学情分析 `/api/analytics`（AI）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /ai-report/{courseId} | 班级学情 AI 分析报告 |
| POST | /ai-student-report/{studentId} | 学生个人 AI 分析 |
| POST | /recommend/{studentId} | 个性化学习资源推荐 |

### 学生与成绩

| 路径前缀 | 说明 |
|----------|------|
| /api/student | 学生管理 CRUD |
| /api/score | 成绩管理 CRUD |
| /api/knowledge-point | 知识点管理 |
| /api/course | 课程管理 CRUD |

---

## RAG（检索增强生成）

系统内置课程标准文档用于教案生成时的知识检索：

```
src/main/resources/rag-documents/
├── math-curriculum-standard.txt       # 高中数学课程标准
├── chinese-curriculum-standard.txt    # 高中语文课程标准
└── english-curriculum-standard.txt    # 高中英语课程标准
```

- 启动时自动加载并向量化（text-embedding-v3）
- 存储在内存向量库中（InMemoryEmbeddingStore）
- 生成教案时自动检索相关课标内容（Top-5，相似度 ≥ 0.6）

如需添加自定义文档，将 `.txt` 文件放入 `rag-documents/` 目录后重启即可。

---

## 数据库

MySQL 8.0+，共 9 张表：

| 表名 | 说明 |
|------|------|
| sys_user | 用户（教师/管理员） |
| course | 课程 |
| lesson_plan | 教案 |
| lesson_plan_version | 教案版本历史 |
| student | 学生 |
| score | 成绩（测验/期中/期末） |
| knowledge_point | 知识点（树形结构） |
| knowledge_point_mastery | 知识点掌握度 |
| exercise | 习题 |

建表脚本：[docs/schema.sql](docs/schema.sql)

---

## 部署

### 生产环境 Docker 部署

```bash
export DASHSCOPE_API_KEY=sk-your-key
cd docs/docker
docker compose -f docker-compose.prod.yml up -d --build

# 访问 http://localhost（Nginx 反向代理）
```

生产模式下前端通过 Nginx 反向代理，`/api/*` 请求转发到后端。

---

## 常见问题

**Q: 后端启动报 `PropertyMapper.alwaysApplyingWhenNonNull()` 错误**
A: MyBatis-Plus 版本与 Spring Boot 4 不兼容，确保 `mybatis-plus.version` 为 `3.5.15`。

**Q: 前端容器启动失败，提示 Node.js 版本不够**
A: Vite 8 要求 Node.js 20.19+，docker-compose 中需使用 `node:20-alpine`。

**Q: AI 功能返回错误**
A: 检查 `AI_API_KEY` 和 `AI_BASE_URL` 环境变量是否正确设置，确认所选模型服务已开通且有余额。

**Q: 如何更换大模型服务商？**
A: 只需修改环境变量 `AI_BASE_URL` 和 `AI_API_KEY`，无需改代码。详见上方「常见服务商配置示例」。

**Q: 可以用中转站吗？**
A: 可以，只要中转站兼容 OpenAI 接口协议，设置 `AI_BASE_URL` 为中转站地址即可。
