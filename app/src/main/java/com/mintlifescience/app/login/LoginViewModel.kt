package com.mintlifescience.app.login

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
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.helperUtils.SingleLiveEvent

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)

    private val _navigateToHome = SingleLiveEvent<Unit>()
    val navigateToHome: LiveData<Unit> = _navigateToHome

    private val _navigateToLogin = SingleLiveEvent<Unit>()
    val navigateToLogin: LiveData<Unit> = _navigateToLogin

    private val _errorMessage = SingleLiveEvent<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Reps authenticate against the Realtime Database, not Firebase Auth.
    // A rep's record lives under /Users/{key} with their email and password; the
    // admin app creates it by approving a signup request. Match the email, then
    // compare the stored password.
    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            _errorMessage.value = "Please enter email and password"
            return
        }
        _isLoading.value = true
        db.orderByChild("email").equalTo(trimmedEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _isLoading.value = false

                    val userNode = snapshot.children.firstOrNull()
                    if (userNode == null) {
                        // No account yet — either never signed up or still awaiting admin approval
                        _errorMessage.value = "No account found for this email. It may still be pending admin approval."
                        return
                    }

                    val user = userNode.getValue(UserData::class.java)
                    if (user?.password != password) {
                        _errorMessage.value = "Incorrect password"
                        return
                    }

                    val nodeKey = userNode.key ?: user.id
                    if (nodeKey == null) {
                        _errorMessage.value = "Login failed. Please try again."
                        return
                    }

                    PrefsManager.saveLoginState(getApplication(), nodeKey, trimmedEmail)
                    user.username?.let { PrefsManager.saveUserName(getApplication(), it) }
                    _navigateToHome.value = Unit
                }

                override fun onCancelled(error: DatabaseError) {
                    _isLoading.value = false
                    _errorMessage.value = error.message
                }
            })
    }

    fun fetchUserName(email: String, onComplete: (String) -> Unit) {
        db.orderByChild("email").equalTo(email.trim())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.children.firstOrNull()
                        ?.getValue(UserData::class.java)?.username
                    onComplete(username ?: "")
                }
                override fun onCancelled(error: DatabaseError) { onComplete("") }
            })
    }

    fun logout() {
        PrefsManager.clearSession(getApplication())
        _navigateToLogin.value = Unit
    }
}
