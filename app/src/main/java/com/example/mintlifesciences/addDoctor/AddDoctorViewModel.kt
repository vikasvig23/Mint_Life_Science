package com.example.mintlifesciences.addDoctor

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utility
import com.example.mintlifesciences.recentDoctors.RecentDoctorData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddDoctorViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var activity: AddDoctorActivity

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    private val userId: String? = sharedPreferences.getString("userId", null)

    private var _docDate = MutableLiveData<List<DoctorData>>()
    val docData: LiveData<List<DoctorData>> get() = _docDate

    fun init(activity: AddDoctorActivity) {
        this.activity = activity
        activity.binding.btn.background = Utility.createGeadientDrawable(
            25f,
            ContextCompat.getColor(activity, R.color.purple_500),
            ContextCompat.getColor(activity, R.color.purple_500)
        )
    }

    fun addDoctor(doctor: DoctorData) {
        val updatedList = _docDate.value?.toMutableList()
        updatedList?.add(doctor)
        _docDate.value = updatedList!!
    }

    fun saveDoctorData(selectedItem: String, doctor: DoctorData) {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(id).child("Mint_Life_Science_Client").child(selectedItem)
                .child("Doctors").child(doctor.docName).setValue(doctor)
            saveInRecentDoctors(selectedItem, doctor)
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot save doctor data.")
    }

    fun saveInRecentDoctors(selectedItem: String, doctor: DoctorData) {
        userId?.let { id ->
            val recentDoctor = RecentDoctorData(
                docName = doctor.docName,
                docSpeciality = doctor.docSpeciality,
                brandName = selectedItem
            )
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(id).child("RecentDoctors").child(recentDoctor.docName)
                .setValue(recentDoctor)
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot save recent doctor data.")
    }

    fun loadDoctorData(selectedItem: String) {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(id).child("Mint_Life_Science_Client").child(selectedItem)
                .child("Doctors").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val doctorList = mutableListOf<DoctorData>()
                    for (doctorSnapshot in snapshot.children) {
                        if (doctorSnapshot.key != "no_doctors") {
                            val doctor = doctorSnapshot.getValue(DoctorData::class.java)
                            if (doctor != null) {
                                Log.d("FirebaseData", "Doctor: $doctor")
                                doctorList.add(doctor)
                            } else {
                                Log.e("FirebaseData", "Error parsing doctor data.")
                            }
                        } else {
                            Log.d("FirebaseData", "Skipping placeholder value.")
                        }
                    }
                    _docDate.value = doctorList
                    Log.d("FirebaseData", "Doctor list updated: $doctorList")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseData", "Failed to retrieve data: ${error.message}")
                }
            })
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot load doctor data.")
    }

    fun deleteDoctor(brandName: String, doctorName: String) {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val doctorRef =
                databaseReference.child(id).child("Mint_Life_Science_Client").child(brandName)
                    .child("Doctors").child(doctorName)

            doctorRef.removeValue().addOnSuccessListener {
                Log.d("DeleteDoctor", "Doctor $doctorName deleted successfully.")

                val brandDoctorsRef =
                    databaseReference.child(id).child("Mint_Life_Science_Client").child(brandName)
                        .child("Doctors")
                brandDoctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                            brandDoctorsRef.child("no_doctors").setValue(true)
                                .addOnSuccessListener {
                                    Log.d(
                                        "DeleteDoctor",
                                        "Placeholder added under $brandName to preserve the brand."
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DeleteDoctor", "Failed to add placeholder: $e")
                                }
                        } else {
                            Log.d("DeleteDoctor", "Doctors still exist under brand: $brandName.")
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("DeleteDoctor", "Failed to check remaining doctors: ${error.message}")
                    }
                })
            }.addOnFailureListener { e ->
                Log.e("DeleteDoctor", "Failed to delete doctor: $e")
            }
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot delete doctor.")
    }
}
