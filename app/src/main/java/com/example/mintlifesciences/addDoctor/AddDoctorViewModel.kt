package com.example.mintlifesciences.addDoctor

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utility
import com.example.mintlifesciences.model.FeedbackData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AddDoctorViewModel(application: Application) : AndroidViewModel(application) {
    lateinit var activity: AddDoctorActivity

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

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

    fun setLoadingState(isLoading: Boolean) {
        _isLoading.value = isLoading
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
        _isLoading.value = true // Show progress bar
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
                        _isLoading.value = false // Hide progress bar
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("FirebaseData", "Failed to retrieve data: ${error.message}")
                        _isLoading.value = false // Hide progress bar on error
                    }
                })
        } ?: run {
            Log.e("AddDoctorViewModel", "User ID is null, cannot load doctor data.")
            _isLoading.value = false // Hide progress bar on error
        }
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

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo != null && networkInfo.isConnected
        }
    }

    fun fetchUserDetails() {
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(id)

            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Retrieve username and email from the snapshot
                    val username = dataSnapshot.child("username").getValue(String::class.java)
                    val email = dataSnapshot.child("email").getValue(String::class.java)


                    with(sharedPreferences.edit()) {
                        putString("userName", username)
                        putString("userEmail", email)
                        apply() // Asynchronously save changes
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                    Log.e("fetchUserDetails", "Database error: ${databaseError.message}")
                }
            })
        }
    }


    fun fetchFeedbackForDoctor(
        doctorName: String,
        callback: (Result<List<FeedbackData>>) -> Unit
    ) {
        val feedbackList = mutableListOf<FeedbackData>()

        // Construct the database reference to the specific doctor's feedback node
        userId?.let { id ->
            val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(id)
                .child("Mint_Life_Science_Client")
                .child("Doctors")
                .child(doctorName)
                .child("feedback") // Assuming 'feedback' is the node containing the list

            // Attach a listener to retrieve the feedback list
            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        // Iterate through all children of the 'feedback' node
                        for (feedbackSnapshot in snapshot.children) {
                            // Deserialize each child into a FeedbackData object
                            val feedback = feedbackSnapshot.getValue(FeedbackData::class.java)
                            // Add the feedback to the list if it's not null
                            feedback?.let { feedbackList.add(it) }
                        }
                        // Invoke the callback with the retrieved feedback list wrapped in a Result
                        callback(Result.success(feedbackList))
                    } catch (e: Exception) {
                        // Handle any exceptions that occur during data processing
                        Log.e("ViewModel", "Error processing feedback data: ${e.message}")
                        callback(Result.failure(e))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Log the error and invoke the callback with a failure result
                    Log.e("ViewModel", "Error fetching feedback: ${error.message}")
                    callback(Result.failure(Exception(error.message)))
                }
            })
        }

    }

//
//    fun fetchFeedbackForDoctor(doctorName: String, callback: (List<FeedbackData>) -> Unit) {
//        // Dummy data for testing purposes
//        val feedbackList = listOf(
//            FeedbackData(
//                message = "Excellent doctor!",
//                date = "2024-08-29"
//            ),
//            FeedbackData(
//                message = "Very helpful and caring.",
//                date = "2024-08-30"
//            ),
//            FeedbackData(
//                message = "Explains treatment options well.",
//                date = "2024-08-31"
//            ),
//            FeedbackData(
//                message = "Highly professional and knowledgeable.",
//                date = "2024-09-01"
//            ),
//            FeedbackData(
//                message = "Explains treatment options well.",
//                date = "2024-08-31"
//            ),
//            FeedbackData(
//                message = "Highly professional and knowledgeable.",
//                date = "2024-09-01"
//            ),
//            FeedbackData(
//                message = "Explains treatment options well.",
//                date = "2024-08-31"
//            ),
//            FeedbackData(
//                message = "Highly professional and knowledgeable.",
//                date = "2024-09-01"
//            ),
//            FeedbackData(
//                message = "Explains treatment options well.",
//                date = "2024-08-31"
//            ),
//            FeedbackData(
//                message = "Highly professional and knowledgeable.",
//                date = "2024-09-01"
//            ),
//            FeedbackData(
//                message = "Explains treatment options well.",
//                date = "2024-08-31"
//            ),
//            FeedbackData(
//                message = "Highly professional and knowledgeable.",
//                date = "2024-09-01"
//            )
//        )
//
//        // Pass the dummy data back via the callback
//        callback(feedbackList)
//    }

}
