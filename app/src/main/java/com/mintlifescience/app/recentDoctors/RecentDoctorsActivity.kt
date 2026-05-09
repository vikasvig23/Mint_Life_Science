package com.mintlifescience.app.recentDoctors

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.mintlifescience.app.R
import com.mintlifescience.app.aboutUs.AboutUsActivity
import com.mintlifescience.app.addDoctor.AddDoctorActivity
import com.mintlifescience.app.addDoctor.AddDoctorViewModel
import com.mintlifescience.app.allPresentation.All_Presentation
import com.mintlifescience.app.databinding.ActivityRecentDoctorsBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.AppUtils
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginViewModel

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

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        addDoctorViewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AddDoctorViewModel::class.java]

        setupDrawer()
        setupRecyclerView()
        setupVersionInfo()

        addDoctorViewModel.docData.observe(this) { doctors ->
            val recent = doctors?.sortedByDescending { it.lastAdded }?.take(20) ?: emptyList()
            adapter.updateList(recent)
            binding.noDoctorFoundText.visibility = if (recent.isEmpty()) View.VISIBLE else View.GONE
            binding.recDocView.visibility = if (recent.isEmpty()) View.GONE else View.VISIBLE
        }

        if (addDoctorViewModel.docData.value.isNullOrEmpty()) {
            addDoctorViewModel.loadDoctorData()
        }

        loginViewModel.navigateToLogin.observe(this) {
            startActivity(Intent(this, com.mintlifescience.app.login.LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START))
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                else finish()
            }
        })
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

        val userName = PrefsManager.userName(this)
        val userEmail = PrefsManager.userEmail(this) ?: ""

        if (userName == null && userEmail.isNotEmpty()) {
            loginViewModel.fetchUserName(userEmail) { name ->
                if (name.isNotEmpty()) {
                    PrefsManager.saveUserName(this, name)
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
        binding.recentNavView.findViewById<TextView>(R.id.recent_nav_ver)
            ?.text = "MintLifeSciences $versionName"
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, AddDoctorActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            R.id.nav_doctors -> startActivity(Intent(this, RecentDoctorsActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            R.id.nav_about -> startActivity(Intent(this, AboutUsActivity::class.java))
            R.id.nav_presentation -> startActivity(Intent(this, All_Presentation::class.java))
            R.id.nav_privacyPolicy -> openPrivacyPolicy()
            R.id.nav_logout -> loginViewModel.logout()
        }
        binding.recentNavView.menu.findItem(item.itemId).isChecked = false
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun openPrivacyPolicy() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.PRIVACY_POLICY_URL)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No browser found to open the link", Toast.LENGTH_SHORT).show()
        }
    }
}
