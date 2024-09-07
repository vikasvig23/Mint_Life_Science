package com.example.mintlifesciences.signUp

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.example.mintlifesciences.R
import com.example.mintlifesciences.UserData
import com.example.mintlifesciences.Utility
import com.example.mintlifesciences.homescreen.HomeActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private lateinit var activity: SignUpActivity
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = firebaseDatabase.getReference("Users")

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun init(activity: SignUpActivity) {
        this.activity = activity
        activity.binding.btn.background = Utility.createGeadientDrawable(
            25f,
            ContextCompat.getColor(activity, R.color.purple_500),
            ContextCompat.getColor(activity, R.color.purple_500)
        )
    }


    fun signUp(username: String, email: String, password: String) {

        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(activity, "Email already exists", Toast.LENGTH_SHORT).show()
                } else {

                    val userId = databaseReference.push().key
                    val userData = UserData(id = userId, username = username, email = email, password = password)

                    userId?.let {
                        databaseReference.child(it).setValue(userData).addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                Toast.makeText(activity, "Sign Up Successful", Toast.LENGTH_SHORT).show()

                                val intent = Intent(activity, HomeActivity::class.java)
                                activity.startActivity(intent)
                                activity.finish()
                            } else {
                                Toast.makeText(activity, "Sign Up Failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(activity, "Failed to check email: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
