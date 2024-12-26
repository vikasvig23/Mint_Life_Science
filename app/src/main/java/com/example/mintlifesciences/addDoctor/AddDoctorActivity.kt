package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.Utils.NetworkChangeReceiver
import com.example.mintlifesciences.aboutUs.AboutUsActivity
import com.example.mintlifesciences.allPresentation.All_Presentation
import com.example.mintlifesciences.databinding.ActivityAddDoctorBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.recentDoctors.RecentDoctorsActivity
import com.example.mintlifesciences.helperUtils.AppUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

class AddDoctorActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var binding: ActivityAddDoctorBinding
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var adapter: AddDoctorAdapter
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var networkChangeReceiver: NetworkChangeReceiver


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_doctor)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[AddDoctorViewModel::class.java]
        viewModel.init(this)
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Setup RecyclerView
        binding.recDocView.layoutManager = LinearLayoutManager(this)
        adapter = AddDoctorAdapter(this, emptyList(), viewModel)
        binding.recDocView.adapter = adapter

        // Initialize the BroadcastReceiver
        networkChangeReceiver = NetworkChangeReceiver {
            // This block executes when internet is available
            viewModel.setLoadingState(true) // Show progress bar
            viewModel.loadDoctorData() // Fetch doctor data
        }

        // Register the receiver to listen for network changes
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)

        // Observe isLoading LiveData to show/hide the progress bar
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.setLoadingState(true)

        if (viewModel.isNetworkAvailable(applicationContext)) {
            viewModel.loadDoctorData()
        } else {
            viewModel.setLoadingState(false)
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show()
        }


        // Observe Doctor Data
        viewModel.docData.observe(this) { doctors ->
            Log.d("AddDoctorActivity", "Received data: $doctors")
            adapter.updateList(doctors)
        }

        // Set up button listener
        binding.btn.setOnClickListener { showDoctorDialog() }


        // Navigation drawer setup
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val versionName = AppUtils.getAppVersion(this)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val versionTextView = navView.findViewById<TextView>(R.id.nav_ver)
        versionTextView.text = "MintLifeSciences $versionName"

        // Handle back button press on system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })

        viewModel.fetchUserDetails()
        setupDrawer()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkChangeReceiver)
    }


    private fun setupDrawer() {
        val headerView = binding.navView.getHeaderView(0)
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

    private fun showDoctorDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null)

        dialogView.setOnClickListener {
            val intent = Intent(this, DoctorMedicineActivity::class.java)
            startActivity(intent)
        }

        val docName = dialogView.findViewById<TextInputEditText>(R.id.doc_edit)
        val docSpec = dialogView.findViewById<TextInputEditText>(R.id.spec_edit)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<AppCompatButton>(R.id.cnfrmBtn).setOnClickListener {
            val name =
                docName.text?.toString()?.trim()?.split(" ")?.joinToString(" ") { it.capitalize() }
                    ?: ""
            val speciality =
                docSpec.text?.toString()?.trim()?.split(" ")?.joinToString(" ") { it.capitalize() }
                    ?: ""

            if (name.isEmpty() || speciality.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val doctor = DoctorData(name, speciality)
            viewModel.saveDoctorData(doctor)
            viewModel.addDoctor(doctor)  // Ensure both methods are necessary
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, AddDoctorActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }

            R.id.nav_doctors -> {
                val intent = Intent(this, RecentDoctorsActivity::class.java)
                startActivity(intent)
                finish()
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
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
