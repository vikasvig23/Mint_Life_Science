package com.example.mintlifesciences.medicinePresentation

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.allPresentation.All_Presentation
import com.example.mintlifesciences.databinding.ActivityMedicineScreenBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.model.FeedbackData
import com.example.mintlifesciences.model.Medicine
import com.example.mintlifesciences.recentDoctors.RecentDoctorsActivity
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicineScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineScreenBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PresentationAdapter
    private val items = mutableListOf<Medicine>()
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var doctorName : String
    private lateinit var db : DatabaseReference
    private lateinit var medicineScreenViewModel: MedicineScreenViewModel
    private lateinit var feedback:String
    private  var recentDoctor : Boolean = false
    private  var allPresentation : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recentDoctor = intent.getBooleanExtra("recentDoctor", false)
        allPresentation = intent.getBooleanExtra("allPresentation", false)
        doctorName = intent.getStringExtra("doctorName") ?: ""

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        medicineScreenViewModel = ViewModelProvider(this).get(MedicineScreenViewModel::class.java)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // Restore views when fragment is removed
                binding.viewPager.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.dimOverlay.visibility = View.GONE
                binding.fragmentContainer.visibility = View.GONE
            }
        }

        // Initialize SharedPreferences inside onCreate
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG)
                .show()

            loginViewModel.logout()
            return
        }

        db = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client").child("Doctors")
            .child(doctorName)

        // Set the Firebase reference in the ViewModel
        medicineScreenViewModel.setDoctorReference(db)

        binding.backArrow.setOnClickListener {
            if(recentDoctor){
                val intent = Intent(this, RecentDoctorsActivity::class.java)
                startActivity(intent)
            }else if(allPresentation) {
                val intent = Intent(this, All_Presentation::class.java)
                startActivity(intent)
            }
            else{
                val intent = Intent(this, AddDoctorActivity::class.java)
                startActivity(intent)
            }
            finish()
        }

        fetchDoctorData()
        fetchDoctorDetails()

        adapter = PresentationAdapter(items, binding.viewPager, this)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL


        //Handle the submit
        binding.submitPresentation.setOnClickListener {
            binding.dimOverlay.visibility = View.VISIBLE

            var selectedDate = medicineScreenViewModel.selectedDate

            if(selectedDate.isEmpty()) {
                selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            }

            if (medicineScreenViewModel.feedbackText.isEmpty()) {
                Toast.makeText(this, "Please add the feedback.", Toast.LENGTH_SHORT).show()
                binding.dimOverlay.visibility = View.VISIBLE
                binding.fragmentContainer.visibility = View.VISIBLE

                // Launch FeedbackFragment to collect feedback
                val fragment = FeedbackFragment.newInstance(medicineScreenViewModel.feedbackText, selectedDate)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                // Navigate back to the previous activity and finish this one
                finish()
            }
        }
    }


    private fun fetchDoctorData() {
        // Show loading indicator while fetching data
        binding.progressBar.visibility = View.VISIBLE

        // The reference path should be corrected based on actual Firebase structure
        val dbs = FirebaseDatabase.getInstance().getReference("Users")
            .child(userId).child("Mint_Life_Science_Client").child("Doctors")
            .child(doctorName).child("medicines")

        dbs.get().addOnSuccessListener { dataSnapshot ->
            binding.progressBar.visibility = View.GONE  // Hide loading indicator

            if (dataSnapshot.exists()) {
                items.clear()

                // Iterate through each brand and add medicines to items
                for (brandSnapshot in dataSnapshot.children) {
                    for (medicineSnapshot in brandSnapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        if (medicine != null) {
                            items.add(medicine)
                        }
                    }
                }

                adapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@MedicineScreenActivity, "No medicines found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE  // Hide loading indicator
            Log.e("MedicineScreenActivity", "Failed to fetch doctor data", e)
            Toast.makeText(this@MedicineScreenActivity, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.presn_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_update_medicine -> {
                updateDoctorPresentationStatus()
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("doctorName", doctorName)
                startActivity(intent)
                finish()
                return true
            }

            R.id.action_edit_feedback -> {
                binding.dimOverlay.visibility = View.VISIBLE
                binding.fragmentContainer.visibility = View.VISIBLE

                var selectedDate = medicineScreenViewModel.selectedDate

                if(selectedDate.isEmpty()) {
                    selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                }

                val fragment = FeedbackFragment.newInstance(medicineScreenViewModel.feedbackText, selectedDate)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()

                return true
            }


        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateDoctorPresentationStatus() {
       db.child("havePresentation").setValue(false)
            .addOnSuccessListener {
                Toast.makeText(this, "Presentation status updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update presentation status", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchDoctorDetails() {
        val feedbackRef = db.child("feedback")

        feedbackRef.orderByKey().limitToLast(1).get().addOnSuccessListener { feedbackSnapshot ->
            if (feedbackSnapshot.exists()) {
                for (snapshot in feedbackSnapshot.children) {
                    val latestFeedback = snapshot.getValue(FeedbackData::class.java)
                    medicineScreenViewModel.feedbackText = latestFeedback?.message ?: ""
                }
            } else {
                medicineScreenViewModel.feedbackText = ""
            }

            // Proceed to fetch the schedule date
            db.child("scheduleMeet").get().addOnSuccessListener { dateSnapshot ->
                medicineScreenViewModel.selectedDate = dateSnapshot.getValue(String::class.java) ?: ""
            }.addOnFailureListener { e ->
                Log.e("MedicineScreenActivity", "Failed to fetch schedule date", e)
            }
        }.addOnFailureListener { e ->
            Log.e("MedicineScreenActivity", "Failed to fetch feedback", e)
        }
    }


}
