package com.example.icyclist.database

import androidx.room.*

@Dao
interface SportRecordDao {
    @Query("SELECT * FROM sport_records ORDER BY startTime DESC")
    suspend fun getAllRecords(): List<SportRecordEntity>

    @Query("SELECT * FROM sport_records WHERE id = :id")
    suspend fun getRecordById(id: Long): SportRecordEntity?

    @Insert
    suspend fun insertRecord(record: SportRecordEntity): Long

    @Update
    suspend fun updateRecord(record: SportRecordEntity)

    @Delete
    suspend fun deleteRecord(record: SportRecordEntity)

    @Query("DELETE FROM sport_records")
    suspend fun deleteAllRecords()
}
