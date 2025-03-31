package com.mintlifescience.app.medicinePresentation

import android.app.Application
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.mintlifescience.app.model.FeedbackData
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicineScreenViewModel(application: Application) : AndroidViewModel(application) {

    val downloadStatus = MutableLiveData<String>()
    private var doctorReference: DatabaseReference? = null
    var selectedDate: String = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
    var feedbackText: String = ""

    // Set the Firebase reference to the ViewModel
    fun setDoctorReference(reference: DatabaseReference) {
        doctorReference = reference
    }

    fun updateDoctorData(selectedDate: String, feedbackText: String) {
        this.selectedDate = selectedDate
        this.feedbackText = feedbackText
        val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        doctorReference?.let { reference ->
            // Retrieve the current feedback list
            reference.child("feedback").get().addOnSuccessListener { snapshot ->
                val feedbackList = snapshot.children.mapNotNull { it.getValue(FeedbackData::class.java) }.toMutableList()

                // Add the new feedback
                val newFeedback = FeedbackData(
                    message = feedbackText,
                    date = currentDate
                )
                feedbackList.add(newFeedback)

                // Update the feedback list in Firebase
                val updates = mapOf(
                    "scheduleMeet" to selectedDate,
                    "feedback" to feedbackList
                )

                reference.updateChildren(updates).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("MedicineViewModel", "Doctor data updated successfully with new feedback")
                        downloadStatus.postValue("Update successful")
                    } else {
                        Log.e("MedicineViewModel", "Failed to update doctor data", task.exception)
                        downloadStatus.postValue("Update failed")
                    }
                }
            }.addOnFailureListener { error ->
                Log.e("MedicineViewModel", "Failed to fetch feedback list", error)
                downloadStatus.postValue("Failed to fetch feedback list")
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
