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
    version = 2, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SportDatabase : RoomDatabase() {
    abstract fun sportRecordDao(): SportRecordDao
    abstract fun communityPostDao(): CommunityPostDao

    companion object {
        @Volatile
        private var INSTANCE: SportDatabase? = null
        
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS community_posts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userEmail TEXT NOT NULL,
                        userNickname TEXT NOT NULL,
                        userAvatarPath TEXT,
                        content TEXT NOT NULL,
                        thumbnailPath TEXT,
                        timestamp INTEGER NOT NULL,
                        sportRecordId INTEGER
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
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
