# 🚴 iCyclist - 骑行社交应用

一个功能完整的Android骑行记录与社交应用，采用前后端分离架构。

[![Android](https://img.shields.io/badge/Android-Kotlin-brightgreen)](https://kotlinlang.org/)
[![Server](https://img.shields.io/badge/Server-Spring%20Boot-blue)](https://spring.io/projects/spring-boot)
[![Database](https://img.shields.io/badge/Database-MySQL-orange)](https://www.mysql.com/)
[![Status](https://img.shields.io/badge/Status-Complete-success)](https://github.com)

## 📱 项目简介

iCyclist 是一个完整的骑行应用，集成了骑行记录、轨迹追踪、社交分享、社区论坛等功能。项目采用**Android + Spring Boot**的前后端分离架构，实现了完整的用户系统和数据同步。

### ✨ 核心功能

- 🚴 **骑行记录**：GPS实时追踪、轨迹绘制、数据统计
- 🗺️ **高德地图**：实时定位、轨迹显示、地图交互
- 📱 **骑友圈**：动态发布、评论点赞、社交互动
- 💬 **社区论坛**：话题讨论、主题分类、回复功能
- 👤 **用户系统**：注册登录、资料管理、JWT认证

## 🏗️ 技术架构

### Android客户端

- **语言**: Kotlin
- **UI框架**: Material Design
- **地图**: 高德地图SDK
- **网络**: Retrofit + OkHttp
- **数据库**: Room Database
- **异步**: Kotlin Coroutines
- **架构**: MVVM + Repository模式

### 后端服务器

- **框架**: Spring Boot 3.1.5
- **语言**: Kotlin
- **数据库**: MySQL 8.0
- **持久层**: MyBatis
- **认证**: JWT Token
- **安全**: Spring Security

## 📂 项目结构

```
iCyclist/
├── app/                          # Android应用
│   ├── src/main/
│   │   ├── java/com/example/icyclist/
│   │   │   ├── fragment/        # 主要Fragment
│   │   │   ├── community/       # 社区功能
│   │   │   ├── network/         # 网络层
│   │   │   ├── database/        # 本地数据库
│   │   │   ├── manager/         # 管理类
│   │   │   └── utils/           # 工具类
│   │   └── res/                 # 资源文件
│   └── build.gradle.kts
├── server/                       # Spring Boot后端
│   └── src/main/
│       ├── kotlin/com/icyclist/server/
│       │   ├── controller/      # 控制器
│       │   ├── service/         # 业务逻辑
│       │   ├── mapper/          # MyBatis接口
│       │   ├── model/           # 数据模型
│       │   ├── dto/             # 数据传输对象
│       │   ├── config/          # 配置类
│       │   └── util/            # 工具类
│       └── resources/
│           ├── mapper/          # MyBatis XML
│           └── application.properties
├── docs/                         # 文档
└── README.md
```

## 🚀 快速开始

### 前置要求

- Android Studio Arctic Fox 或更高版本
- JDK 17
- MySQL 8.0
- 高德地图API Key

### 1. 克隆项目

```bash
git clone https://github.com/your-username/iCyclist.git
cd iCyclist
```

### 2. 配置数据库

```sql
CREATE DATABASE icyclist CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库初始化脚本（如有）。

### 3. 配置服务器

编辑 `server/src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/icyclist
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 配置Android应用

1. 复制 `apikeys.properties.example` 为 `apikeys.properties`
2. 填入您的高德地图API Key

```properties
AMAP_API_KEY=your_amap_api_key_here
```

3. 修改 `app/src/main/java/com/example/icyclist/network/RetrofitClient.kt` 中的服务器地址：

```kotlin
private const val BASE_URL = "http://your-server-ip:8080/"
```

### 5. 启动服务器

```bash
./gradlew :server:bootRun
```

服务器将在 `http://localhost:8080` 启动。

### 6. 运行Android应用

1. 在Android Studio中打开项目
2. 连接Android设备或启动模拟器
3. 点击运行按钮

## 📱 应用截图

<details>
<summary>点击查看截图</summary>

<!-- 在这里添加应用截图 -->

</details>

## 🔌 API接口

### 用户认证

- `POST /api/users/register` - 用户注册
- `POST /api/users/login` - 用户登录
- `GET /api/users/profile/{userId}` - 获取用户资料
- `PUT /api/users/profile/{userId}` - 更新用户资料

### 骑行记录

- `POST /api/rides` - 创建骑行记录
- `GET /api/rides/user/{userId}` - 获取用户骑行记录
- `GET /api/rides/{id}` - 获取骑行记录详情

### 骑友圈

- `GET /api/posts` - 获取动态列表
- `POST /api/posts` - 发布动态
- `GET /api/posts/{id}` - 获取动态详情
- `POST /api/posts/{id}/comments` - 发表评论
- `POST /api/posts/{id}/like` - 点赞/取消点赞

### 社区论坛

- `GET /api/forum/categories` - 获取论坛分类
- `GET /api/forum/categories/{id}/topics` - 获取主题列表
- `GET /api/forum/topics/{id}` - 获取主题详情
- `POST /api/forum/topics` - 发布新主题
- `POST /api/forum/topics/{id}/replies` - 发布回复

详细API文档请参见 [API文档](docs/API.md)（如有）。

## 🎯 核心特性

### 架构设计

- **前后端分离**：Android客户端 + Spring Boot服务器
- **服务器优先**：优先从服务器获取数据
- **本地缓存降级**：网络失败时使用本地数据库
- **JWT认证**：安全的用户认证机制
- **RESTful API**：标准的API设计

### 数据流

```
Android App (本地Room数据库)
    ↓ 网络请求 (Retrofit + JWT)
Spring Boot Server
    ↓
MySQL数据库
```

- **读操作**：服务器优先 → 失败则降级到本地缓存
- **写操作**：提交服务器 → 成功后同步到本地缓存

## 📖 详细文档

- [项目需求完成度对照表](项目需求完成度对照表.md)
- [部署与使用说明](部署与使用说明.md)
- [快速开始测试](快速开始测试.md)
- [100%完成最终报告](100%完成-最终报告.md)
- [新增功能说明](新增功能说明.md)

## 🤝 贡献指南

欢迎提交Issue和Pull Request！

## 📄 许可证

[MIT License](LICENSE)

## 👨‍💻 作者

- 您的名字
- GitHub: [@your-username](https://github.com/your-username)

## 🙏 致谢

- 高德地图SDK
- Spring Boot
- Kotlin
- Material Design

## 📝 更新日志

### v1.0.0 (2025-10-17)

- ✅ 完整的用户认证系统
- ✅ GPS骑行记录和轨迹追踪
- ✅ 骑友圈社交功能
- ✅ 社区论坛系统
- ✅ 用户资料管理
- ✅ 前后端完全集成

---

**项目状态**: ✅ 已完成 | **完成度**: 100%
