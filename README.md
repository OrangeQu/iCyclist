# iCyclist - 智能骑行助手

<div align="center">
  <img src="docs/screenshots/main.jpg" width="200" alt="主界面"/>
  <img src="docs/screenshots/sport.jpg" width="200" alt="运动界面"/>
  <img src="docs/screenshots/community.jpg" width="200" alt="社区界面"/>
  <img src="docs/screenshots/profile.jpg" width="200" alt="个人界面"/>
</div>

## 📖 项目简介

**iCyclist** 是一款专为骑行爱好者打造的智能骑行助手应用，集成了运动追踪、社交分享、论坛交流等功能。该应用基于Android原生开发，采用本地数据库存储，无需服务器即可完整运行。

### 核心特性

- 🚴 **实时运动追踪**：利用高德地图SDK实现GPS轨迹记录、速度监测、距离统计
- 📊 **数据可视化**：运动轨迹地图展示、历史记录查询、运动数据统计
- 🌐 **骑友圈**：发布骑行动态、图片分享、点赞评论互动
- 💬 **社区论坛**：多分类讨论板块、主题发布、回复交流
- 👤 **个人中心**：用户资料管理、头像更换、运动历史记录

---

## 🛠 技术栈

### 开发语言与框架
- **Kotlin** - 现代化的Android开发语言
- **Android SDK** - 最低支持API 26 (Android 8.0)
- **Material Design 3** - 现代化UI设计规范

### 核心技术
- **Room Database** - 本地SQLite数据持久化
- **Kotlin Coroutines** - 异步任务处理
- **View Binding & Data Binding** - 视图绑定技术
- **Lifecycle Components** - Android生命周期感知组件

### 第三方SDK与库

#### 地图与定位
- **高德地图 3D SDK** - 地图显示与轨迹绘制
- **高德定位SDK** - GPS定位与运动追踪

#### 网络与数据
- **Retrofit 2** - RESTful API调用（预留）
- **OkHttp 3** - HTTP客户端
- **Gson** - JSON序列化/反序列化

#### 图像处理
- **Glide** - 图片加载与缓存
- **CircleImageView** - 圆形头像视图

#### 安全
- **AndroidX Security** - 加密的SharedPreferences

---

## 📁 项目架构

