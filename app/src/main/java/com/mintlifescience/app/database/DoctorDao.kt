package com.mintlifescience.app.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DoctorDao {

    // Doctor-related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoctors(doctors: List<DoctorEntity>)

    @Query("SELECT * FROM doctor_table")
    suspend fun getAllDoctors(): List<DoctorEntity>

    @Query("DELETE FROM doctor_table")
    suspend fun clearDoctors()

    // Medicine-related queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<MedicineEntity>)

    @Query("SELECT * FROM medicine_table WHERE doctorName = :doctorName")
    suspend fun getMedicinesByDoctor(doctorName: String): List<MedicineEntity>

    @Query("DELETE FROM medicine_table WHERE doctorName = :doctorName")
    suspend fun deleteMedicinesForDoctor(doctorName: String)


}
