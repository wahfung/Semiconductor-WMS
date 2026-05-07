# 半导体仓库管理系统 (Semiconductor WMS)

## 🛠 技术栈

- **Frontend**: React 18 + Ant Design 5 + Three.js (@react-three/fiber)
- **Backend**: Spring Boot 3.2 + Spring Security + JPA
- **Database**: MySQL 8.0

## 🚀 启动指南 (How to Run)

### 后端启动

1. 进入 backend 目录：`cd backend`
2. 使用 Maven 构建并运行：
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

### 前端启动

1. 进入 frontend 目录：`cd frontend`
2. 安装依赖并启动：
   ```bash
   npm install
   npm start
   ```

## 🔗 服务地址 (Services)

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Database**: localhost:3306 (user: root / pass: wms123456)

## 🧪 测试账号

| 角色   | 用户名   | 密码   |
| ------ | -------- | ------ |
| 管理员 | admin    | 123456 |
| 操作员 | operator | 123456 |

## 📦 功能模块

### 基础数据管理

- **物料管理**: 管理原材料、半制品、产成品的基础信息
- **仓库管理**: 管理仓库基本信息和容量配置
- **货架管理**: 管理仓库货架及货位配置

### 3D仓库可视化

- **3D仓库视图**: 实时查看仓库货架和库存分布
  - 支持鼠标旋转、平移、缩放
  - 悬停显示货位详细信息
  - 不同颜色区分物料类型

### 库存管理

- **库存查询**: 查询仓库库存信息和分布情况
- **入库管理**: 管理物料入库操作（采购入库、退货入库等）
- **出库管理**: 管理物料出库操作（销售出库、生产领料等）
- **盘点管理**: 管理仓库库存盘点

### 采购管理

- **采购管理**: 管理物料采购订单，支持审批流程

## 🎨 UI特色

- 现代渐变色主题设计
- 卡片式布局，层次分明
- 3D货架实时展示
- 响应式设计，支持多终端

## 📁 项目结构

```
583/
├── backend/                 # Spring Boot 后端
│   └── src/main/java/com/wms/
│       ├── controller/     # API控制器
│       ├── service/        # 业务服务
│       ├── repository/     # 数据仓库
│       ├── entity/         # 实体类
│       ├── dto/            # 数据传输对象
│       ├── config/         # 配置类
│       └── security/       # 安全配置
└── frontend/                # React 前端
    ├── src/
    │   ├── components/     # 公共组件
    │   ├── pages/          # 页面组件
    │   ├── services/       # API服务
    │   ├── utils/          # 工具函数
    │   └── styles/         # 样式文件
    └── nginx.conf
```

## 🗄️ 数据库设计

系统包含以下核心数据表：

- `users` - 用户表
- `materials` - 物料表
- `warehouses` - 仓库表
- `shelves` - 货架表
- `shelf_slots` - 货位表
- `inventories` - 库存表
- `purchase_orders` - 采购单表
- `inbound_orders` - 入库单表
- `outbound_orders` - 出库单表
- `stocktaking_orders` - 盘点单表

## 📝 初始数据

系统启动时自动初始化：

- 2个测试用户
- 19种半导体物料（原材料8种、半制品5种、产成品6种）
- 3个仓库（原材料仓库、半制品仓库、成品仓库）
- 每个仓库4个货架
- 预置库存数据
