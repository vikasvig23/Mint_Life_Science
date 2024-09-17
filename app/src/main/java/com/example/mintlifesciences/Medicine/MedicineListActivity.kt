package com.example.mintlifesciences.Medicine

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.adapters.MedicineAdapter
import com.example.mintlifesciences.model.Medicine
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityMedicineListBinding
import com.example.mintlifesciences.login.LoginViewModel
import com.google.firebase.database.*

class MedicineListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicineListBinding
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var database: DatabaseReference

    private lateinit var brandName: String
    private lateinit var doctorName: String

    private var medicineList: MutableList<Medicine> = mutableListOf()
    private var selectedMedicines: MutableList<Medicine> = mutableListOf()
    private var alreadySelectedMedicine: MutableList<Medicine> = mutableListOf()
    private lateinit var userId: String


    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        medicineAdapter =
            MedicineAdapter(this, medicineList, selectedMedicines, alreadySelectedMedicine)
        binding.recyclerView.adapter = medicineAdapter

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)


        brandName = intent.getStringExtra("brand_name") ?: ""
        doctorName = intent.getStringExtra("doctorName") ?: ""


        // Initialize SharedPreferences inside onCreate
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG)
                .show()

            loginViewModel.logout()
            return
        }

        fetchDoctorMedicines()
        fetchMedicines()

        // Search functionality
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMedicines(newText ?: "")
                return true
            }
        })

        binding.done.setOnClickListener {
            // Compare lists properly by their content
            if (selectedMedicines != alreadySelectedMedicine) {
                saveDoctorData()
            }
            finish()
        }
    }

    /// FUNCTION TO FETCH MEDICINES
    private fun fetchMedicines() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Initialize Firebase Database
        database =
            FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Admin").child(brandName)

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicineList.clear()
                for (medicineSnapshot in snapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        medicineList.add(medicine)
                    }
                }
                medicineAdapter.updateMedicineList(medicineList)
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
                Toast.makeText(
                    this@MedicineListActivity,
                    "Failed to fetch medicines",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    /// FUNCTION TO FILTER MEDICINES
    private fun filterMedicines(query: String) {
        val trimmedQuery = query.trim()
        val filteredList = if (trimmedQuery.isEmpty()) {
            medicineList // Show the full list if the query is empty or just spaces
        } else {
            medicineList.filter {
                it.name?.contains(trimmedQuery, ignoreCase = true) ?: false
            }
        }
        medicineAdapter.updateMedicineList(filteredList)

        // Show a message if no results are found
        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show()
        }
    }

    /// FUNCTION TO SAVE DOCTOR DATA
    private fun saveDoctorData() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val doctorMedicinesRef = databaseReference.child(userId).child("Mint_Life_Science_Client")
            .child(brandName).child("Doctors").child(doctorName).child("medicines")

        doctorMedicinesRef.setValue(selectedMedicines)
            .addOnSuccessListener {
                Log.d("MedicineListActivity", "Doctor data saved successfully!")
                saveInRecentDoctors()
                selectedMedicines.clear()
            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to save doctor data", e)
            }
    }

    private fun saveInRecentDoctors() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val recentDoctorsRef =
            databaseReference.child(userId).child("RecentDoctors").child(doctorName)
                .child("medicines")

        recentDoctorsRef.setValue(selectedMedicines)
            .addOnSuccessListener {
                Log.d("MedicineListActivity", "Doctor data saved to RecentDoctors successfully!")
                limitRecentDoctors()
            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to save doctor data to RecentDoctors", e)
            }
    }

    private fun limitRecentDoctors() {
        val recentDoctorsRef = FirebaseDatabase.getInstance().getReference("Users").child(userId)
            .child("RecentDoctors")
        recentDoctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val children = snapshot.children.toList()
                if (children.size > 20) {
                    val oldestChild = children.firstOrNull()
                    oldestChild?.key?.let { oldestKey ->
                        recentDoctorsRef.child(oldestKey).removeValue()
                            .addOnSuccessListener {
                                Log.d(
                                    "MedicineListActivity",
                                    "Oldest doctor removed from RecentDoctors"
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e("MedicineListActivity", "Failed to remove oldest doctor", e)
                            }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(
                    "MedicineListActivity",
                    "Failed to retrieve RecentDoctors data: ${error.message}"
                )
            }
        })
    }

    /// FUNCTION TO FETCH DOCTOR MEDICINES
    private fun fetchDoctorMedicines() {
        // Fetch doctor details from Firebase
        val db = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client").child(brandName).child("Doctors")
            .child(doctorName)

        db.get().addOnSuccessListener { dataSnapshot ->
            val doctorData = dataSnapshot.getValue(DoctorData::class.java)
            if (doctorData != null) {
                // Fetch medicines from the "medicines" node
                val medicinesSnapshot = dataSnapshot.child("medicines")
                alreadySelectedMedicine.clear()
                for (medicineSnapshot in medicinesSnapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        alreadySelectedMedicine.add(medicine)
                    }
                }
            } else {
                Toast.makeText(this, "No doctor data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("DoctorMedicineActivity", "Failed to fetch doctor data", e)
        }
    }
}


//binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//        super.onScrolled(recyclerView, dx, dy)
//        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//        val visibleItemCount = layoutManager.childCount
//        val totalItemCount = layoutManager.itemCount
//        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//
//        if (!medicineViewModel.isLoading && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount) {
//            // Fetch more data when reaching the end
//            medicineViewModel.fetchMedicines(brandName, isLoadMore = true)
//        }
//    }
//})
//The scroll listener checks if the user has scrolled near the end of the list.
//When the user reaches the end, it calls fetchMedicines() with isLoadMore = true to append more items.
//The pagination is handled smoothly, fetching 20 items at a time and appending them to the list.