```
iCyclist/
├── app/                                    # 主应用模块
│   ├── src/main/
│   │   ├── java/com/example/icyclist/
│   │   │   ├── activity/                   # Activity页面
│   │   │   │   ├── LoginActivity          # 登录页面
│   │   │   │   ├── RegisterActivity       # 注册页面
│   │   │   │   ├── MainContainerActivity  # 主容器（Fragment导航）
│   │   │   │   ├── EditProfileActivity    # 编辑资料
│   │   │   │   ├── SportTrackingActivity  # 运动追踪
│   │   │   │   ├── SportHistoryActivity   # 运动历史
│   │   │   │   ├── TrackDetailActivity    # 轨迹详情
│   │   │   │   └── PostDetailActivity     # 帖子详情
│   │   │   │
│   │   │   ├── fragment/                   # Fragment碎片
│   │   │   │   ├── SportFragment          # 运动模块
│   │   │   │   ├── MomentFragment         # 骑友圈
│   │   │   │   ├── CommunityFragment      # 社区论坛
│   │   │   │   └── ProfileFragment        # 个人中心
│   │   │   │
│   │   │   ├── community/                  # 社区相关Activity
│   │   │   │   ├── CreatePostActivity     # 创建帖子
│   │   │   │   ├── CreateTopicActivity    # 创建主题
│   │   │   │   ├── TopicListActivity      # 主题列表
│   │   │   │   └── TopicDetailActivity    # 主题详情
│   │   │   │
│   │   │   ├── database/                   # 数据库层
│   │   │   │   ├── SportDatabase          # Room数据库
│   │   │   │   ├── entities/              # 数据实体
│   │   │   │   │   ├── SportRecordEntity  # 运动记录
│   │   │   │   │   ├── CommunityPostEntity # 社区帖子
│   │   │   │   │   ├── CommentEntity      # 评论
│   │   │   │   │   ├── LikeEntity         # 点赞
│   │   │   │   │   ├── ForumCategoryEntity # 论坛分类
│   │   │   │   │   ├── ForumTopicEntity   # 论坛主题
│   │   │   │   │   └── ForumReplyEntity   # 论坛回复
│   │   │   │   └── dao/                   # 数据访问对象
│   │   │   │       ├── SportRecordDao
│   │   │   │       ├── CommunityPostDao
│   │   │   │       ├── CommentDao
│   │   │   │       ├── LikeDao
│   │   │   │       ├── ForumCategoryDao
│   │   │   │       ├── ForumTopicDao
│   │   │   │       └── ForumReplyDao
│   │   │   │
│   │   │   ├── adapter/                    # RecyclerView适配器
│   │   │   │   ├── SportRecordAdapter     # 运动记录列表
│   │   │   │   ├── CommunityPostAdapter   # 社区帖子列表
│   │   │   │   ├── CommentAdapter         # 评论列表
│   │   │   │   ├── ForumCategoryAdapter   # 论坛分类列表
│   │   │   │   ├── TopicAdapter           # 主题列表
│   │   │   │   └── ReplyAdapter           # 回复列表
│   │   │   │
│   │   │   ├── manager/                    # 管理器
│   │   │   │   └── UserManager            # 用户会话管理
│   │   │   │
│   │   │   ├── network/                    # 网络层（预留）
│   │   │   │   ├── ApiService             # API接口定义
│   │   │   │   ├── RetrofitClient         # Retrofit客户端
│   │   │   │   └── model/                 # 网络数据模型
│   │   │   │
│   │   │   └── utils/                      # 工具类
│   │   │       ├── SportRecord            # 运动记录工具
│   │   │       └── TrackThumbnailGenerator # 轨迹缩略图生成
│   │   │
│   │   ├── res/                            # 资源文件
│   │   │   ├── layout/                    # 布局文件
│   │   │   ├── drawable/                  # 图片资源
│   │   │   ├── values/                    # 值资源
│   │   │   └── xml/                       # XML配置
│   │   │
│   │   └── AndroidManifest.xml            # 应用清单
│   │
│   └── build.gradle.kts                    # 应用构建脚本
│
├── server/                                 # Spring Boot后端（可选）
├── docs/                                   # 文档与截图
├── gradle/                                 # Gradle配置
├── apikeys.properties.example              # API密钥配置示例
├── build.gradle.kts                        # 项目构建脚本
└── settings.gradle.kts                     # 项目设置
```

---

## 🗄 数据库设计

应用采用 **Room Persistence Library** 进行本地数据存储，数据库版本：**v5**

### 核心数据表

#### 1. `sport_records` - 运动记录表
```kotlin
@Entity(tableName = "sport_records")
data class SportRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startTime: Long,              // 开始时间戳
    val endTime: Long,                // 结束时间戳
    val duration: Long,               // 运动时长（秒）
    val distance: Float,              // 运动距离（米）
    val averageSpeed: Float,          // 平均速度（km/h）
    val maxSpeed: Float,              // 最大速度（km/h）
    val calories: Int,                // 消耗卡路里
    val trackPoints: String,          // 轨迹坐标点（JSON）
    val thumbnailPath: String?        // 轨迹缩略图路径
)
```

#### 2. `community_posts` - 社区帖子表
```kotlin
@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userAvatar: String,           // 用户头像
    val userNickname: String,         // 用户昵称
    val content: String,              // 帖子内容
    val imageUrl: String?,            // 图片路径
    val timestamp: Long,              // 发布时间戳
    val sportRecordId: Int?,          // 关联的运动记录ID
    val sportDistance: String?,       // 运动距离展示
    val sportDuration: String?,       // 运动时长展示
    val sportThumbPath: String?       // 运动轨迹缩略图
)
```

