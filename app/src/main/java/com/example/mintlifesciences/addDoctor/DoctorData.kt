package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.model.FeedbackData
import com.example.mintlifesciences.model.Medicine

data class DoctorData(
    val docName: String = "",
    val docSpeciality: String = "",
    val groups: List<Group> = emptyList(),
    var scheduleMeet: String = "",
    var havePresentation: Boolean = false,
    var feedback: List<FeedbackData> = emptyList(),
    var lastAdded: Long = System.currentTimeMillis() // Timestamp for ordering
)

data class Group(
    val groupName: String = "",
    val medicines: List<Medicine> = emptyList()
)
