package com.example.mintlifesciences.recentDoctors

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.aboutUs.AboutUsActivity
import com.example.mintlifesciences.databinding.ActivityRecentDoctorsBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.utils.AppUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*

class RecentDoctorsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityRecentDoctorsBinding
    private lateinit var adapter: RecentDoctorAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var loginViewModel: LoginViewModel

    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Retrieve userId from SharedPreferences
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", "null") ?: "null"

        if (userId.isNullOrEmpty()) {
            Log.e("RecentDoctorsActivity", "User ID is null or empty, cannot load recent doctors.")
            loginViewModel.logout()
            return
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Recent Doctors"

        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.recentNavView.setNavigationItemSelectedListener(this)

        val versionName = AppUtils.getAppVersion(this)
        val navView = findViewById<NavigationView>(R.id.recent_nav_view)
        val versionTextView = navView.findViewById<TextView>(R.id.recent_nav_ver)
        versionTextView.text = "MintLifeSciences $versionName"

        // Set up RecyclerView
        adapter = RecentDoctorAdapter(emptyList())
        binding.recDocView.layoutManager = LinearLayoutManager(this)
        binding.recDocView.adapter = adapter

        // Initialize Firebase reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("RecentDoctors")

        // Load recent doctors from Firebase
        loadRecentDoctors()

        // Handle back button press on system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish() // Finish the current activity
                }
            }
        })
    }

    // Load recent doctors from Firebase
    private fun loadRecentDoctors() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctors = mutableListOf<RecentDoctorData>()

                // Iterate through each child node under RecentDoctors
                for (data in snapshot.children) {
                    val doctor = data.getValue(RecentDoctorData::class.java)
                    if (doctor != null) {
                        doctors.add(doctor)
                    } else {
                        Log.e("RecentDoctorsActivity", "Invalid doctor data at: ${data.key}")
                    }
                }

                if (doctors.isEmpty()) {
                    Log.d("RecentDoctorsActivity", "No recent doctors found")
                }

                // Update the adapter with the list of doctors
                adapter.updateList(doctors)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RecentDoctorsActivity", "Failed to load recent doctors", error.toException())
            }
        })
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_doctors -> {
                val intent = Intent(this, RecentDoctorsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }

            R.id.nav_about -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_logout -> {
                loginViewModel.logout()
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
