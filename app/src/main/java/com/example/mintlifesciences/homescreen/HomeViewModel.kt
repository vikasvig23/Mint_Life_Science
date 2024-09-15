package com.example.mintlifesciences.homescreen

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeViewModel (application: Application) : AndroidViewModel(application) {
    lateinit var activity: HomeActivity
    private val _items = MutableLiveData<List<String>>()
    val items: LiveData<List<String>> get() = _items

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")



    fun init(activity:HomeActivity) {

        fetchDataFromFirebase()
        this.activity = activity
    }
    fun refreshData() {
        fetchDataFromFirebase() // Refresh data from Firebase
    }


    //_items.value = listOf("Mini Life Sciences Pvt Ltd", "USP Life Sciences", "USP Medicraft", "Critical Care", "Gyno Care", "Bv-Clean")
        fun fetchDataFromFirebase() {

        _loading.value = true

        if (!isNetworkAvailable(getApplication<Application>().applicationContext)) {
            _loading.value = true
            _error.value = "No Internet Connection"
            return
        }

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val itemList = mutableListOf<String>()
                        for (itemSnapshot in snapshot.children) {
                            itemSnapshot.key?.let { itemList.add(it) }
                        }
                        _items.value = itemList
                    } else {
                        val initialList = listOf(
                            "Mini Life Sciences Pvt Ltd",
                            "USP Life Sciences",
                            "USP Medicraft",
                            "Critical Care",
                            "Gyno Care",
                            "Bv-Clean"
                        )
                        _items.value = initialList
                        saveInitialListToFirebase(initialList)
                    }

                    _loading.value = false
                }

                override fun onCancelled(error: DatabaseError) {
                    _loading.value = false
                    _error.value = error.message
                }
            })
        }





    private fun showLoader() {
        activity.binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        activity.binding.progressBar.visibility = View.GONE
    }



    private fun saveInitialListToFirebase(initialList: List<String>) {
        for (item in initialList) {
            databaseRef.child(item).setValue(true)  // Save as keys in Firebase
        }
    }


    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun errorHandled() {
        _error.value = null
    }


}