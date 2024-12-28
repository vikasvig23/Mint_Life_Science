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
import com.example.mintlifesciences.databinding.ActivityMedicineScreenBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.login.LoginViewModel
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
    private lateinit var selectedDate : String
    private  var recentDoctor : Boolean = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        recentDoctor = intent.getBooleanExtra("recentDoctor", false)
        doctorName = intent.getStringExtra("doctorName") ?: ""

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        medicineScreenViewModel = ViewModelProvider(this).get(MedicineScreenViewModel::class.java)

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                // Restore views when fragment is removed
                binding.viewPager.visibility = View.VISIBLE
                binding.toolbar.visibility = View.VISIBLE
                binding.dimOverlay.visibility = View.GONE
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
            }else {
                val intent = Intent(this, AddDoctorActivity::class.java)
                startActivity(intent)
            }
        }

        fetchDoctorData()
        fetchDoctorDetails()


        adapter = PresentationAdapter(items, binding.viewPager, this)
        binding.viewPager.adapter = adapter
        binding.viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
    }


    private fun fetchDoctorData() {
        binding.progressBar.visibility = View.VISIBLE

        val dbs = db.child("medicines")
        dbs.get().addOnSuccessListener { dataSnapshot ->
            binding.progressBar.visibility = View.GONE
            if (dataSnapshot.exists()) {
                items.clear()
                for (brandSnapshot in dataSnapshot.children) {
                    for (medicineSnapshot in brandSnapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        medicine?.videoUrl =
                            medicineSnapshot.child("videoUrl").getValue(String::class.java) ?: ""
                        if (medicine != null && medicine.videoUrl!!.isNotEmpty()) {
                            items.add(medicine)
                        } else {
                            Log.e(
                                "MedicineScreenActivity",
                                "Media URL is null or empty for ${medicineSnapshot.key}"
                            )
                        }
                    }
                }
                binding.viewPager.adapter?.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No medicines found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            binding.progressBar.visibility = View.GONE
            Log.e("MedicineScreenActivity", "Failed to fetch doctor data", e)
            Toast.makeText(this, "Failed to fetch data. Please try again.", Toast.LENGTH_SHORT).show()
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

                if (selectedDate.isEmpty()) {
                    selectedDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                }

                val fragment = FeedbackFragment.newInstance(feedback, selectedDate)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit()

                true
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

//    private fun fetchDoctorDetails() {
//        db.child("feedback").get().addOnSuccessListener { feedbackSnapshot ->
//            feedback = feedbackSnapshot.getValue(String::class.java) ?: "No feedback available"
//
//            db.child("scheduleMeet").get().addOnSuccessListener { dateSnapshot ->
//                selectedDate = dateSnapshot.getValue(String::class.java) ?: ""
//
//            }.addOnFailureListener { e ->
//                Log.e("MedicineScreenActivity", "Failed to fetch schedule date", e)
//            }
//        }.addOnFailureListener { e ->
//            Log.e("MedicineScreenActivity", "Failed to fetch feedback", e)
//        }
//    }

    private fun fetchDoctorDetails() {
        db.child("feedback").get().addOnSuccessListener { feedbackSnapshot ->
            feedback = feedbackSnapshot.getValue(String::class.java) ?: ""

            if (feedback.isNotEmpty() && feedback != "") {
                //   binding.response.setText(feedback)
            } else {
                //  binding.response.setText("")
            }

            db.child("scheduleMeet").get().addOnSuccessListener { dateSnapshot ->
                selectedDate = dateSnapshot.getValue(String::class.java) ?: ""
            }.addOnFailureListener { e ->
                Log.e("MedicineScreenActivity", "Failed to fetch schedule date", e)
            }
        }.addOnFailureListener { e ->
            Log.e("MedicineScreenActivity", "Failed to fetch feedback", e)
        }
    }

}