#### 3. `comments` - 评论表
```kotlin
@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,                  // 关联的帖子ID
    val userId: String,               // 评论用户ID
    val userNickname: String,         // 评论用户昵称
    val userAvatar: String,           // 评论用户头像
    val content: String,              // 评论内容
    val timestamp: Long               // 评论时间戳
)
```

#### 4. `likes` - 点赞表
```kotlin
@Entity(tableName = "likes", primaryKeys = ["postId", "userId"])
data class LikeEntity(
    val postId: Int,                  // 关联的帖子ID
    val userId: String,               // 点赞用户ID
    val timestamp: Long               // 点赞时间戳
)
```

#### 5. `forum_categories` - 论坛分类表
```kotlin
@Entity(tableName = "forum_categories")
data class ForumCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,                 // 分类名称
    val description: String,          // 分类描述
    val topicCount: Int = 0           // 主题数量
)
```

#### 6. `forum_topics` - 论坛主题表
```kotlin
@Entity(tableName = "forum_topics")
data class ForumTopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryId: Int,              // 所属分类ID
    val userId: String,               // 作者ID
    val userNickname: String,         // 作者昵称
    val userAvatar: String,           // 作者头像
    val title: String,                // 主题标题
    val content: String,              // 主题内容
    val timestamp: Long,              // 发布时间戳
    val replyCount: Int = 0           // 回复数量
)
```

#### 7. `forum_replies` - 论坛回复表
```kotlin
@Entity(tableName = "forum_replies")
data class ForumReplyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicId: Int,                 // 关联的主题ID
    val userId: String,               // 回复用户ID
    val userNickname: String,         // 回复用户昵称
    val userAvatar: String,           // 回复用户头像
    val content: String,              // 回复内容
    val timestamp: Long               // 回复时间戳
)
```

### 数据库迁移

项目包含完整的数据库迁移策略（MIGRATION_1_2 至 MIGRATION_4_5），确保版本升级时数据不丢失：

- **v1→v2**: 创建社区帖子表
- **v2→v3**: 创建评论、点赞、论坛相关表，插入初始分类数据
- **v3→v4**: 清理重复的论坛分类数据
- **v4→v5**: 插入示例论坛数据（主题、回复）

---

## 🚀 功能模块详解

### 1. 用户认证模块

#### 登录功能 (`LoginActivity`)
- 邮箱 + 密码登录
- 输入验证（邮箱格式、密码长度）
- 使用 `EncryptedSharedPreferences` 加密存储用户凭证
- 记住登录状态，自动登录

#### 注册功能 (`RegisterActivity`)
- 邮箱、昵称、密码注册
- 密码确认验证
- 邮箱格式验证
- 昵称长度限制（2-20字符）

#### 用户管理 (`UserManager`)
- 加密存储用户信息
- 获取当前用户邮箱、昵称、头像
- 更新用户资料
- 登出功能

---

### 2. 运动追踪模块

#### 实时追踪 (`SportFragment` + `SportTrackingActivity`)

**功能特性：**
- GPS实时定位（高德地图SDK）
- 运动轨迹绘制
- 实时数据监测：
  - 运动时长（HH:MM:SS）
  - 运动距离（公里）
  - 当前速度（km/h）
  - 平均速度（km/h）
  - 消耗卡路里（自动计算）
- 地图模式切换（标准/卫星/夜间）
- 地图缩放、旋转、倾斜控制
- 定位按钮（一键回到当前位置）

**运动状态管理：**
```kotlin
enum class SportState {
    IDLE,      // 未开始
    TRACKING,  // 追踪中
    PAUSED,    // 已暂停
    ENDED      // 已结束
}
```

**速度计算算法：**
```kotlin
// 基于两个GPS坐标点计算速度
speed = distance / time  // m/s
speedKmh = speed * 3.6   // 转换为 km/h
```

**卡路里计算：**
```kotlin
// 基于距离和平均速度的简化公式
calories = (distance_km * 50).toInt()
```

