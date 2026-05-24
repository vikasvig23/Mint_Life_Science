package com.mintlifescience.app.signUp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mintlifescience.app.UserData
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.SingleLiveEvent

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val usersRef = FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
    private val requestsRef = FirebaseDatabase.getInstance()
        .getReference(FirebaseConstants.SIGNUP_REQUESTS)

    private val _navigateToLogin = SingleLiveEvent<Unit>()
    val navigateToLogin: LiveData<Unit> = _navigateToLogin

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _successMessage = SingleLiveEvent<String>()
    val successMessage: LiveData<String> = _successMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun signUp(username: String, email: String, password: String) {
        _isLoading.value = true
        usersRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _isLoading.value = false
                        _errorMessage.value = "Email already registered"
                        return
                    }
                    checkPendingRequests(username, email, password)
                }
                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    _errorMessage.value = error.message
                }
            })
    }

    private fun checkPendingRequests(username: String, email: String, password: String) {
        requestsRef.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        _isLoading.value = false
                        _errorMessage.value = "A sign-up request for this email is already pending"
                        return
                    }
                    submitRequest(username, email, password)
                }
                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    _errorMessage.value = error.message
                }
            })
    }

    private fun submitRequest(username: String, email: String, password: String) {
        val key = requestsRef.push().key ?: run {
            _isLoading.value = false
            _errorMessage.value = "Failed to create request"
            return
        }
        val userData = UserData(id = key, username = username, email = email, password = password)
        requestsRef.child(key).setValue(userData)
            .addOnSuccessListener {
                _isLoading.value = false
                _successMessage.value = "Sign-up request sent for admin approval"
                _navigateToLogin.value = Unit
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Sign-up request failed"
            }
    }
}
