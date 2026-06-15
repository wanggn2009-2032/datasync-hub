# DataSync Hub - 数据同步中台

基于 Java Spring Boot 和 Vue 3 的企业级数据同步平台，提供可视化 DAG 编辑、实时监控、多源数据同步等功能。

## 🌟 核心功能

- **DAG 流程编��器** - 拖拽式可视化任务流设计
- **实时监控仪表板** - 任务执行状态实时监控
- **多源数据同步** - 支持 MySQL、PostgreSQL、MongoDB、REST API 等
- **任务调度引擎** - Cron 表达式，灵活的调度策略
- **数据预览功能** - 实时预览同步数据
- **执行日志** - 详细的任务执行日志跟踪
- **告警通知** - 邮件、钉钉、企业微信等多渠道告警

## 🏗️ 技术栈

### 后端
- Java 11+
- Spring Boot 2.7.x
- Spring Data JPA
- MyBatis Plus
- MySQL 8.0
- Redis
- Quartz 任务调度

### 前端
- Vue 3
- Vite
- Element Plus
- Pinia (状态管理)
- G6 (DAG 编辑器)
- Echarts (数据可视化)

## 📦 项目结构

```
datasync-hub/
├── backend/                    # Java Spring Boot 后端
├── frontend/                   # Vue 3 前端
├── docker-compose.yml         # Docker 编排
└── README.md
```

## 🚀 快速开始

### 前置要求
- Java 11+
- Node.js 14+
- MySQL 8.0+
- Redis 6.0+

### 后端启动

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:8080`

### 前端启动

```bash
cd frontend
npm install
npm run dev
```

前端应用将运行在 `http://localhost:5173`

## 🐳 Docker 启动

```bash
docker-compose up -d
```

## 📖 API 文档

Swagger 文档：`http://localhost:8080/swagger-ui.html`

## 📝 许可证

MIT
