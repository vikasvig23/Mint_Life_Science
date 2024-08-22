package com.example.mintlifesciences.homescreen

import android.app.Application
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

    private val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")



    fun init(activity:HomeActivity) {
       // fetchItemsFromFirebase()
        this.activity = activity

        //_items.value = listOf("Mini Life Sciences Pvt Ltd", "USP Life Sciences", "USP Medicraft", "Critical Care", "Gyno Care", "Bv-Clean")
        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // Data exists, retrieve it
                    val itemList = mutableListOf<String>()
                    for (itemSnapshot in snapshot.children) {
                        itemSnapshot.key?.let { itemList.add(it) }
                    }
                    _items.value = itemList
                } else {
                    // Data doesn't exist, store the initial list
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
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle possible errors.
            }
        })
    }

    private fun saveInitialListToFirebase(initialList: List<String>) {
        for (item in initialList) {
            databaseRef.child(item).setValue(true)  // Save as keys in Firebase
        }
    }
}