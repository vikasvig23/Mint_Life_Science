package com.mintlifescience.app.allPresentation

import android.content.ActivityNotFoundException
import android.content.Context
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
import com.mintlifescience.app.R
import com.mintlifescience.app.aboutUs.AboutUsActivity
import com.mintlifescience.app.addDoctor.AddDoctorActivity
import com.mintlifescience.app.addDoctor.AddDoctorViewModel
import com.mintlifescience.app.databinding.ActivityAllPresentationBinding
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.recentDoctors.RecentDoctorsActivity
import com.mintlifescience.app.helperUtils.AppUtils
import com.google.android.material.navigation.NavigationView

class All_Presentation : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityAllPresentationBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var adapter: AllPresentationAdapter
    private lateinit var addDoctorViewModel: AddDoctorViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllPresentationBinding.inflate(layoutInflater)
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
            val filteredDoctors = doctors?.filter { it.havePresentation } ?: emptyList()
            adapter.updateList(filteredDoctors)

            // Handle "No Presentations Found" text visibility
            if (filteredDoctors.isEmpty()) {
                binding.noPresentationFoundText.visibility = View.VISIBLE
                binding.mainRecyclerView.visibility = View.GONE
            } else {
                binding.noPresentationFoundText.visibility = View.GONE
                binding.mainRecyclerView.visibility = View.VISIBLE
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
        binding.navigationView.setNavigationItemSelectedListener(this)


        val headerView = binding.navigationView.getHeaderView(0)
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
        adapter = AllPresentationAdapter(emptyList())
        binding.mainRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.mainRecyclerView.adapter = adapter
    }

    private fun setupVersionInfo() {
        val versionName = AppUtils.getAppVersion(this)
        val navView = findViewById<NavigationView>(R.id.navigation_view) // Updated ID
        val versionTextView = navView.findViewById<TextView>(R.id.nav_ver) // Updated ID
        versionTextView.text = "MintLifeSciences $versionName"
    }

    private fun handleBackPressed() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
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
                startActivity(intent)
            }
            R.id.nav_about -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_presentation -> {
                val intent = Intent(this, All_Presentation::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }

            R.id.nav_privacyPolicy -> {
                val url = "https://www.mintlifesciences.com/privacy-policy.php"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                try {
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(this, "No browser found to open the link", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.nav_logout -> {
                loginViewModel.logout()
            }
        }
        // Remove selection from the clicked item
        binding.navigationView.menu.findItem(item.itemId).isChecked = false

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
