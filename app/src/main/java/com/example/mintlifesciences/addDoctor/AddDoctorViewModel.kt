package com.example.mintlifesciences.addDoctor

import android.app.Application
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

class AddDoctorViewModel(application: Application):AndroidViewModel(application) {
    lateinit var activity: AddDoctorActivity

    private var _docDate= MutableLiveData<List<DoctorData>>()
    val docData:LiveData<List<DoctorData>> get()=_docDate

  //  private val database:DatabaseReference= FirebaseDatabase.getInstance().getReference("Doctors_Data")

    fun init(activity: AddDoctorActivity){
       // loadDoctorFromFirebase()
        this.activity=activity
        activity.binding.btn.background= Utility.createGeadientDrawable(25f,
            ContextCompat.getColor(activity, R.color.purple_500,),
            ContextCompat.getColor(activity, R.color.purple_500))
    }

    fun addDoctor(doctor:DoctorData){
        val updatedList= _docDate.value?.toMutableList()
        updatedList?.add(doctor)
        _docDate.value=updatedList!!

    }

    fun saveDoctorData(selectedItem: String, doctor: DoctorData) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
        databaseReference.child(selectedItem).child("Doctors").child(doctor.docName).setValue(doctor)

        saveInRecentDoctors(selectedItem,doctor)
    }

    fun saveInRecentDoctors(selectedItem: String, doctor: DoctorData) {

        val recentDoctor = RecentDoctorData(
            docName = doctor.docName,
            docSpeciality = doctor.docSpeciality,
            brandName = selectedItem
        )

        // Reference to the RecentDoctors node in Firebase
        val databaseReference = FirebaseDatabase.getInstance().getReference("RecentDoctors")
        databaseReference.child(recentDoctor.docName).setValue(recentDoctor)
    }

    fun loadDoctorData(selectedItem: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")

        databaseReference.child(selectedItem).child("Doctors").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctorList = mutableListOf<DoctorData>()
                for (doctorSnapshot in snapshot.children) {
                    val doctor = doctorSnapshot.getValue(DoctorData::class.java)
                    if(doctor!=null){
                        Log.d("FirebaseData","Doctor: $doctor")
                        doctorList.add(doctor)
                    }
                    else {
                        Log.e("FirebaseData", "Error parsing doctor data.")
                    }

                    //doctor?.let { doctorList.add(it) }
                }
                _docDate.value = doctorList
                Log.d("FirebaseData", "Doctor list updated: $doctorList")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseData", "Failed to retrieve data: ${error.message}")
            }
        })
    }

    fun deleteDoctor(brandName: String, doctorName: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
        val doctorRef = databaseReference.child(brandName).child("Doctors").child(doctorName)


        doctorRef.removeValue().addOnSuccessListener {
            Log.d("DeleteDoctor", "Doctor $doctorName deleted successfully.")


            val brandDoctorsRef = databaseReference.child(brandName).child("Doctors")
            brandDoctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {

                        brandDoctorsRef.child("no_doctors").setValue(true)
                            .addOnSuccessListener {
                                Log.d("DeleteDoctor", "Placeholder added under $brandName to preserve the brand.")
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
    }

}