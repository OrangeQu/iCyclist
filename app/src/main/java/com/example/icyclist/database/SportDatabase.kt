package com.example.icyclist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        SportRecordEntity::class,
        CommunityPostEntity::class,
        CommentEntity::class,
        LikeEntity::class,
        ForumCategoryEntity::class,
        ForumTopicEntity::class,
        ForumReplyEntity::class
    ],
    version = 5, // 数据库版本升级到 5
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SportDatabase : RoomDatabase() {
    abstract fun sportRecordDao(): SportRecordDao
    abstract fun communityPostDao(): CommunityPostDao
    abstract fun commentDao(): CommentDao
    abstract fun likeDao(): LikeDao
    abstract fun forumCategoryDao(): ForumCategoryDao
    abstract fun forumTopicDao(): ForumTopicDao
    abstract fun forumReplyDao(): ForumReplyDao

    companion object {
        @Volatile
        private var INSTANCE: SportDatabase? = null

        // 定义从版本 1 到 2 的迁移策略
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建 community_posts 表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `community_posts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userAvatar` TEXT NOT NULL,
                        `userNickname` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `timestamp` INTEGER NOT NULL,
                        `likes` INTEGER NOT NULL,
                        `comments` INTEGER NOT NULL,
                        `sportRecordId` INTEGER,
                        `sportDistance` TEXT,
                        `sportDuration` TEXT,
                        `sportThumbPath` TEXT
                    )
                """.trimIndent())
            }
        }

        // 定义从版本 2 到 3 的迁移策略
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建评论表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `comments` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `postId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `userNickname` TEXT NOT NULL,
                        `userAvatar` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 创建点赞表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `likes` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `postId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 创建论坛分类表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `forum_categories` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `topicCount` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 创建论坛主题表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `forum_topics` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `userNickname` TEXT NOT NULL,
                        `userAvatar` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `replyCount` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 创建论坛回复表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `forum_replies` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `topicId` INTEGER NOT NULL,
                        `userId` TEXT NOT NULL,
                        `userNickname` TEXT NOT NULL,
                        `userAvatar` TEXT NOT NULL,
                        `content` TEXT NOT NULL,
                        `timestamp` INTEGER NOT NULL
                    )
                """.trimIndent())

                // 插入初始论坛分类数据
                db.execSQL("INSERT INTO `forum_categories` (`name`, `description`, `topicCount`) VALUES ('装备讨论', '分享和讨论骑行装备', 0)")
                db.execSQL("INSERT INTO `forum_categories` (`name`, `description`, `topicCount`) VALUES ('路线分享', '推荐你最喜欢的骑行路线', 0)")
                db.execSQL("INSERT INTO `forum_categories` (`name`, `description`, `topicCount`) VALUES ('新手问答', '新手上路？在这里提问吧', 0)")
                db.execSQL("INSERT INTO `forum_categories` (`name`, `description`, `topicCount`) VALUES ('骑闻轶事', '分享骑行中的趣闻和故事', 0)")
                db.execSQL("INSERT INTO `forum_categories` (`name`, `description`, `topicCount`) VALUES ('二手交易', '买卖你的闲置骑行装备', 0)")
            }
        }

        // 定义从版本 3 到 4 的迁移策略：清理重复的论坛分类
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 创建临时表
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `forum_categories_temp` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `topicCount` INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // 将去重后的数据插入临时表（每个名称只保留第一条记录）
                db.execSQL("""
                    INSERT INTO `forum_categories_temp` (`name`, `description`, `topicCount`)
                    SELECT `name`, `description`, MAX(`topicCount`)
                    FROM `forum_categories`
                    GROUP BY `name`
                """.trimIndent())
                
                // 删除原表
                db.execSQL("DROP TABLE `forum_categories`")
                
                // 重命名临时表
                db.execSQL("ALTER TABLE `forum_categories_temp` RENAME TO `forum_categories`")
            }
        }

        // 定义从版本 4 到 5 的迁移策略：添加示例论坛数据
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val currentTime = System.currentTimeMillis()
                
                // 插入装备讨论分类的主题
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (1, 'user@example.com', '骑行达人', 'ic_twotone_person_24', '求推荐入门级公路车', '预算5000左右，想买辆入门级公路车，大家有什么推荐吗？主要用于周末城市周边骑行。', ${currentTime - 86400000 * 2}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (1, 'cyclist@example.com', '装备控', 'ic_twotone_person_24', '骑行头盔选择指南', '头盔是骑行最重要的安全装备，这里分享一下我的选购经验：\n1. 一定要选择符合安全标准的产品\n2. 尺寸要合适，试戴很重要\n3. 通风性和舒适度也要考虑\n4. 价格在200-500元的中档产品性价比最高', ${currentTime - 86400000 * 3}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (1, 'bike_lover@example.com', '速度追求者', 'ic_twotone_person_24', '碳纤维车架值得买吗？', '一直在纠结要不要升级碳纤维车架，有经验的朋友来说说碳架和铝架的区别？', ${currentTime - 86400000}, 0)")
                
                // 插入路线分享分类的主题
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (2, 'explorer@example.com', '路线探索者', 'ic_twotone_person_24', '市区环湖骑行路线推荐', '周末骑了一圈城市周边的湖泊，全程30公里，风景超美！路况也很好，适合休闲骑行。起点在湖东公园，沿湖一圈，路上有好几个观景台可以休息拍照。', ${currentTime - 86400000 * 4}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (2, 'mountain_rider@example.com', '山地骑士', 'ic_twotone_person_24', '郊外山地越野线路分享', '上周去郊外探了条新线路，有山路有土路，风景一流！适合山地车骑行，全程约50公里，爬升800米。难度中等，推荐给喜欢挑战的朋友。', ${currentTime - 86400000 * 1}, 0)")
                
                // 插入新手问答分类的主题
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (3, 'newbie@example.com', '新手上路', 'ic_twotone_person_24', '刚买了车，需要准备哪些装备？', '刚入坑，买了辆山地车，除了车本身，还需要准备什么装备呢？求前辈指点！', ${currentTime - 86400000 * 5}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (3, 'beginner@example.com', '零基础学骑行', 'ic_twotone_person_24', '骑行姿势应该注意什么？', '最近开始骑车锻炼，但骑完总觉得腰酸背痛，是不是姿势不对？大家有什么建议吗？', ${currentTime - 86400000 * 2}, 0)")
                
                // 插入骑闻轶事分类的主题
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (4, 'storyteller@example.com', '故事大王', 'ic_twotone_person_24', '第一次长途骑行的经历', '上个月完成了人生第一次200公里长途骑行！从出发时的兴奋，到中途的疲惫，再到最后抵达终点的成就感，这段经历太难忘了。分享给大家，希望给想尝试长途的朋友一些参考。', ${currentTime - 86400000 * 6}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (4, 'adventure_seeker@example.com', '冒险家', 'ic_twotone_person_24', '骑行路上遇到的趣事', '昨天骑车时遇到一只狗一直跟着我跑了3公里，太可爱了哈哈哈！大家骑车时有没有遇到过有趣的事情？', ${currentTime - 86400000}, 0)")
                
                // 插入二手交易分类的主题
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (5, 'seller@example.com', '闲置处理', 'ic_twotone_person_24', '出售9成新山地车', '因升级装备，出售一辆9成新的捷安特ATX777山地车，买了不到半年，保养得很好，配件齐全。价格面议，同城交易优先。', ${currentTime - 86400000 * 3}, 0)")
                db.execSQL("INSERT INTO forum_topics (categoryId, userId, userNickname, userAvatar, title, content, timestamp, replyCount) VALUES (5, 'buyer@example.com', '寻车中', 'ic_twotone_person_24', '求购二手公路车', '想入手一辆二手公路车，预算3000左右，要求车况良好，最好是知名品牌。有出的朋友请联系！', ${currentTime - 86400000 * 2}, 0)")
                
                // 为一些主题添加回复
                db.execSQL("INSERT INTO forum_replies (topicId, userId, userNickname, userAvatar, content, timestamp) VALUES (1, 'expert@example.com', '骑行专家', 'ic_twotone_person_24', '推荐美利达斯特拉94或捷安特OCR，都是经典的入门公路车，性价比很高。', ${currentTime - 86400000})")
                db.execSQL("INSERT INTO forum_replies (topicId, userId, userNickname, userAvatar, content, timestamp) VALUES (1, 'shop_owner@example.com', '车店老板', 'ic_twotone_person_24', '这个预算可以考虑迪卡侬的RC120，配置均衡，适合新手。', ${currentTime - 86400000 + 3600000})")
                
                db.execSQL("INSERT INTO forum_replies (topicId, userId, userNickname, userAvatar, content, timestamp) VALUES (6, 'experienced@example.com', '老骑手', 'ic_twotone_person_24', '基本装备：头盔（必备）、手套、骑行裤、水壶、车灯、简易工具、备胎。这些都配齐了就可以安心出发了！', ${currentTime - 86400000 * 4})")
                
                db.execSQL("INSERT INTO forum_replies (topicId, userId, userNickname, userAvatar, content, timestamp) VALUES (7, 'coach@example.com', '骑行教练', 'ic_twotone_person_24', '注意几点：1.座椅高度调整到踩到底时膝盖微微弯曲；2.握把不要太紧；3.保持上身放松；4.核心要收紧。慢慢调整，会越来越舒服的。', ${currentTime - 86400000})")
                
                // 更新这些主题的回复计数
                db.execSQL("UPDATE forum_topics SET replyCount = 2 WHERE id = 1")
                db.execSQL("UPDATE forum_topics SET replyCount = 1 WHERE id = 6")
                db.execSQL("UPDATE forum_topics SET replyCount = 1 WHERE id = 7")
            }
        }

        fun getDatabase(context: Context): SportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportDatabase::class.java,
                    "sport_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5) // 添加迁移策略
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
