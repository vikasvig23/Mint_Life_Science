package com.example.mintlifesciences.recentDoctors

import com.example.mintlifesciences.model.Medicine

data class RecentDoctorData(
    val docName: String = "",
    val docSpeciality: String = "",
    val medicines: List<Medicine> = emptyList(), // Add this line
    val brandName: String = ""
)