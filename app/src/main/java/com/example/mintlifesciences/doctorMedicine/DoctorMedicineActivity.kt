package com.example.mintlifesciences.doctorMedicine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.Adapters.DoctorMedicineAdapter
import com.example.mintlifesciences.Model.Medicine
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding
import com.google.firebase.database.*

class DoctorMedicineActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDoctorMedicineBinding
    private lateinit var brandName: String
    private lateinit var doctorName: String
    private lateinit var database: DatabaseReference
    private lateinit var doctorMedicineAdapter: DoctorMedicineAdapter
    private var medicineList: MutableList<Medicine> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_doctor_medicine)

        doctorName = intent.getStringExtra("doctorName") ?: ""
        brandName = intent.getStringExtra("brandName") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false) // Disable default title

        binding.docT.text = doctorName

        binding.medRec.layoutManager = LinearLayoutManager(this)
        doctorMedicineAdapter = DoctorMedicineAdapter(this, medicineList)
        binding.medRec.adapter = doctorMedicineAdapter

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
            .child(brandName).child("Doctors").child(doctorName)

        // Fetch doctor details and medicines
        fetchDoctorDetails()

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

        // Handle back button press on system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Custom behavior for back press, if any
                finish() // Finish the current activity
            }
        })

    }

    override fun onResume() {
        super.onResume()
        // Fetch doctor details and medicines again when returning to this activity
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
