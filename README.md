# Personal Planner

个人规划与每日复盘系统。

## 技术栈

- **后端**: Spring Boot 3.2.7 + Spring Security 6 + JWT + MyBatis-Plus 3.5.7 + MySQL 8.0
- **前端**: Vue 3.4 + TypeScript 5 + Vite 5 + Element Plus 2.7 + Pinia 2 + ECharts 5

## 快速启动

### 1. 启动数据库

```bash
docker-compose up -d
```

### 2. 后端

```bash
cd backend
./mvnw spring-boot:run
# 或用 IDE 启动 PlannerApplication
```

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 访问

- 前端: http://localhost:5173
- 后端: http://localhost:8080

### 5. 测试账号

- 用户名: `test`
- 密码: `test123`

## 项目结构

```
personal-planner/
├── backend/               # Spring Boot 后端
│   └── src/main/java/com/planner/
│       ├── common/       # Result/PageResult/BusinessException
│       ├── config/       # Security/MybatisPlus/Cors
│       ├── security/     # JWT/Filter
│       ├── entity/       # 12 张表对应实体
│       ├── mapper/       # MyBatis-Plus Mapper
│       ├── dto/          # 请求/响应 DTO
│       ├── service/      # 业务逻辑
│       └── controller/   # REST API
├── frontend/             # Vue 3 前端
│   └── src/
│       ├── api/          # Axios 封装 + 模块 API
│       ├── store/        # Pinia store
│       ├── router/       # Vue Router
│       ├── layouts/      # 布局组件
│       └── views/        # 页面
├── sql/
│   └── init.sql          # 数据库初始化 DDL
└── docker-compose.yml    # MySQL + Redis
```