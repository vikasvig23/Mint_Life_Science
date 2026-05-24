package com.mintlifescience.app.homescreen

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.mintlifescience.app.databinding.ActivityHomeBinding
import com.mintlifescience.app.doctorMedicine.BrandMedicinesActivity
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.NetworkUtils
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.medicinePresentation.MedicineScreenActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter
    private lateinit var doctorName: String
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""

        // Show the doctor's name as a subtitle so the user always knows whose profile they're in
        binding.toolbarSubtitle.text = doctorName

        val userId = PrefsManager.userId(this) ?: run {
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

        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })

        // 2-column grid looks great for brand cards
        adapter = HomeAdapter(emptyList()) { item -> navigateToBrand(item) }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        viewModel.items.observe(this) { adapter.updateItems(it) }

        // Start Presentation FAB — prominent, easy to find
        binding.fabPresentation.setOnClickListener { startPresentation() }

    }

    private fun navigateToBrand(brandName: String) {
        if (NetworkUtils.isAvailable(applicationContext)) {
            startActivity(
                Intent(this, BrandMedicinesActivity::class.java)
                    .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName)
                    .putExtra(AppConstants.IntentKeys.BRAND_NAME, brandName)
            )
        } else {
            Toast.makeText(this, "No internet connection. Please check and try again.", Toast.LENGTH_LONG).show()
        }
    }

    private fun startPresentation() {
        if (!NetworkUtils.isAvailable(applicationContext)) {
            Toast.makeText(this, "No internet connection. Please check and try again.", Toast.LENGTH_LONG).show()
            return
        }
        viewModel.updateDoctorPresentationStatus(doctorName, true)
        startActivity(
            Intent(this, MedicineScreenActivity::class.java)
                .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName)
                .putExtra(AppConstants.IntentKeys.IS_PRESENTATION, true)
        )
        finish()
    }

}
