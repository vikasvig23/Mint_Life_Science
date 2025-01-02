package com.example.mintlifesciences.database

import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.addDoctor.Group
import com.example.mintlifesciences.model.FeedbackData
import com.example.mintlifesciences.model.Medicine
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DoctorRepository(private val doctorDao: DoctorDao) {

    suspend fun saveDoctors(doctors: List<DoctorData>) {
        val entities = doctors.map { it.toEntity() }
        doctorDao.insertDoctors(entities)
    }

    suspend fun getDoctors(): List<DoctorData> {
        return doctorDao.getAllDoctors().map { it.toDoctorData() }
    }

    suspend fun saveMedicinesForDoctor(doctorName: String, medicines: List<MedicineEntity>) {
        doctorDao.insertMedicines(medicines)
    }

    suspend fun getMedicinesForDoctor(doctorName: String): List<Medicine> {
        return doctorDao.getMedicinesByDoctor(doctorName).map { it.toMedicine() }
    }

    suspend fun clearDoctors() {
        doctorDao.clearDoctors()
    }

    suspend fun deleteMedicinesForDoctor(doctorName: String) {
        doctorDao.deleteMedicinesForDoctor(doctorName)
    }
}

fun Medicine.toEntity(doctorName: String): MedicineEntity {
    return MedicineEntity(
        image = image,
        name = name,
        salt = salt,
        description = description,
        uses = uses,
        mrp = mrp,
        videoUrl = videoUrl,
        pdfUrl = pdfUrl,
        doctorName = doctorName
    )
}

fun MedicineEntity.toMedicine(): Medicine {
    return Medicine(
        image = image,
        name = name,
        salt = salt,
        description = description,
        uses = uses,
        mrp = mrp,
        videoUrl = videoUrl,
        pdfUrl = pdfUrl
    )
}

fun DoctorData.toEntity(): DoctorEntity {
    val gson = Gson()
    return DoctorEntity(
        docName = docName,
        docSpeciality = docSpeciality,
        groups = groups, // Directly assign the list; Room will use the converter
        scheduleMeet = scheduleMeet,
        havePresentation = havePresentation,
        feedback = feedback,
        lastAdded = lastAdded
    )
}

fun DoctorEntity.toDoctorData(): DoctorData {
    return DoctorData(
        docName = docName,
        docSpeciality = docSpeciality,
        groups = groups, // Room will automatically convert the stored JSON back to List<Group>
        scheduleMeet = scheduleMeet,
        havePresentation = havePresentation,
        feedback = feedback,
        lastAdded = lastAdded
    )
}