#### 轨迹保存与缩略图生成
- 轨迹点坐标序列化为JSON存储
- 自动生成轨迹缩略图（MapView截图）
- 保存到本地数据库

#### 运动历史 (`SportHistoryActivity`)
- 显示所有历史运动记录
- 按时间倒序排列
- 显示运动日期、距离、时长、速度
- 点击查看详情
- 长按删除记录

#### 轨迹详情 (`TrackDetailActivity`)
- 在地图上重现历史轨迹
- 显示详细运动数据
- 轨迹回放功能
- 分享功能（集成到骑友圈）

---

### 3. 骑友圈模块

#### 动态列表 (`MomentFragment`)

**显示内容：**
- 用户头像、昵称
- 文字内容
- 图片（可选）
- 运动数据卡片（关联运动记录时）
- 点赞数、评论数
- 发布时间

**交互功能：**
- 点赞/取消点赞（实时更新UI）
- 查看评论列表
- 发表评论
- 数据持久化到本地数据库

#### 发布帖子 (`CreatePostActivity`)

**支持内容：**
- 纯文字动态
- 图文动态
- 运动记录分享

**图片处理：**
- 使用 **Glide** 加载和缓存
- 后台线程保存图片到内部存储
- 防止OOM（内存溢出）优化

**发布流程：**
```kotlin
1. 输入内容
2. 选择图片（可选）
3. 关联运动记录（可选）
4. 保存到本地数据库
5. 返回动态列表，自动刷新
```

#### 帖子详情 (`PostDetailActivity`)
- 查看完整帖子内容
- 显示所有评论
- 评论输入框
- 实时更新评论列表

---

### 4. 社区论坛模块

#### 论坛分类 (`CommunityFragment`)

**预设分类：**
1. **装备讨论** - 分享和讨论骑行装备
2. **路线分享** - 推荐你最喜欢的骑行路线
3. **新手问答** - 新手上路？在这里提问吧
4. **骑闻轶事** - 分享骑行中的趣闻和故事
5. **二手交易** - 买卖你的闲置骑行装备

**实时统计：**
- 动态计算每个分类的主题数量
- 使用 SQL COUNT 查询实现

#### 主题列表 (`TopicListActivity`)
- 按分类显示主题
- 显示主题标题、作者、回复数
- 按时间倒序排列
- FloatingActionButton 创建新主题

#### 创建主题 (`CreateTopicActivity`)
- 输入标题（限100字）
- 输入内容（限500字）
- 实时字数统计
- 保存到数据库，更新分类计数

#### 主题详情 (`TopicDetailActivity`)
- 显示主题完整内容
- 显示所有回复
- 回复输入框
- 实时更新回复列表和回复计数

---

### 5. 个人中心模块

#### 个人资料 (`ProfileFragment`)

**显示内容：**
- 用户头像（圆形显示）
- 用户昵称
- 用户邮箱
- 运动统计数据：
  - 总运动次数
  - 总运动距离
  - 总运动时长
  - 总消耗卡路里

**功能入口：**
- 编辑资料
- 运动历史
- 设置（预留）
- 退出登录

#### 编辑资料 (`EditProfileActivity`)

**可编辑项：**
- 头像（支持从相册选择）
- 昵称

**头像处理：**
- 支持自定义照片
- 圆形裁剪显示
- 使用 Glide 加载，防止OOM
- 区分默认头像和自定义头像的显示方式：
  - 默认头像：有背景色和padding
  - 自定义头像：无padding，完全填充圆形区域

**保存机制：**
- 图片保存到应用内部存储（`/data/data/com.example.icyclist/files/avatars/`）
- 路径存储到 `SharedPreferences`
- 更新成功后返回个人中心，自动刷新

---

## 📱 安装与部署

### 环境要求

