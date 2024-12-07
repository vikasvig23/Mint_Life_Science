package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.model.Medicine
import java.util.Date

data class DoctorData(
    val docName: String = "",
    val docSpeciality: String = "",
    val groups: List<Group> = emptyList(),
    var scheduleMeet: String = "",
    var havePresentation: Boolean = false
)

data class Group(
    val groupName: String = "",
    val medicines: List<Medicine> = emptyList()
)
