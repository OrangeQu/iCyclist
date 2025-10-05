# 🎯 快速参考卡片

## 📋 日常开发快速检查

### ✅ 提交代码前 (每次都要检查!)

```bash
# 1️⃣ 查看即将提交的文件
git status

# 2️⃣ 确认没有敏感文件
git status | findstr /I "apikeys.properties"
# ⚠️ 应该只看到 apikeys.properties.example,不应该看到 apikeys.properties

# 3️⃣ 搜索是否有硬编码的密钥
git diff | findstr /I "45d2957aadba33132959499897a33fab"
# ⚠️ 不应该有任何输出

# 4️⃣ 提交
git add .
git commit -m "你的提交信息"
git push
```

---

## 🔐 敏感文件速查表

| 文件                         | 包含内容      | 提交到 Git? | 位置       |
| ---------------------------- | ------------- | ----------- | ---------- |
| `apikeys.properties`         | 真实 API 密钥 | ❌ 否       | 项目根目录 |
| `local.properties`           | SDK 路径      | ❌ 否       | 项目根目录 |
| `apikeys.properties.example` | 密钥模板      | ✅ 是       | 项目根目录 |
| `local.properties.example`   | 配置模板      | ✅ 是       | 项目根目录 |

---

## 🆕 新成员配置 (3 步完成)

```bash
# 1. 创建配置文件
cp apikeys.properties.example apikeys.properties

# 2. 编辑 apikeys.properties
# 将 your_amap_api_key_here 替换为你的密钥

# 3. Sync Gradle 并运行
```

---

## 🚨 紧急情况处理

### 如果不小心提交了密钥:

```bash
# 1. 立即更换 API 密钥! (去高德控制台)

# 2. 从暂存区移除
git reset HEAD apikeys.properties

# 3. 如果已经 push,联系管理员清理历史
```

---

## 📞 常见问题

**Q: 构建失败,提示找不到 API 密钥?**

```
A: 检查 apikeys.properties 文件是否存在
   检查文件中是否有 AMAP_API_KEY=你的密钥
```

**Q: 如何获取高德地图 API 密钥?**

```
A: 访问 https://console.amap.com/dev/key/app
   注册/登录后创建应用获取密钥
```

**Q: 可以在 gradle.properties 中添加密钥吗?**

```
A: ❌ 不可以! gradle.properties 会被提交到 Git
   只能在 apikeys.properties 中添加
```

**Q: 团队成员如何获取密钥?**

```
A: 每个成员需要自己申请高德 API 密钥
   不要共享密钥文件
```

---

## 📚 完整文档

- 📖 `README.md` - 项目完整说明
- 🔒 `SECURITY_CHECKLIST.md` - 安全检查清单
- 🚀 `GIT_SETUP.md` - Git 初始化指南
- 📊 `SUMMARY.md` - 配置总结
- 🎯 `QUICK_REFERENCE.md` - 本文档

---

**💡 提示**: 将此文件加入书签,随时查阅! 🌟
