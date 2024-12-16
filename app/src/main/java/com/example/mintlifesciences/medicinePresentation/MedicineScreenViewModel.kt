package com.example.mintlifesciences.medicinePresentation

import android.app.Application
import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class MedicineScreenViewModel(application: Application) : AndroidViewModel(application) {

    val downloadStatus = MutableLiveData<String>()


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
