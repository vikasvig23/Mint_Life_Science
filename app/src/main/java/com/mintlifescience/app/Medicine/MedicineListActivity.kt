package com.mintlifescience.app.Medicine

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.mintlifescience.app.adapters.MedicineAdapter
import com.mintlifescience.app.addDoctor.DoctorData
import com.mintlifescience.app.databinding.ActivityMedicineListBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.model.Medicine
import android.content.Intent

class MedicineListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineListBinding
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var brandName: String
    private lateinit var doctorName: String
    private var medicineList: List<Medicine> = emptyList()
    private var selectedMedicines: MutableList<Medicine> = mutableListOf()
    private var alreadySelectedMedicine: MutableList<Medicine> = mutableListOf()
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        brandName = intent.getStringExtra(AppConstants.IntentKeys.BRAND_NAME_SHORT) ?: ""
        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""

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

        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        medicineAdapter = MedicineAdapter(this, medicineList, selectedMedicines, alreadySelectedMedicine)
        binding.recyclerView.adapter = medicineAdapter

        // Fetch already-selected medicines first, then load catalog so selection state is correct
        fetchDoctorMedicines {
            fetchMedicines()
        }

        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false
            override fun onQueryTextChange(newText: String?): Boolean {
                filterMedicines(newText ?: "")
                return true
            }
        })

        binding.done.setOnClickListener {
            if (selectedMedicines != alreadySelectedMedicine) saveDoctorData()
            finish()
        }
    }

    private fun fetchMedicines() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(com.mintlifescience.app.R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        FirebaseDatabase.getInstance()
            .getReference(FirebaseConstants.ADMIN).child(brandName)
            .get()
            .addOnSuccessListener { snapshot ->
                val newList = mutableListOf<Medicine>()
                for (medicineSnapshot in snapshot.children) {
                    medicineSnapshot.getValue(Medicine::class.java)?.let { newList.add(it) }
                }
                medicineList = newList
                Log.d("MedicineListActivity", "Fetched ${medicineList.size} medicines for brand: $brandName")
                medicineAdapter.updateMedicineList(medicineList)
                updateCartVisibility()
                dialog.dismiss()
            }
            .addOnFailureListener { e ->
                dialog.dismiss()
                Log.e("MedicineListActivity", "Failed to fetch medicines from path: ${FirebaseConstants.ADMIN}/$brandName", e)
                Toast.makeText(this, "Failed to fetch medicines", Toast.LENGTH_SHORT).show()
                updateCartVisibility()
            }
    }

    private fun updateCartVisibility() {
        binding.cartEmptyText.visibility = if (medicineList.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (medicineList.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun filterMedicines(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) medicineList
        else medicineList.filter { it.name?.contains(trimmed, ignoreCase = true) == true }
        medicineAdapter.updateMedicineList(filtered)
    }

    private fun saveDoctorData() {
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(userId).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctorName)
            .child(FirebaseConstants.MEDICINES).child(brandName)
            .setValue(selectedMedicines)
            .addOnSuccessListener { selectedMedicines.clear() }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to save doctor data", e)
                Toast.makeText(this, "Failed to save medicines", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchDoctorMedicines(onComplete: () -> Unit) {
        FirebaseDatabase.getInstance().getReference(FirebaseConstants.USERS)
            .child(userId).child(FirebaseConstants.CLIENT)
            .child(FirebaseConstants.DOCTORS).child(doctorName)
            .get()
            .addOnSuccessListener { snapshot ->
                alreadySelectedMedicine.clear()
                snapshot.child(FirebaseConstants.MEDICINES).child(brandName).children
                    .mapNotNull { it.getValue(Medicine::class.java) }
                    .let { alreadySelectedMedicine.addAll(it) }
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to fetch doctor medicines", e)
                onComplete() // still load catalog even if pre-selection fails
            }
    }
}
