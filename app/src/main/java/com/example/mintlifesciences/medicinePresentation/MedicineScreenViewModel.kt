package com.example.mintlifesciences.medicinePresentation

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MedicineScreenViewModel(application: Application) : AndroidViewModel(application) {

    val downloadStatus = MutableLiveData<String>()
    private var doctorReference: DatabaseReference? = null
    var selectedDate :String? = null

    // Set the Firebase reference to the ViewModel
    fun setDoctorReference(reference: DatabaseReference) {
        doctorReference = reference
    }

    fun updateDoctorData(selectedDate: String, feedbackText: String) {
        this.selectedDate = selectedDate
        doctorReference?.let { reference ->
            val updates = mapOf(
                "scheduleMeet" to selectedDate,
                "feedback" to feedbackText
            )

            reference.updateChildren(updates).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MedicineViewModel", "Doctor data updated successfully")
                    downloadStatus.postValue("Update successful")
                } else {
                    Log.e("MedicineViewModel", "Failed to update doctor data", task.exception)
                    downloadStatus.postValue("Update failed")
                }
            }
        } ?: run {
            Log.e("MedicineViewModel", "Doctor reference is not set")
            downloadStatus.postValue("Reference not set")
        }
    }

    fun downloadPdfFromFirebase(context: Context, fileName: String) {
        val storageRef = FirebaseStorage.getInstance().reference.child("pdfs/$fileName")

        val outputDir = File(Environment.getExternalStorageDirectory(), "Download")
        if (!outputDir.exists()) outputDir.mkdirs()

        val outputFile = File(outputDir, fileName)

        storageRef.getFile(outputFile)
            .addOnSuccessListener {
                downloadStatus.postValue("PDF downloaded to ${outputFile.absolutePath}")
            }
            .addOnFailureListener { exception ->
                downloadStatus.postValue("Failed to download PDF: ${exception.message}")
            }
    }
}
