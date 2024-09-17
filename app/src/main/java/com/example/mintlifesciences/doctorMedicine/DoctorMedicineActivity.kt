package com.example.mintlifesciences.doctorMedicine

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.Medicine.MedicineListActivity
import com.example.mintlifesciences.adapters.DoctorMedicineAdapter
import com.example.mintlifesciences.model.Medicine
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding
import com.example.mintlifesciences.login.LoginActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.google.firebase.database.*

class DoctorMedicineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorMedicineBinding
    private lateinit var brandName: String
    private lateinit var doctorName: String
    private lateinit var database: DatabaseReference
    private lateinit var doctorMedicineAdapter: DoctorMedicineAdapter
    private var medicineList: MutableList<Medicine> = mutableListOf()
    private lateinit var loginViewModel: LoginViewModel

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_doctor_medicine)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Initialize SharedPreferences inside onCreate
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            finish() // Finish the activity if userId is null
            return
        }

        if (userId.isNullOrEmpty()) {
            Log.e("RecentDoctorsActivity", "User ID is null or empty, cannot load recent doctors.")
            loginViewModel.logout()
            return
        }

        // Retrieve doctorName and brandName from the Intent
        doctorName = intent.getStringExtra("doctorName") ?: ""
        brandName = intent.getStringExtra("brandName") ?: ""

        // Set up the toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        binding.docT.text = doctorName

        // Set up RecyclerView
        binding.medRec.layoutManager = LinearLayoutManager(this)
        doctorMedicineAdapter = DoctorMedicineAdapter(this, medicineList, brandName)
        binding.medRec.adapter = doctorMedicineAdapter

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client")
            .child(brandName).child("Doctors").child(doctorName)

        // Fetch doctor details and medicines
        fetchDoctorDetails()

        // Add Medicine Button Click Listener
        binding.addMed.setOnClickListener {
            val intent = Intent(this, MedicineListActivity::class.java)
            intent.putExtra("brand_name", brandName)
            intent.putExtra("doctorName", doctorName)
            startActivity(intent)
        }

        // Handle back button click
        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed() // Handle back button press
        }

        // Custom back button behavior
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the current activity
            }
        })
    }

    override fun onResume() {
        super.onResume()
        // You can remove this call to fetchDoctorDetails if it’s not necessary to refetch data
        fetchDoctorDetails()
    }

    private fun fetchDoctorDetails() {
        // Fetch doctor details from Firebase
        database.get().addOnSuccessListener { dataSnapshot ->
            val doctorData = dataSnapshot.getValue(DoctorData::class.java)
            if (doctorData != null) {
                // Fetch medicines from the "medicines" node
                val medicinesSnapshot = dataSnapshot.child("medicines")
                medicineList.clear()

                for (medicineSnapshot in medicinesSnapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        medicineList.add(medicine)
                    }
                }

                // Notify the adapter
                doctorMedicineAdapter.updateMedicineList(medicineList)
            } else {
                Toast.makeText(this, "No doctor data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("DoctorMedicineActivity", "Failed to fetch doctor data", e)
        }
    }
}
