package com.example.mintlifesciences.homescreen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.medicinePresentation.MedicineScreenActivity
import com.example.mintlifesciences.model.BrandItem
import com.example.mintlifesciences.model.Medicine
import com.google.firebase.database.FirebaseDatabase

class HomeActivity : AppCompatActivity(){

    lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter
    private lateinit var doctorName: String
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Retrieve doctorName and brandName from the Intent
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

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the current activity
            }
        })


        adapter = HomeAdapter(emptyList()) { item ->
            navigateToNextScreen(item)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter



        viewModel.items.observe(this, Observer { items ->
            updateUI(items)
        })

//        binding.swipeRefreshLayout.setOnRefreshListener {
//            viewModel.refreshData()
//            Handler().postDelayed({
//                binding.swipeRefreshLayout.isRefreshing = false
//            }, 5000)
//        }

//        viewModel.loading.observe(this, Observer { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            if (!isLoading) {
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//        })
//
//        viewModel.error.observe(this, Observer { errorMessage ->
//            errorMessage?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//                viewModel.errorHandled()
//            }
//        })


        binding.menu.setOnClickListener {
            openMenu()
        }
    }

    private fun updateUI(items: List<BrandItem>) {
        items?.let {
            adapter.updateItems(it)
        }
    }

    private fun navigateToNextScreen(item: String) {
        if (viewModel.isNetworkAvailable(getApplication().applicationContext)){
        val intent = Intent(this, DoctorMedicineActivity::class.java)
        intent.putExtra("doctorName",doctorName)
        intent.putExtra("brandName",item)
        startActivity(intent)
    }
        else {
            Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
        }
    }

    private fun openMenu() {
        val popupMenu = PopupMenu(this, binding.menu)
        menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.make_presentation -> {
                    // Set isPresentation to true for the doctor
                    viewModel.updateDoctorPresentationStatus(doctorName, true)

                    // Navigate to MedicineScreenActivity
                    val intent = Intent(this, MedicineScreenActivity::class.java)
                    intent.putExtra("doctorName", doctorName)
                    intent.putExtra("isPresentation", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun addSelectedMedicineToFirebase(selectedMedicine: Medicine, brandName : String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val doctorMedicinesRef = databaseReference.child(userId)
            .child("Mint_Life_Science_Client")
            .child("Doctors")
            .child(doctorName)
            .child("medicines")
            .child(brandName)

        // Retrieve existing medicines first
        doctorMedicinesRef.get().addOnSuccessListener { snapshot ->
            val medicineList = snapshot.children.mapNotNull { it.getValue(Medicine::class.java) }.toMutableList()

            // Avoid duplicate entries
            if (!medicineList.any { it.name == selectedMedicine.name }) {
                medicineList.add(selectedMedicine)

                // Update Firebase with the new list
                doctorMedicinesRef.setValue(medicineList)
                    .addOnSuccessListener {
                        Log.d("MedicineListActivity", "Medicine added successfully!")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MedicineListActivity", "Failed to add medicine", e)
                    }
            }
        }
    }



}
