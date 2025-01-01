package com.example.mintlifesciences.database

import com.example.mintlifesciences.addDoctor.DoctorData

class DoctorRepository(private val doctorDao: DoctorDao) {

    suspend fun saveDoctors(doctors: List<DoctorData>) {
        val entities = doctors.map { it.toEntity() }
        doctorDao.insertDoctors(entities)
    }

    suspend fun getDoctors(): List<DoctorData> {
        return doctorDao.getAllDoctors().map { it.toDoctorData() }
    }

    suspend fun clearDoctors() {
        doctorDao.clearDoctors()
    }
}

fun DoctorData.toEntity(): DoctorEntity = DoctorEntity(
    docName = docName,
    docSpeciality = docSpeciality,
    groups = groups,
    scheduleMeet = scheduleMeet,
    havePresentation = havePresentation,
    feedback = feedback,
    lastAdded = lastAdded
)

fun DoctorEntity.toDoctorData(): DoctorData = DoctorData(
    docName = docName,
    docSpeciality = docSpeciality,
    groups = groups,
    scheduleMeet = scheduleMeet,
    havePresentation = havePresentation,
    feedback = feedback,
    lastAdded = lastAdded
)
