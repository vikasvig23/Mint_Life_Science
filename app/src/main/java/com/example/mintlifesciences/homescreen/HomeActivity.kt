package com.example.mintlifesciences.homescreen

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.medicinePresentation.MedicineScreenActivity
import com.example.mintlifesciences.model.BrandItem
import com.example.mintlifesciences.model.Medicine
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeActivity : AppCompatActivity(){

    lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter
    private lateinit var doctorName: String
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel
    private val allMedicines = mutableListOf<Pair<Medicine, String>>()
    private lateinit var suggestionAdapter: MedicineSuggestionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        // Retrieve doctorName and brandName from the Intent
        doctorName = intent.getStringExtra("doctorName") ?: ""

        // Initialize SharedPreferences inside onCreate
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        userId = sharedPreferences.getString("userId", null) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG)
                .show()

            loginViewModel.logout()
            return
        }

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the current activity
            }
        })


        adapter = HomeAdapter(emptyList()) { item ->
            navigateToNextScreen(item)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter



        viewModel.items.observe(this, Observer { items ->
            updateUI(items)
        })

        binding.menu.setOnClickListener {
            openMenu()
        }

//
//        setupSearchView()
//
//        fetchAllMedicines() // Fetch all medicines from Firebase
    }

    private fun updateUI(items: List<BrandItem>) {
        items?.let {
            adapter.updateItems(it)
        }
    }

    private fun navigateToNextScreen(item: String) {
        if (viewModel.isNetworkAvailable(getApplication().applicationContext)){
        val intent = Intent(this, DoctorMedicineActivity::class.java)
        intent.putExtra("doctorName",doctorName)
        intent.putExtra("brandName",item)
        startActivity(intent)
    }
        else {
            Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
        }
    }

    private fun openMenu() {
        val popupMenu = PopupMenu(this, binding.menu)
        menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.make_presentation -> {
                    // Set isPresentation to true for the doctor
                    viewModel.updateDoctorPresentationStatus(doctorName, true)

                    // Navigate to MedicineScreenActivity
                    val intent = Intent(this, MedicineScreenActivity::class.java)
                    intent.putExtra("doctorName", doctorName)
                    intent.putExtra("isPresentation", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupSearchView() {
        suggestionAdapter = MedicineSuggestionAdapter(emptyList()) { medicine, brandName ->
            addSelectedMedicineToFirebase(medicine, brandName)
        }

        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchMedicine(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { searchMedicine(it) }
                return false
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = suggestionAdapter
        }
    }

    private fun fetchAllMedicines() {
        val databaseRef = FirebaseDatabase.getInstance()
            .getReference("Mint_Life_Science_Admin")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                allMedicines.clear()
                for (brandSnapshot in snapshot.children) {
                    val brandName = brandSnapshot.key ?: continue
                    for (medicineSnapshot in brandSnapshot.children) {
                        val medicine = medicineSnapshot.getValue(Medicine::class.java)
                        if (medicine != null) {
                            allMedicines.add(Pair(medicine, brandName))
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HomeActivity, "Failed to load medicines", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun searchMedicine(query: String) {
        val filteredList = allMedicines.filter { it.first.name?.contains(query, true) ?: false }
        suggestionAdapter.updateList(filteredList)
    }

    private fun addSelectedMedicineToFirebase(selectedMedicine: Medicine, brandName: String) {
        val databaseReference = FirebaseDatabase.getInstance()
            .getReference("Users")
            .child("userId") // Replace with actual user ID
            .child("Mint_Life_Science_Client")
            .child("Doctors")
            .child("doctorName") // Replace with actual doctor name
            .child("medicines")
            .child(brandName)

        databaseReference.push().setValue(selectedMedicine)
            .addOnSuccessListener {
                Toast.makeText(this, "Medicine Added Successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Add Medicine", Toast.LENGTH_SHORT).show()
            }
    }


}







//dead code for obeserving new brand added
//        binding.swipeRefreshLayout.setOnRefreshListener {
//            viewModel.refreshData()
//            Handler().postDelayed({
//                binding.swipeRefreshLayout.isRefreshing = false
//            }, 5000)
//        }

//        viewModel.loading.observe(this, Observer { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            if (!isLoading) {
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//        })
//
//        viewModel.error.observe(this, Observer { errorMessage ->
//            errorMessage?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//                viewModel.errorHandled()
//            }
//        })


