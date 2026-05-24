package com.mintlifescience.app.addDoctor

import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.mintlifescience.app.R
import com.mintlifescience.app.Utils.NetworkChangeReceiver
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

    private var allDoctors: List<DoctorData> = emptyList()
    private var searchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_doctor)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel = ViewModelProvider(this)[AddDoctorViewModel::class.java]
        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.recDocView.layoutManager = LinearLayoutManager(this)
        adapter = AddDoctorAdapter(this, emptyList(), viewModel)
        binding.recDocView.adapter = adapter

        setupSwipeToDelete()
        setupSearch()
        setupSwipeToRefresh()
        setupBottomNav()

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
                withContext(Dispatchers.Main) { applyDoctorList(localDoctors) }
            }
        }

        viewModel.docData.observe(this) { doctors ->
            allDoctors = doctors
            applyFilter()
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

    private fun applyDoctorList(list: List<DoctorData>) {
        allDoctors = list
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (searchQuery.isBlank()) allDoctors
        else allDoctors.filter {
            it.docName.contains(searchQuery, ignoreCase = true) ||
                    it.docSpeciality.contains(searchQuery, ignoreCase = true)
        }
        adapter.updateList(filtered)
        val isEmpty = filtered.isEmpty()
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recDocView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                searchQuery = s?.toString() ?: ""
                applyFilter()
            }
        })
    }

    private fun setupSwipeToDelete() {
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.baseline_delete_24)
        val redPaint = Paint().apply { color = Color.parseColor("#C62828") }

        val callback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                if (pos == RecyclerView.NO_POSITION) return
                val deleted = adapter.docList[pos]
                adapter.removeItem(pos)

                Snackbar.make(binding.root, "${deleted.docName} removed", Snackbar.LENGTH_LONG)
                    .setAction("Undo") { adapter.restoreItem(deleted, pos) }
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(snackbar: Snackbar, event: Int) {
                            if (event != DISMISS_EVENT_ACTION) {
                                viewModel.deleteDoctor(deleted.docName)
                            }
                        }
                    })
                    .show()
            }

            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
                                     dX: Float, dY: Float, actionState: Int, isActive: Boolean) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val item = vh.itemView
                    c.drawRect(item.right + dX, item.top.toFloat(),
                        item.right.toFloat(), item.bottom.toFloat(), redPaint)
                    deleteIcon?.let { icon ->
                        icon.setTint(Color.WHITE)
                        val iconMargin = (item.height - icon.intrinsicHeight) / 2
                        val top = item.top + iconMargin
                        val bottom = item.bottom - iconMargin
                        val right = item.right - iconMargin
                        val left = right - icon.intrinsicWidth
                        icon.setBounds(left, top, right, bottom)
                        icon.draw(c)
                    }
                }
                super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive)
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.recDocView)
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

    private fun setupBottomNav() {
        binding.bottomNav.selectedItemId = R.id.bnav_doctors
        attachBottomNavListener()
    }

    private fun attachBottomNavListener() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == R.id.bnav_doctors) return@setOnItemSelectedListener true
            val opts = ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle()
            when (item.itemId) {
                R.id.bnav_recent -> startActivity(
                    Intent(this, RecentDoctorsActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), opts)
                R.id.bnav_presentations -> startActivity(
                    Intent(this, All_Presentation::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT), opts)
            }
            false // keep this screen's tab highlighted while navigating away
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-affirm correct tab when brought to front via REORDER_TO_FRONT
        binding.bottomNav.setOnItemSelectedListener(null)
        binding.bottomNav.selectedItemId = R.id.bnav_doctors
        attachBottomNavListener()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_about -> startActivity(Intent(this, AboutUsActivity::class.java))
            R.id.nav_privacyPolicy -> openPrivacyPolicy()
            R.id.nav_logout -> loginViewModel.logout()
        }
        binding.navView.menu.findItem(item.itemId)?.isChecked = false
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
