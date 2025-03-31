package com.mintlifescience.app.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mintlifescience.app.addDoctor.Group
import com.mintlifescience.app.model.FeedbackData

@Entity(tableName = "doctor_table")
data class DoctorEntity(
    @PrimaryKey val docName: String = "",
    val docSpeciality: String = "",
    @TypeConverters(GroupConverter::class) // Ensure this is present
    val groups: List<Group> = emptyList(),
    var scheduleMeet: String = "",
    var havePresentation: Boolean = false,
    @TypeConverters(FeedbackConverter::class) // For feedback
    var feedback: List<FeedbackData> = emptyList(),
    var lastAdded: Long = System.currentTimeMillis()
)
