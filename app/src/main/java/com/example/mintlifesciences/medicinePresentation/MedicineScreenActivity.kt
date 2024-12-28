package com.example.mintlifesciences.medicinePresentation

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityMedicineScreenBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.model.Medicine
import com.google.firebase.database.*

class MedicineScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineScreenBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PresentationAdapter
    private val items = mutableListOf<Medicine>()
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var doctorName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // Restore views when fragment is removed
                binding.viewPager.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
            }
        }


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
            val intent = Intent(this, AddDoctorActivity::class.java)
            startActivity(intent)
        }

        doctorName = intent.getStringExtra("doctorName") ?: ""
        fetchDoctorData(doctorName)

        adapter = PresentationAdapter(items, binding.viewPager, this)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }

    private fun fetchDoctorData(doctorName: String) {
        // Show loading indicator while fetching data
        binding.progressBar.visibility = View.VISIBLE

        // The reference path should be corrected based on actual Firebase structure
        val db = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client").child("Doctors")
            .child(doctorName).child("medicines")

        db.get().addOnSuccessListener { dataSnapshot ->
            binding.progressBar.visibility = View.GONE  // Hide loading indicator

            if (dataSnapshot.exists()) {
                items.clear()

                // Iterate through each brand and add medicines to items
                for (brandSnapshot in dataSnapshot.children) {
                    for (medicineSnapshot in brandSnapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        if (medicine != null) {
                            items.add(medicine)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@MedicineScreenActivity, "No medicines found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE  // Hide loading indicator
            Log.e("MedicineScreenActivity", "Failed to fetch doctor data", e)
            Toast.makeText(this@MedicineScreenActivity, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presn_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_update_medicine -> {
                updateDoctorPresentationStatus()
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("doctorName", doctorName)
                startActivity(intent)
                finish()
                return true
            }

            R.id.action_edit_feedback -> {
                // Hide other views
                binding.viewPager.visibility = View.GONE
                binding.toolbar.visibility = View.GONE

                val fragment = FeedbackFragment()
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateDoctorPresentationStatus() {
        val db = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client").child("Doctors")
            .child(doctorName)

       db.child("havePresentation").setValue(false)
            .addOnSuccessListener {
                Toast.makeText(this, "Presentation status updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update presentation status", Toast.LENGTH_SHORT).show()
            }
    }
}
