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

    fun saveDoctorData(doctor: DoctorData) {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val doctorWithTimestamp = doctor.copy(lastAdded = System.currentTimeMillis())
            databaseReference.child(id).child("Mint_Life_Science_Client")
                .child("Doctors").child(doctor.docName).setValue(doctorWithTimestamp)
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot save doctor data.")
    }

    fun loadDoctorData() {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            databaseReference.child(id).child("Mint_Life_Science_Client")
                .child("Doctors").orderByChild("lastAdded")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val doctorList = mutableListOf<DoctorData>()
                        for (doctorSnapshot in snapshot.children) {
                            val doctor = doctorSnapshot.getValue(DoctorData::class.java)
                            if (doctor != null) {
                                doctorList.add(doctor)
                            }
                        }
                        _docDate.value = doctorList.reversed()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseData", "Failed to retrieve data: ${error.message}")
                    }
                })
        } ?: Log.e("AddDoctorViewModel", "User ID is null, cannot load doctor data.")
    }


    fun deleteDoctor(doctorName: String) {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            val doctorRef =
                databaseReference.child(id).child("Mint_Life_Science_Client")
                    .child("Doctors").child(doctorName)

            doctorRef.removeValue().addOnSuccessListener {
                Log.d("DeleteDoctor", "Doctor $doctorName deleted successfully.")

                val brandDoctorsRef =
                    databaseReference.child(id).child("Mint_Life_Science_Client")
                        .child("Doctors")
                brandDoctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                            brandDoctorsRef.child("no_doctors").setValue(true)
                                .addOnSuccessListener {
                                    Log.d(
                                        "DeleteDoctor",
                                        "Placeholder added under to preserve the brand."
                                    )
                                }
                                .addOnFailureListener { e ->
                                    Log.e("DeleteDoctor", "Failed to add placeholder: $e")
                                }
                        } else {
                            Log.d("DeleteDoctor", "Doctors still exist.")
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
