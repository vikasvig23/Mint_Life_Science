package com.mintlifescience.app.recentDoctors

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mintlifescience.app.R
import com.mintlifescience.app.aboutUs.AboutUsActivity
import com.mintlifescience.app.addDoctor.AddDoctorActivity
import com.mintlifescience.app.addDoctor.AddDoctorViewModel
import com.mintlifescience.app.allPresentation.All_Presentation
import com.mintlifescience.app.databinding.ActivityRecentDoctorsBinding
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.helperUtils.AppUtils
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

        addDoctorViewModel.docData.observe(this) { doctors ->
            val recentDoctors = doctors?.sortedByDescending { it.lastAdded }?.take(20) ?: emptyList()
            adapter.updateList(recentDoctors)

            if (recentDoctors.isEmpty()) {
                binding.noDoctorFoundText.visibility = View.VISIBLE
                binding.recDocView.visibility = View.GONE
            } else {
                binding.noDoctorFoundText.visibility = View.GONE
                binding.recDocView.visibility = View.VISIBLE
            }
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

        val headerView = binding.recentNavView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.nav_header_user_name)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.nav_header_user_email)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var userName = sharedPreferences.getString("userName", null)
        val userEmail = sharedPreferences.getString("userEmail", "user@example.com")

        if (userName == null && userEmail != null) {

            loginViewModel.fetchUserName(userEmail) { fetchUser ->
                fetchUser?.let { name ->
                    with(sharedPreferences.edit()) {
                        putString("userName", name)
                        apply()
                    }
                    userNameTextView.text = name
                }
            }
        } else {
            userNameTextView.text = userName
        }
        userEmailTextView.text = userEmail
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
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
            R.id.nav_presentation -> {
                val intent = Intent(this, All_Presentation::class.java)
                startActivity(intent)
            }
            R.id.nav_logout -> {
                loginViewModel.logout()
            }
        }

        // Remove selection from the clicked item
        binding.recentNavView.menu.findItem(item.itemId).isChecked = false

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
