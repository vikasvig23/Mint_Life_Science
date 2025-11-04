package com.mintlifescience.app.addDoctor

import com.mintlifescience.app.doctorMedicine.DoctorMedicineActivity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mintlifescience.app.R
import com.mintlifescience.app.Utils.NetworkChangeReceiver
import com.mintlifescience.app.aboutUs.AboutUsActivity
import com.mintlifescience.app.allPresentation.All_Presentation
import com.mintlifescience.app.databinding.ActivityAddDoctorBinding
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.recentDoctors.RecentDoctorsActivity
import com.mintlifescience.app.helperUtils.AppUtils
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


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

        // Setup SwipeRefreshLayout
        setupSwipeToRefresh()

        // Initialize the BroadcastReceiver
        networkChangeReceiver = NetworkChangeReceiver {
            viewModel.setLoadingState(true)
            viewModel.loadDoctorData()
        }

        // Register the receiver to listen for network changes
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, filter)

        // Observe isLoading LiveData to show/hide the progress bar
        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.setLoadingState(true)

        //Handling the doctor data fetching and local storage
        if (viewModel.isNetworkAvailable(applicationContext)) {
            viewModel.loadDoctorData() // Fetch from Firebase and update local storage
        } else {
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show()

            viewModel.setLoadingState(false)
            // Fetch data from Room and update adapter
            CoroutineScope(Dispatchers.IO).launch {
                val localDoctors = viewModel.getDoctorsFromLocal()
                withContext(Dispatchers.Main) {
                    adapter.updateList(localDoctors)
                }
            }
        }

        // Observe Doctor Data
        viewModel.docData.observe(this) { doctors ->
            Log.d("AddDoctorActivity", "Received data: $doctors")
            adapter.updateList(doctors)

            if (doctors.isEmpty()) {
                binding.noDoctorText.visibility = View.VISIBLE
                binding.recDocView.visibility = View.GONE
            } else {
                binding.noDoctorText.visibility = View.GONE
                binding.recDocView.visibility = View.VISIBLE
            }
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
            val name = docName.text?.toString()?.trim()
                ?.split(" ")
                ?.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                } ?: ""

            val speciality = docSpec.text?.toString()?.trim()
                ?.split(" ")
                ?.joinToString(" ") { word ->
                    word.lowercase().replaceFirstChar { it.uppercase() }
                } ?: ""

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

    private fun setupSwipeToRefresh() {
        // Set up the refresh listener for SwipeRefreshLayout
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Trigger data reload
            refreshDoctorData()
        }
    }

    private fun refreshDoctorData() {
        if (viewModel.isNetworkAvailable(applicationContext)) {
            viewModel.loadDoctorData()
        } else {
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show()
        }

        // Stop the refreshing animation after data reload
        binding.swipeRefreshLayout.isRefreshing = false
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
            }

            R.id.nav_about -> {
                val intent = Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_presentation -> {
                val intent = Intent(this, All_Presentation::class.java)
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
        binding.navView.menu.findItem(item.itemId).isChecked = false

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
