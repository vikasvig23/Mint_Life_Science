package com.example.mintlifesciences.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DoctorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Query("SELECT * FROM doctor_table")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Query("DELETE FROM doctor_table")
    suspend fun clearDoctors()
}
