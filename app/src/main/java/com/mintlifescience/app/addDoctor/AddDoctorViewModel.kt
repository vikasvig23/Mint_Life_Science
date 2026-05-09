package com.mintlifescience.app.addDoctor

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.helperUtils.SingleLiveEvent
import com.mintlifescience.app.database.DoctorDatabase
import com.mintlifescience.app.database.DoctorRepository
import com.mintlifescience.app.model.FeedbackData
import kotlinx.coroutines.launch

class AddDoctorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs get() = PrefsManager
    private val userId: String? get() = prefs.userId(getApplication())

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _docData = MutableLiveData<List<DoctorData>>()
    val docData: LiveData<List<DoctorData>> get() = _docData

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val doctorDao = DoctorDatabase.getDatabase(application).doctorDao()
    private val repository = DoctorRepository(doctorDao)

    fun setLoadingState(loading: Boolean) { _isLoading.value = loading }

    fun addDoctor(doctor: DoctorData) {
        val updated = _docData.value.orEmpty().toMutableList().also { it.add(0, doctor) }
        _docData.value = updated
    }

    fun saveDoctorData(doctor: DoctorData) {
        val id = userId ?: run {
            _errorMessage.value = "Session expired. Please log in again."
            return
        }
        val withTimestamp = doctor.copy(lastAdded = System.currentTimeMillis())
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(id).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctor.docName)
            .setValue(withTimestamp)
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to save doctor: ${e.message}"
            }
    }

    suspend fun saveDoctorsLocally(doctors: List<DoctorData>) = repository.saveDoctors(doctors)

    suspend fun getDoctorsFromLocal(): List<DoctorData> = repository.getDoctors()

    fun loadDoctorData() {
        _isLoading.value = true
        val id = userId ?: run {
            _isLoading.value = false
            _errorMessage.value = "Session expired. Please log in again."
            return
        }
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(id).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).orderByChild("lastAdded")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = snapshot.children.mapNotNull { it.getValue(DoctorData::class.java) }.reversed()
                    _docData.value = list
                    _isLoading.value = false
                    viewModelScope.launch { saveDoctorsLocally(list) }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AddDoctorViewModel", "loadDoctorData cancelled: ${error.message}")
                    _isLoading.value = false
                    _errorMessage.value = error.message
                }
            })
    }

    fun deleteDoctor(doctorName: String) {
        val id = userId ?: return
        val ref = FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(id).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctorName)

        ref.removeValue()
            .addOnSuccessListener {
                viewModelScope.launch {
                    doctorDao.deleteDoctorByName(doctorName)
                    doctorDao.deleteMedicinesForDoctor(doctorName)
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to delete doctor: ${e.message}"
            }
    }

    fun fetchUserDetails() {
        val id = userId ?: return
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS).child(id)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    val email = snapshot.child("email").getValue(String::class.java)
                    username?.let { PrefsManager.saveUserName(getApplication(), it) }
                    email?.let {
                        PrefsManager.saveLoginState(getApplication(), id, it)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("AddDoctorViewModel", "fetchUserDetails cancelled: ${error.message}")
                }
            })
    }

    fun fetchFeedbackForDoctor(doctorName: String, callback: (Result<List<FeedbackData>>) -> Unit) {
        val id = userId ?: run {
            callback(Result.failure(Exception("Not logged in")))
            return
        }
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(id).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctorName)
            .child(FirebaseConstants.FEEDBACK)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        val list = snapshot.children.mapNotNull { it.getValue(FeedbackData::class.java) }
                        callback(Result.success(list))
                    } catch (e: Exception) {
                        callback(Result.failure(e))
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback(Result.failure(Exception(error.message)))
                }
            })
    }
}
