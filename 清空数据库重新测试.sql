-- ======================================
-- iCyclist 数据库清空脚本
-- 用途：清空所有测试数据，重新开始测试
-- ======================================

USE icyclist;

-- 1. 清空用户表
TRUNCATE TABLE users;

-- 2. 清空论坛相关表
TRUNCATE TABLE forum_replies;
TRUNCATE TABLE forum_topics;
TRUNCATE TABLE forum_categories;

-- 3. 清空骑行圈相关表
TRUNCATE TABLE comments;
TRUNCATE TABLE post_likes;
TRUNCATE TABLE posts;

-- 4. 清空骑行记录表
TRUNCATE TABLE ride_records;

-- 5. 重新插入论坛分类（基础数据）
INSERT INTO forum_categories (name, description) VALUES
('装备讨论', '分享和讨论骑行装备'),
('路线分享', '推荐你最喜欢的骑行路线'),
('新手问答', '新手上路？在这里提问吧'),
('骑闻轶事', '分享骑行中的趣闻和故事'),
('二手交易', '买卖你的闲置骑行装备');

-- 完成！
SELECT '✅ 数据库已清空，可以重新测试了！' AS message;

