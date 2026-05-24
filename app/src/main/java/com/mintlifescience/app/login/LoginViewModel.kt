package com.mintlifescience.app.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.helperUtils.SingleLiveEvent

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)

    private val _navigateToHome = SingleLiveEvent<Unit>()
    val navigateToHome: LiveData<Unit> = _navigateToHome

    private val _navigateToLogin = SingleLiveEvent<Unit>()
    val navigateToLogin: LiveData<Unit> = _navigateToLogin

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter email and password"
            return
        }
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: run {
                    _isLoading.value = false
                    _errorMessage.value = "Login failed. Please try again."
                    return@addOnSuccessListener
                }
                fetchUserNodeId(uid, email)
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Login failed"
            }
    }

    // Firebase Auth UID ≠ the custom userId key in Realtime DB.
    // Look up the Realtime DB node whose "firebaseUid" matches, or fall back to email match.
    // Username is saved from the same snapshot — no second query needed.
    private fun fetchUserNodeId(firebaseUid: String, email: String) {
        db.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.value = false
                    val userNode = snapshot.children.firstOrNull()
                    val nodeKey = userNode?.key
                    if (nodeKey != null) {
                        // Save username from this same snapshot — avoids a second DB round-trip
                        val username = userNode
                            .getValue(com.mintlifescience.app.UserData::class.java)?.username
                        PrefsManager.saveLoginState(getApplication(), nodeKey, email)
                        username?.let { PrefsManager.saveUserName(getApplication(), it) }
                        _navigateToHome.value = Unit
                    } else {
                        // First login after migration — store uid as the node key
                        PrefsManager.saveLoginState(getApplication(), firebaseUid, email)
                        _navigateToHome.value = Unit
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    _errorMessage.value = error.message
                }
            })
    }

    fun fetchUserName(email: String, onComplete: (String) -> Unit) {
        db.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.children.firstOrNull()
                        ?.getValue(com.mintlifescience.app.UserData::class.java)?.username
                    onComplete(username ?: "")
                }
                override fun onCancelled(error: DatabaseError) { onComplete("") }
            })
    }

    fun logout() {
        auth.signOut()
        PrefsManager.clearSession(getApplication())
        _navigateToLogin.value = Unit
    }
}
