package com.example.mintlifesciences.recentDoctors

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityRecentDoctorsBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*

class RecentDoctorsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var binding: ActivityRecentDoctorsBinding
    private lateinit var adapter: RecentDoctorAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        // Set up RecyclerView
        adapter = RecentDoctorAdapter(emptyList())
        binding.recDocView.layoutManager = LinearLayoutManager(this)
        binding.recDocView.adapter = adapter

        // Initialize Firebase reference
        databaseReference =
            FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client/RecentDoctors")

        // Load recent doctors from Firebase
        loadRecentDoctors()
    }

    private fun loadRecentDoctors() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val doctors = mutableListOf<RecentDoctorData>()
                for (data in snapshot.children) {
                    val doctor = data.getValue(RecentDoctorData::class.java)
                    if (doctor != null) {
                        doctors.add(doctor)
                    }
                }
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
                // Open RecentDoctorsActivity
                val intent = Intent(this, RecentDoctorsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }
            // Handle other menu items here
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
