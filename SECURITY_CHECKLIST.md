# 🔐 Git 安全提交检查清单

在提交代码到 Git 之前,请确保以下事项:

## ✅ 必须检查的项目

### 1. 敏感文件已被 `.gitignore` 排除

- [ ] `apikeys.properties` - 包含 API 密钥
- [ ] `local.properties` - 包含本地 SDK 路径
- [ ] `*.keystore` / `*.jks` - 签名证书文件(如果有)

### 2. 配置文件中无硬编码密钥

- [ ] `gradle.properties` - 不应包含 `AMAP_API_KEY` 或其他密钥
- [ ] `local.properties` - 不应包含 API 密钥(仅包含 SDK 路径)
- [ ] `build.gradle.kts` - 不应硬编码密钥,应从文件读取
- [ ] `AndroidManifest.xml` - 使用 `${AMAP_API_KEY}` 占位符,不硬编码

### 3. 示例文件已提供

- [ ] `apikeys.properties.example` - 已创建并包含占位符
- [ ] `local.properties.example` - 已创建并包含说明
- [ ] `README.md` - 包含配置说明

## 🚫 不应提交的内容

```
❌ apikeys.properties          (包含真实密钥)
❌ local.properties            (包含本地路径)
❌ *.keystore / *.jks          (签名证书)
❌ google-services.json        (如果包含敏感配置)
❌ build/                      (构建产物)
❌ .gradle/                    (Gradle 缓存)
❌ .idea/ (部分文件)           (IDE 配置)
```

## ✅ 应该提交的内容

```
✅ apikeys.properties.example  (模板文件)
✅ local.properties.example    (模板文件)
✅ .gitignore                  (忽略规则)
✅ README.md                   (项目说明)
✅ build.gradle.kts            (构建脚本)
✅ gradle.properties           (Gradle 配置,不含密钥)
✅ 源代码文件                   (*.kt, *.xml 等)
```

## 🔍 提交前检查命令

运行以下命令检查即将提交的文件:

```bash
# 查看即将提交的文件
git status

# 查看具体更改内容
git diff

# 搜索是否有硬编码的密钥
git grep -i "45d2957aadba33132959499897a33fab"
git grep -i "AMAP_API_KEY.*="
```

## 🛡️ 如果不小心提交了敏感信息

如果已经提交了包含密钥的文件:

1. **立即更换泄露的 API 密钥**
2. **从 Git 历史中移除敏感信息**:

   ```bash
   # 使用 git filter-branch 或 BFG Repo-Cleaner
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch apikeys.properties" \
     --prune-empty --tag-name-filter cat -- --all

   # 强制推送(谨慎操作!)
   git push origin --force --all
   ```

3. **通知团队成员**更新他们的本地仓库

## 📝 最佳实践

1. ✅ 使用环境变量或配置文件管理敏感信息
2. ✅ 在 `.gitignore` 中明确标注敏感文件
3. ✅ 提供 `.example` 模板文件供团队参考
4. ✅ 在 README 中说明如何配置敏感信息
5. ✅ 定期审查 Git 历史,确保无敏感信息泄露
6. ✅ 使用 Git hooks 自动检查敏感信息(可选)

## 🎯 快速自检

提交前快速检查:

```bash
# 1. 确认 .gitignore 正确配置
cat .gitignore | grep -E "apikeys|local.properties"

# 2. 确认敏感文件不在暂存区
git status | grep -E "apikeys.properties|local.properties"

# 3. 确认没有硬编码密钥
grep -r "45d2957aadba33132959499897a33fab" --exclude-dir=.git .
```

如果以上检查都通过,可以安全提交! 🎉