- **Android Studio**: Hedgehog (2023.1.1) 或更高版本
- **JDK**: 17 或更高版本
- **Android SDK**: API 26 (Android 8.0) 至 API 34
- **Gradle**: 8.0+
- **Kotlin**: 1.9.0+

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/yourusername/iCyclist.git
cd iCyclist
```

#### 2. 配置高德地图API密钥

**获取API Key：**
1. 访问 [高德开放平台](https://lbs.amap.com/)
2. 注册/登录账号
3. 进入控制台 → 应用管理 → 创建新应用
4. 添加Key：选择"Android平台"
5. 填写应用包名：`com.example.icyclist`
6. 获取SHA1指纹：
   ```bash
   # Windows PowerShell
   .\gradlew printSHA1
   
   # 或手动获取
   keytool -list -v -keystore %USERPROFILE%\.android\debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
7. 复制SHA1值填入高德控制台

**配置密钥：**
```bash
# 复制示例配置文件
cp apikeys.properties.example apikeys.properties

# 编辑 apikeys.properties，填入你的API密钥
AMAP_API_KEY=你的高德地图API密钥
```

#### 3. 同步项目依赖
```bash
# 在Android Studio中点击
File → Sync Project with Gradle Files

# 或使用命令行
./gradlew build
```

#### 4. 连接设备并运行

**方式一：Android Studio运行**
1. 连接Android设备或启动模拟器
2. 点击 `Run` 按钮或按 `Shift + F10`

**方式二：命令行安装**
```bash
# 编译并安装Debug版本
./gradlew installDebug

# 启动应用
adb shell am start -n com.example.icyclist/.MainContainerActivity
```

---

## 📖 使用指南

### 首次使用

1. **注册账号**
   - 启动应用，进入注册页面
   - 填写邮箱、昵称、密码
   - 点击"注册"

2. **登录应用**
   - 输入注册的邮箱和密码
   - 点击"登录"
   - 自动跳转到主界面

### 开始骑行

1. **进入运动模块**
   - 点击底部导航栏"运动"Tab
   - 等待GPS定位完成

2. **开始追踪**
   - 点击"开始骑行"按钮
   - 应用开始记录轨迹和运动数据

3. **结束运动**
   - 点击"结束运动"按钮
   - 确认保存
   - 可选择分享到骑友圈

### 社交互动

1. **查看骑友圈**
   - 点击底部导航栏"骑友圈"Tab
   - 浏览其他用户的动态
   - 点赞、评论

2. **发布动态**
   - 点击右下角"+"按钮
   - 输入文字、选择图片
   - 可选择关联运动记录
   - 点击"发布"

3. **参与论坛讨论**
   - 点击底部导航栏"社区"Tab
   - 选择感兴趣的分类
   - 查看主题、发表回复
   - 点击"+"创建新主题

### 个人资料管理

1. **查看个人信息**
   - 点击底部导航栏"我的"Tab
   - 查看运动统计数据

2. **编辑资料**
   - 点击"编辑资料"
   - 更换头像、修改昵称
   - 点击"保存"

3. **查看运动历史**
   - 点击"运动历史"
   - 查看所有历史记录
   - 点击记录查看详情

---

## 🔧 开发指南

### 项目配置文件

#### `gradle.properties`
```properties
# JVM参数配置
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=GBK \
  --add-exports=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
  ...（其他JDK模块导出配置）

# 并行构建
org.gradle.parallel=true

# 配置缓存（因KAPT兼容性暂时禁用）
org.gradle.configuration-cache=false

# AndroidX
android.useAndroidX=true
```

#### `app/build.gradle.kts`
```kotlin
dependencies {
    // 核心库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    
    // 高德地图
    implementation("com.amap.api:3dmap:latest.integration")
    
    // Room数据库
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    
    // 网络请求
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
}
```

### 添加新功能

#### 1. 创建新的Activity
```kotlin
// 1. 创建Activity类
class NewFeatureActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewFeatureBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewFeatureBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

// 2. 在AndroidManifest.xml注册
<activity
    android:name=".NewFeatureActivity"
    android:exported="false"
    android:screenOrientation="portrait" />
```

