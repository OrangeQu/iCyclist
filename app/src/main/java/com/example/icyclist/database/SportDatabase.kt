package com.example.icyclist.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SportRecordEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SportDatabase : RoomDatabase() {
    abstract fun sportRecordDao(): SportRecordDao

    companion object {
        @Volatile
        private var INSTANCE: SportDatabase? = null

        fun getDatabase(context: Context): SportDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SportDatabase::class.java,
                    "sport_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
