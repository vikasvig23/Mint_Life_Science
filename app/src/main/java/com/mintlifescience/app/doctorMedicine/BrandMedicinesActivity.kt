package com.mintlifescience.app.doctorMedicine

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mintlifescience.app.Medicine.MedicineListActivity
import com.mintlifescience.app.adapters.DoctorMedicineAdapter
import com.mintlifescience.app.addDoctor.DoctorData
import com.mintlifescience.app.databinding.ActivityDoctorMedicineBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.model.Medicine
import com.google.firebase.database.FirebaseDatabase

class BrandMedicinesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDoctorMedicineBinding
    private lateinit var brandName: String
    private lateinit var doctorName: String
    private lateinit var userId: String
    private lateinit var doctorMedicineAdapter: DoctorMedicineAdapter
    private var medicineList: MutableList<Medicine> = mutableListOf()
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoctorMedicineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        userId = PrefsManager.userId(this) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            loginViewModel.logout()
            return
        }

        loginViewModel.navigateToLogin.observe(this) {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }

        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""
        brandName  = intent.getStringExtra(AppConstants.IntentKeys.BRAND_NAME) ?: ""

        // Brand name as the main title, doctor name as subtitle
        binding.toolbarTitle.text = brandName
        binding.toolbarSubtitle.text = "Dr. $doctorName"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.medRec.layoutManager = LinearLayoutManager(this)
        doctorMedicineAdapter = DoctorMedicineAdapter(this, medicineList, brandName)
        binding.medRec.adapter = doctorMedicineAdapter

        fetchDoctorDetails()

        binding.addMed.setOnClickListener {
            startActivity(
                Intent(this, MedicineListActivity::class.java)
                    .putExtra(AppConstants.IntentKeys.BRAND_NAME_SHORT, brandName)
                    .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName)
            )
        }

        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchDoctorDetails()
    }

    private fun fetchDoctorDetails() {
        binding.progressBar.visibility = View.VISIBLE
        binding.medRec.visibility = View.GONE
        binding.emptyState.visibility = View.GONE

        FirebaseDatabase.getInstance()
            .getReference(FirebaseConstants.doctorPath(userId, doctorName))
            .get()
            .addOnSuccessListener { dataSnapshot ->
                binding.progressBar.visibility = View.GONE

                dataSnapshot.getValue(DoctorData::class.java) ?: run {
                    showEmpty()
                    return@addOnSuccessListener
                }

                val brandSnapshot = dataSnapshot
                    .child(FirebaseConstants.MEDICINES)
                    .child(brandName)

                medicineList.clear()
                if (brandSnapshot.exists()) {
                    for (snap in brandSnapshot.children) {
                        snap.getValue(Medicine::class.java)?.let { medicineList.add(it) }
                    }
                }

                doctorMedicineAdapter.updateMedicineList(medicineList)

                if (medicineList.isEmpty()) showEmpty() else showList()
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("BrandMedicinesActivity", "Failed to fetch medicines", e)
                Toast.makeText(this, "Failed to load medicines. Please try again.", Toast.LENGTH_SHORT).show()
                showEmpty()
            }
    }

    private fun showList() {
        binding.medRec.visibility = View.VISIBLE
        binding.emptyState.visibility = View.GONE
    }

    private fun showEmpty() {
        binding.medRec.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }
}
