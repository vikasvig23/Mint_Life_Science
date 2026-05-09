package com.mintlifescience.app.addDoctor

import android.app.AlertDialog
import android.content.ActivityNotFoundException
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
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.mintlifescience.app.R
import com.mintlifescience.app.Utils.NetworkChangeReceiver
import com.mintlifescience.app.Utility
import com.mintlifescience.app.aboutUs.AboutUsActivity
import com.mintlifescience.app.allPresentation.All_Presentation
import com.mintlifescience.app.databinding.ActivityAddDoctorBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.NetworkUtils
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.recentDoctors.RecentDoctorsActivity
import com.mintlifescience.app.helperUtils.AppUtils
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

        viewModel = ViewModelProvider(this)[AddDoctorViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Style the Add button
        binding.btn.background = Utility.createGeadientDrawable(
            25f,
            ContextCompat.getColor(this, R.color.purple_500),
            ContextCompat.getColor(this, R.color.purple_500)
        )

        binding.recDocView.layoutManager = LinearLayoutManager(this)
        adapter = AddDoctorAdapter(this, emptyList(), viewModel)
        binding.recDocView.adapter = adapter

        setupSwipeToRefresh()

        networkChangeReceiver = NetworkChangeReceiver {
            viewModel.setLoadingState(true)
            viewModel.loadDoctorData()
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        ContextCompat.registerReceiver(this, networkChangeReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        viewModel.isLoading.observe(this) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        viewModel.setLoadingState(true)
        if (NetworkUtils.isAvailable(applicationContext)) {
            viewModel.loadDoctorData()
        } else {
            Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show()
            viewModel.setLoadingState(false)
            CoroutineScope(Dispatchers.IO).launch {
                val localDoctors = viewModel.getDoctorsFromLocal()
                withContext(Dispatchers.Main) { adapter.updateList(localDoctors) }
            }
        }

        viewModel.docData.observe(this) { doctors ->
            adapter.updateList(doctors)
            binding.noDoctorText.visibility = if (doctors.isEmpty()) View.VISIBLE else View.GONE
            binding.recDocView.visibility = if (doctors.isEmpty()) View.GONE else View.VISIBLE
        }

        loginViewModel.navigateToLogin.observe(this) {
            startActivity(Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            finish()
        }

        binding.btn.setOnClickListener { showDoctorDialog() }

        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        binding.navView.setNavigationItemSelectedListener(this)

        val versionName = AppUtils.getAppVersion(this)
        val versionTextView = binding.navView.findViewById<TextView>(R.id.nav_ver)
        versionTextView.text = "MintLifeSciences $versionName"

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

    private fun showDoctorDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null)
        val docName = dialogView.findViewById<TextInputEditText>(R.id.doc_edit)
        val docSpec = dialogView.findViewById<TextInputEditText>(R.id.spec_edit)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<AppCompatButton>(R.id.cnfrmBtn).setOnClickListener {
            val name = docName.text?.toString()?.trim()
                ?.split(" ")?.joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } } ?: ""
            val speciality = docSpec.text?.toString()?.trim()
                ?.split(" ")?.joinToString(" ") { it.lowercase().replaceFirstChar { c -> c.uppercase() } } ?: ""

            if (name.isEmpty() || speciality.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val doctor = DoctorData(name, speciality)
            viewModel.saveDoctorData(doctor)
            viewModel.addDoctor(doctor)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun setupSwipeToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (NetworkUtils.isAvailable(applicationContext)) {
                viewModel.loadDoctorData()
            } else {
                Toast.makeText(this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show()
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> startActivity(Intent(this, AddDoctorActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT))
            R.id.nav_doctors -> startActivity(Intent(this, RecentDoctorsActivity::class.java))
            R.id.nav_about -> startActivity(Intent(this, AboutUsActivity::class.java))
            R.id.nav_presentation -> startActivity(Intent(this, All_Presentation::class.java))
            R.id.nav_privacyPolicy -> openPrivacyPolicy()
            R.id.nav_logout -> loginViewModel.logout()
        }
        binding.navView.menu.findItem(item.itemId).isChecked = false
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
