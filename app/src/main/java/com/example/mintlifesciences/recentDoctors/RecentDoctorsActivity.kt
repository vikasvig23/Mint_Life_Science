package com.example.mintlifesciences.recentDoctors

import android.content.Context
import android.content.Intent
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
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.addDoctor.AddDoctorViewModel
import com.example.mintlifesciences.databinding.ActivityRecentDoctorsBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.utils.AppUtils
import com.google.android.material.navigation.NavigationView

class RecentDoctorsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityRecentDoctorsBinding
    private lateinit var adapter: RecentDoctorAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var addDoctorViewModel: AddDoctorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentDoctorsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Initialize AddDoctorViewModel
        addDoctorViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(AddDoctorViewModel::class.java)

        setupDrawer()
        setupRecyclerView()
        setupVersionInfo()

        // Observe the LiveData from AddDoctorViewModel
        addDoctorViewModel.docData.observe(this) { doctors ->
            adapter.updateList(doctors ?: emptyList())
        }

        // Load data into ViewModel if not already loaded
        if (addDoctorViewModel.docData.value.isNullOrEmpty()) {
            addDoctorViewModel.loadDoctorData()
        }

        handleBackPressed()
    }

    private fun setupDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.recentNavView.setNavigationItemSelectedListener(this)
    }

    private fun setupRecyclerView() {
        adapter = RecentDoctorAdapter(emptyList())
        binding.recDocView.layoutManager = LinearLayoutManager(this)
        binding.recDocView.adapter = adapter
    }

    private fun setupVersionInfo() {
        val versionName = AppUtils.getAppVersion(this)
        val navView = findViewById<NavigationView>(R.id.recent_nav_view)
        val versionTextView = navView.findViewById<TextView>(R.id.recent_nav_ver)
        versionTextView.text = "MintLifeSciences $versionName"
    }

    private fun handleBackPressed() {
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, AddDoctorActivity::class.java)
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
