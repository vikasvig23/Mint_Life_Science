package com.mintlifescience.app.medicinePresentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.mintlifescience.app.addDoctor.AddDoctorActivity
import com.mintlifescience.app.allPresentation.All_Presentation
import com.mintlifescience.app.database.DoctorDao
import com.mintlifescience.app.database.DoctorDatabase
import com.mintlifescience.app.database.DoctorRepository
import com.mintlifescience.app.database.toEntity
import com.mintlifescience.app.databinding.ActivityMedicineScreenBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.NetworkUtils
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.homescreen.HomeActivity
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.model.FeedbackData
import com.mintlifescience.app.model.Medicine
import com.mintlifescience.app.recentDoctors.RecentDoctorsActivity
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.mintlifescience.app.R

class MedicineScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineScreenBinding
    private lateinit var adapter: PresentationAdapter
    private val items = mutableListOf<Medicine>()
    private lateinit var userId: String
    private lateinit var doctorName: String
    private lateinit var db: DatabaseReference
    private lateinit var medicineScreenViewModel: MedicineScreenViewModel
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var doctorDao: DoctorDao
    private lateinit var repository: DoctorRepository
    private var recentDoctor = false
    private var allPresentation = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        doctorDao = DoctorDatabase.getDatabase(application).doctorDao()
        repository = DoctorRepository(doctorDao)

        recentDoctor = intent.getBooleanExtra(AppConstants.IntentKeys.RECENT_DOCTOR, false)
        allPresentation = intent.getBooleanExtra(AppConstants.IntentKeys.ALL_PRESENTATION, false)
        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        medicineScreenViewModel = ViewModelProvider(this)[MedicineScreenViewModel::class.java]

        userId = PrefsManager.userId(this) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            loginViewModel.navigateToLogin.observe(this) {
                startActivity(Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                finish()
            }
            loginViewModel.logout()
            return
        }

        db = FirebaseDatabase.getInstance()
            .getReference(FirebaseConstants.doctorPath(userId, doctorName))
        medicineScreenViewModel.setDoctorReference(db)

        // Observe successful submission → finish
        medicineScreenViewModel.downloadStatus.observe(this) { status ->
            if (status == "Update successful") {
                Toast.makeText(this, "Presentation submitted!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // Restore UI when feedback fragment is dismissed without submitting
        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.viewPager.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.dimOverlay.visibility = View.GONE
                binding.fragmentContainer.visibility = View.GONE
            }
        }

        binding.backArrow.setOnClickListener {
            startActivity(Intent(this, when {
                recentDoctor -> RecentDoctorsActivity::class.java
                allPresentation -> All_Presentation::class.java
                else -> AddDoctorActivity::class.java
            }))
            finish()
        }

        adapter = PresentationAdapter(items, binding.viewPager, this)
        binding.viewPager.offscreenPageLimit = 2
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        binding.viewPager.adapter = adapter

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateCounter(position)
                adapter.notifyItemChanged(position)
            }
        })

        // Submit always opens FeedbackFragment (pre-filled if already set)
        binding.submitPresentation.setOnClickListener {
            showFeedbackFragment()
        }

        fetchDoctorDetails()
        fetchDoctorData()
    }

    private fun showFeedbackFragment() {
        val selectedDate = medicineScreenViewModel.selectedDate.ifEmpty {
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        }
        binding.dimOverlay.visibility = View.VISIBLE
        binding.fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container,
                FeedbackFragment.newInstance(medicineScreenViewModel.feedbackText, selectedDate))
            .addToBackStack(null)
            .commit()
    }

    private fun updateCounter(position: Int) {
        binding.toolbarTitle.text = if (items.isEmpty()) "Presentation"
        else "Medicine ${position + 1} / ${items.size}"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presn_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_share_presentation -> {
                if (items.isEmpty()) {
                    Toast.makeText(this, "No medicines to share", Toast.LENGTH_SHORT).show()
                } else {
                    sharePresentationAsPdf()
                }
                true
            }
            R.id.action_edit_feedback -> {
                showFeedbackFragment()
                true
            }
            R.id.action_update_medicine -> {
                db.child(FirebaseConstants.HAVE_PRESENTATION).setValue(false)
                startActivity(Intent(this, HomeActivity::class.java)
                    .putExtra(AppConstants.IntentKeys.DOCTOR_NAME, doctorName))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sharePresentationAsPdf() {
        binding.progressBar.visibility = View.VISIBLE
        PresentationPdfBuilder.buildAndShare(
            context = this,
            medicines = items.toList(),
            doctorName = doctorName,
            scope = lifecycleScope
        ) { status ->
            if (status.isEmpty()) {
                binding.progressBar.visibility = View.GONE
            } else if (status.startsWith("Failed")) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, status, Toast.LENGTH_LONG).show()
            } else {
                // "Building page X of Y…" — keep spinner showing
                Toast.makeText(this, status, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchDoctorData() {
        binding.progressBar.visibility = View.VISIBLE

        if (NetworkUtils.isAvailable(this)) {
            FirebaseDatabase.getInstance()
                .getReference(FirebaseConstants.doctorPath(userId, doctorName))
                .child(FirebaseConstants.MEDICINES)
                .get()
                .addOnSuccessListener { snapshot ->
                    binding.progressBar.visibility = View.GONE
                    if (!snapshot.exists()) {
                        Toast.makeText(this, "No medicines found", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val fetched = mutableListOf<Medicine>()
                    for (brand in snapshot.children) {
                        for (med in brand.children) {
                            med.getValue(Medicine::class.java)?.let { fetched.add(it) }
                        }
                    }

                    items.clear()
                    items.addAll(fetched)
                    adapter.notifyDataSetChanged()
                    updateCounter(0)

                    lifecycleScope.launch(Dispatchers.IO) {
                        repository.deleteMedicinesForDoctor(doctorName)
                        repository.saveMedicinesForDoctor(doctorName, fetched.map { it.toEntity(doctorName) })
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Log.e("MedicineScreenActivity", "Failed to fetch medicines", e)
                    Toast.makeText(this, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show()
                }
        } else {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val cached = repository.getMedicinesForDoctor(doctorName)
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        if (cached.isEmpty()) {
                            Toast.makeText(this@MedicineScreenActivity,
                                "No medicines found in local storage", Toast.LENGTH_SHORT).show()
                        } else {
                            items.clear()
                            items.addAll(cached)
                            adapter.notifyDataSetChanged()
                            updateCounter(0)
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@MedicineScreenActivity,
                            "Error loading offline data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun fetchDoctorDetails() {
        db.child(FirebaseConstants.FEEDBACK).orderByKey().limitToLast(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    snapshot.children.firstOrNull()
                        ?.getValue(FeedbackData::class.java)
                        ?.let { medicineScreenViewModel.feedbackText = it.message ?: "" }
                }
            }
            .addOnFailureListener { Log.e("MedicineScreenActivity", "Failed to fetch feedback", it) }

        db.child(FirebaseConstants.SCHEDULE_MEET).get()
            .addOnSuccessListener { snapshot ->
                medicineScreenViewModel.selectedDate = snapshot.getValue(String::class.java) ?: ""
            }
            .addOnFailureListener { Log.e("MedicineScreenActivity", "Failed to fetch schedule", it) }
    }
}