#### 2. 添加数据库表
```kotlin
// 1. 定义Entity
@Entity(tableName = "new_table")
data class NewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val field1: String,
    val field2: Long
)

// 2. 定义DAO
@Dao
interface NewDao {
    @Insert
    suspend fun insert(entity: NewEntity)
    
    @Query("SELECT * FROM new_table")
    suspend fun getAll(): List<NewEntity>
}

// 3. 更新SportDatabase
@Database(
    entities = [..., NewEntity::class],
    version = 6  // 版本号+1
)
abstract class SportDatabase : RoomDatabase() {
    abstract fun newDao(): NewDao
    
    companion object {
        // 添加迁移策略
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE new_table (...)")
            }
        }
    }
}
```

### 调试技巧

#### 查看日志
```bash
# 实时查看应用日志
adb logcat | grep "iCyclist"

# 查看特定TAG
adb logcat -s "SportFragment"

# 清空日志缓冲区
adb logcat -c
```

#### 数据库调试
```bash
# 进入设备Shell
adb shell

# 进入应用数据目录
cd /data/data/com.example.icyclist/databases/

# 使用SQLite查看数据
sqlite3 sport_database

# SQLite命令
.tables                          # 查看所有表
.schema sport_records           # 查看表结构
SELECT * FROM sport_records;    # 查询数据
```

#### 性能优化

**内存优化：**
- 使用 Glide 加载图片，避免OOM
- 及时释放 Bitmap 资源
- 使用 `lifecycleScope` 管理协程生命周期

**启动优化：**
- 延迟初始化非必要组件
- 使用 `SplashScreen` API

**网络优化：**
- 使用 OkHttp 连接池
- 启用响应缓存

---

## 🐛 常见问题

### 1. 编译错误：KAPT 不兼容

**错误信息：**
```
IllegalAccessError: class org.jetbrains.kotlin.kapt3.base.javac.KaptJavaCompiler 
cannot access class com.sun.tools.javac.main.JavaCompiler
```

**解决方案：**
```bash
# 1. 停止Gradle守护进程
./gradlew --stop

# 2. 清理构建缓存
./gradlew clean

# 3. 删除.gradle和build目录
rm -rf .gradle app/build

# 4. 重新构建
./gradlew build
```

### 2. 高德地图不显示

**可能原因：**
- API Key未配置或错误
- SHA1指纹不匹配
- 网络权限未授予

**检查步骤：**
```kotlin
// 1. 检查AndroidManifest.xml中的KEY
<meta-data
    android:name="com.amap.api.v2.apikey"
    android:value="${AMAP_API_KEY}" />

// 2. 验证SHA1指纹
./gradlew printSHA1

// 3. 检查网络权限
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

### 3. GPS定位不准确

**优化建议：**
- 在室外空旷环境测试
- 确保设备GPS功能已开启
- 等待GPS信号稳定（通常需要10-30秒）
- 调整定位模式：
  ```kotlin
  locationOption.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
  ```

### 4. 图片加载失败或OOM

**解决方案：**
```kotlin
// 使用Glide加载图片
Glide.with(context)
    .load(imageUrl)
    .override(800, 800)  // 限制图片尺寸
    .centerCrop()
    .into(imageView)

// 在后台线程保存图片
lifecycleScope.launch(Dispatchers.IO) {
    // 图片处理逻辑
}
```

---

## 📄 许可证

本项目为课程作业项目，仅供学习交流使用。

---

## 👥 贡献者

- **开发者**: [Your Name]
- **指导老师**: [Teacher Name]

---

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- **Email**: your.email@example.com
- **GitHub Issues**: [项目Issues页面](https://github.com/yourusername/iCyclist/issues)

---

## 🙏 致谢

- 感谢 [高德地图开放平台](https://lbs.amap.com/) 提供地图SDK
- 感谢 Android 开源社区的各类优秀库
- 感谢课程老师的指导与支持

---

<div align="center">
  <p>⭐ 如果这个项目对你有帮助，请给它一个Star！</p>
  <p>Made with ❤️ by iCyclist Team</p>
</div>

