package com.example.mintlifesciences.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicine_table")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val image: String? = null,
    val name: String? = null,
    val salt: String? = null,
    val description: String? = null,
    val uses: String? = null,
    val mrp: String? = null,
    val videoUrl: String? = null,
    val pdfUrl: String? = null,
    val doctorName: String // Foreign key to link with DoctorEntity
)
