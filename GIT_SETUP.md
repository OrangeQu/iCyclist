# 🚀 Git 仓库初始化指南

如果这是一个新项目,按照以下步骤初始化 Git 仓库并安全提交代码。

## 📋 初始化步骤

### 1️⃣ 初始化 Git 仓库

```bash
# 在项目根目录执行
git init
```

### 2️⃣ 验证 .gitignore 配置

```bash
# 确认 .gitignore 存在且配置正确
cat .gitignore

# 应该看到以下关键配置:
# - apikeys.properties
# - local.properties
# - build/
# - .gradle/
```

### 3️⃣ 添加文件到暂存区

```bash
# 添加所有应提交的文件
git add .gitignore
git add README.md
git add SECURITY_CHECKLIST.md
git add apikeys.properties.example
git add local.properties.example
git add build.gradle.kts
git add settings.gradle.kts
git add gradle.properties
git add app/
git add gradle/

# 查看即将提交的文件
git status
```

### 4️⃣ 安全检查 ⚠️

```bash
# 确认敏感文件不在暂存区
git status | findstr /I "apikeys.properties local.properties"

# 如果上面的命令有输出(除了 .example 文件),说明有问题!
# 应该只看到 apikeys.properties.example 和 local.properties.example
```

### 5️⃣ 首次提交

```bash
# 提交初始代码
git commit -m "feat: 初始化 iCyclist 项目

- 配置高德地图 SDK 集成
- 实现定位功能
- 添加安全的 API 密钥管理机制
- 配置 Git 友好的项目结构"
```

### 6️⃣ 关联远程仓库(如果需要)

```bash
# 添加远程仓库
git remote add origin <你的仓库地址>

# 推送到远程
git branch -M main
git push -u origin main
```

## 🔍 提交后验证

### 在远程仓库检查:

- ✅ `apikeys.properties.example` 存在
- ✅ `local.properties.example` 存在
- ✅ `README.md` 包含配置说明
- ❌ `apikeys.properties` 不应存在
- ❌ `local.properties` 不应存在
- ❌ 不应看到真实的 API 密钥

### 克隆仓库测试:

```bash
# 在另一个目录克隆
cd /tmp
git clone <你的仓库地址> test-clone
cd test-clone

# 检查敏感文件是否存在
ls apikeys.properties       # 应该报错: 找不到文件
ls local.properties         # 应该报错: 找不到文件

# 检查示例文件是否存在
ls apikeys.properties.example   # 应该存在
ls local.properties.example     # 应该存在
```

## 🎯 新团队成员配置步骤

当新成员克隆仓库后:

```bash
# 1. 克隆仓库
git clone <仓库地址>
cd iCyclist

# 2. 创建本地配置文件
cp apikeys.properties.example apikeys.properties
cp local.properties.example local.properties

# 3. 编辑配置文件
# - 在 apikeys.properties 中填入你的高德 API 密钥
# - 在 local.properties 中填入你的 Android SDK 路径

# 4. 同步 Gradle
# 在 Android Studio 中点击 "Sync Project with Gradle Files"

# 5. 运行应用
# 连接设备或启动模拟器,点击 Run
```

## 🛠️ 常用 Git 命令

```bash
# 查看当前状态
git status

# 查看更改内容
git diff

# 添加文件
git add <文件名>

# 提交更改
git commit -m "描述你的更改"

# 推送到远程
git push

# 拉取最新代码
git pull

# 查看提交历史
git log --oneline

# 搜索敏感信息(安全检查)
git grep -i "api_key"
git grep -i "密钥"
```

## 📞 遇到问题?

- 检查 `SECURITY_CHECKLIST.md` 了解安全最佳实践
- 查看 `README.md` 了解项目配置说明
- 如果不小心提交了敏感信息,参考 `SECURITY_CHECKLIST.md` 中的补救措施

祝开发顺利! 🎉
