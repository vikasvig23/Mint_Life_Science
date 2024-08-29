package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.Model.Medicine

data class DoctorData(
    val docName: String = "",
    val docSpeciality: String = "",
    val medicines: List<Medicine> = emptyList() // Add this line
)