# 快速开始指南

## 构建项目

1. 同步Gradle依赖
```bash
./gradlew build
```

2. 确保 `apikeys.properties` 文件存在并包含高德地图API密钥
```properties
AMAP_API_KEY=your_amap_api_key_here
```

## 使用运动记录功能

### 第一次使用

1. **授予权限**
   - 打开应用后，允许位置权限
   - Android 10及以上需要允许后台定位

2. **等待定位**
   - 确保GPS信号良好（建议在户外）
   - 等待地图上出现蓝色定位点

### 开始运动

1. 点击 **"开始运动"** 按钮
2. 开始移动（骑行、跑步或步行）
3. 地图上会显示蓝色的实时轨迹

### 结束运动

1. **长按** 红色的 **"结束运动"** 按钮
2. 等待保存完成提示
3. 在列表中查看运动记录

### 查看历史记录

- 向下滚动列表查看所有运动记录
- 每条记录显示：
  - 轨迹缩略图
  - 日期时间
  - 用时
  - 距离
  - 平均速度

## 常见问题

### Q: 为什么GPS定位不准确？
A: 
- 确保在户外使用
- 避开高楼密集区域
- 等待GPS信号稳定（通常需要30秒）

### Q: 为什么无法保存运动记录？
A: 
- 确保至少移动产生2个有效轨迹点
- 检查是否有存储权限
- 查看Log输出的错误信息

### Q: 轨迹为什么不连续？
A: 
- GPS信号弱会导致定位失败
- 应用会过滤精度差的点
- 移动速度过快可能导致采样不足

### Q: 如何删除运动记录？
A: 
- 当前版本暂不支持删除功能
- 可以通过清除应用数据来删除所有记录

## 开发调试

### 查看日志
```bash
adb logcat | grep MainActivity
```

### 数据库位置
```
/data/data/com.example.icyclist/databases/sport_database
```

### 缩略图位置
```
/data/data/com.example.icyclist/files/track_*.png
```

### 使用Android Studio Database Inspector
1. 运行应用
2. View -> Tool Windows -> App Inspection
3. 选择 Database Inspector
4. 查看 sport_database

## 性能优化建议

1. **电量优化**
   - 运动记录会持续使用GPS
   - 建议使用移动电源

2. **存储空间**
   - 每条记录约占用几KB到几十KB
   - 轨迹缩略图通常小于50KB

3. **内存使用**
   - 长时间运动会积累大量轨迹点
   - 建议单次运动不超过6小时

## 技术支持

遇到问题请查看：
- `SPORT_TRACKING_GUIDE.md` - 详细功能说明
- `IMPLEMENTATION_SUMMARY.md` - 技术实现总结
- `README.md` - 项目总体说明

## 版本信息

- Android Min SDK: 26 (Android 8.0)
- Android Target SDK: 34 (Android 14)
- Kotlin Version: 最新
- Room Version: 2.6.1
- 高德地图SDK: latest.integration
