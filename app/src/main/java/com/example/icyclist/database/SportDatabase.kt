package com.example.icyclist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [SportRecordEntity::class, CommunityPostEntity::class],
    version = 2, // 数据库版本升级到 2
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SportDatabase : RoomDatabase() {
    abstract fun sportRecordDao(): SportRecordDao
    abstract fun communityPostDao(): CommunityPostDao // 确保 DAO 被声明

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

        fun getDatabase(context: Context): SportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportDatabase::class.java,
                    "sport_database"
                )
                .addMigrations(MIGRATION_1_2) // 添加迁移策略
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
