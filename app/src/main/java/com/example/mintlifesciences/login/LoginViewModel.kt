package com.example.mintlifesciences.login

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.mintlifesciences.MainActivity // Replace with your target activity
import com.example.mintlifesciences.R
import com.example.mintlifesciences.UserData
import com.example.mintlifesciences.Utility
import com.example.mintlifesciences.homescreen.HomeActivity
import com.google.firebase.database.*

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var activity: LoginActivity
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = firebaseDatabase.getReference("Users")
    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun init(activity: LoginActivity) {
        this.activity = activity
        activity.binding.btn.background = Utility.createGeadientDrawable(
            25f,
            ContextCompat.getColor(activity, R.color.purple_500),
            ContextCompat.getColor(activity, R.color.purple_500)
        )
    }

    fun login(email: String, password: String) {
        databaseReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(UserData::class.java)
                            if (user?.password == password) {
                                // Save login state
                                with(sharedPreferences.edit()) {
                                    putBoolean("isLoggedIn", true)
                                    putString("userEmail", email)
                                    apply()
                                }

                                Toast.makeText(activity, "Login Successful", Toast.LENGTH_SHORT).show()

                                // Navigate to the next screen
                                val intent = Intent(activity, HomeActivity::class.java)
                                activity.startActivity(intent)
                                activity.finish()
                            } else {
                                Toast.makeText(activity, "Incorrect Password", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(activity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT).show()
                }
            })
    }

    fun logout() {

        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", false)
            remove("userEmail")
            apply()
        }

        val intent = Intent(getApplication(), LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        getApplication<Application>().startActivity(intent)
    }
}
