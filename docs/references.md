# 技术参考文档

> 收集了本项目开发过程中使用的所有关键技术文档和链接，方便后续开发者快速上手。

## LangChain4j

- **官方文档**: https://docs.langchain4j.dev/
- **GitHub 仓库**: https://github.com/langchain4j/langchain4j
- **AI Services 教程**: https://docs.langchain4j.dev/tutorials/ai-services/
- **RAG 教程**: https://docs.langchain4j.dev/tutorials/rag/
- **Spring Boot 集成**: https://docs.langchain4j.dev/tutorials/spring-boot-integration/
- **DashScope 集成**: https://docs.langchain4j.dev/integrations/language-models/dashscope/
- **Spring Boot 4.0 兼容性问题**: https://github.com/langchain4j/langchain4j/issues/4268
  - 解决方案: 使用纯 Java 库(非 starter), 手动 `@Bean` 注册

### LangChain4j Maven 坐标

```xml
<!-- 核心库 (AiServices, RAG, ChatMemory) -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j</artifactId>
    <version>1.11.0</version>
</dependency>

<!-- DashScope/通义千问 (纯 Java 库, 非 starter) -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-community-dashscope</artifactId>
    <version>1.11.0-beta19</version>
</dependency>
```

### 关键 API 变更 (1.x vs 0.x)

- `ChatLanguageModel` → **`ChatModel`** (1.0+ 重命名)
- `AiServices.builder().chatLanguageModel()` → **`.chatModel()`**
- DashScope 从 `langchain4j-dashscope` 迁移到 `langchain4j-community-dashscope`

## DashScope (通义千问)

- **开放平台**: https://dashscope.aliyun.com/
- **API 文档**: https://help.aliyun.com/zh/model-studio/
- **模型列表**:
  - 聊天: `qwen-max` (最强), `qwen-plus` (均衡), `qwen-turbo` (快速)
  - 嵌入: `text-embedding-v3` (推荐, 1024维)
- **免费额度**: 新账号注册后有免费 tokens 可用
- **API Key 获取**: 登录 DashScope → API Key 管理 → 创建

## Spring Boot 4.0

- **官方文档**: https://docs.spring.io/spring-boot/
- **迁移指南 (3.x → 4.x)**: Jakarta EE 命名空间 (`javax.*` → `jakarta.*`)
- **Spring Framework 7**: 底层框架版本

## MyBatis-Plus

- **官方文档**: https://baomidou.com/
- **安装指南**: https://baomidou.com/getting-started/install/
- **Spring Boot 4 支持**: 使用 `mybatis-plus-spring-boot4-starter` (3.5.14+)

```xml
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot4-starter</artifactId>
    <version>3.5.14</version>
</dependency>
```

## Sa-Token

- **官方文档**: https://sa-token.cc/
- **GitHub**: https://github.com/dromara/sa-token
- **Spring Boot 4 支持**: `sa-token-spring-boot4-starter` (1.45.0+)
- **核心用法**:
  - `StpUtil.login(userId)` - 登录
  - `StpUtil.checkLogin()` - 检查登录
  - `StpUtil.getLoginIdAsLong()` - 获取当前用户ID
  - `StpUtil.getTokenValue()` - 获取 Token

## Vue 3 + Element Plus

- **Vue 3 文档**: https://vuejs.org/
- **Element Plus 文档**: https://element-plus.org/
- **Vue Router 4**: https://router.vuejs.org/
- **ECharts**: https://echarts.apache.org/
- **vue-echarts**: https://github.com/ecomfe/vue-echarts
- **markdown-it**: https://github.com/markdown-it/markdown-it (渲染 AI 生成的 Markdown)

## Docker

- **Docker 文档**: https://docs.docker.com/
- **Docker Compose**: https://docs.docker.com/compose/
- **MySQL 镜像**: https://hub.docker.com/_/mysql
- **Maven 镜像**: https://hub.docker.com/_/maven
- **Node 镜像**: https://hub.docker.com/_/node
- **Nginx 镜像**: https://hub.docker.com/_/nginx
