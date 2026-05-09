package com.mintlifescience.app.homescreen

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mintlifescience.app.R
import com.mintlifescience.app.databinding.ActivityHomeBinding
import com.mintlifescience.app.doctorMedicine.DoctorMedicineActivity
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""

        val userId = PrefsManager.userId(this) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            loginViewModel.logout()
            return
        }

        loginViewModel.navigateToLogin.observe(this) {
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })

        adapter = HomeAdapter(emptyList()) { item -> navigateToNextScreen(item) }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.items.observe(this) { adapter.updateItems(it) }

        binding.menu.setOnClickListener { openMenu() }
    }

    private fun navigateToNextScreen(item: String) {
        if (NetworkUtils.isAvailable(applicationContext)) {
            val intent = Intent(this, DoctorMedicineActivity::class.java)
                .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName)
                .putExtra(AppConstants.IntentKeys.BRAND_NAME, item)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun openMenu() {
        val popupMenu = PopupMenu(this, binding.menu)
        menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.make_presentation -> {
                    viewModel.updateDoctorPresentationStatus(doctorName, true)
                    startActivity(Intent(this, MedicineScreenActivity::class.java)
                        .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName)
                        .putExtra(AppConstants.IntentKeys.IS_PRESENTATION, true))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
