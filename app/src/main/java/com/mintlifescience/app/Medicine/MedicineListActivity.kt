package com.mintlifescience.app.Medicine

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.FirebaseDatabase
import com.mintlifescience.app.adapters.MedicineAdapter
import com.mintlifescience.app.databinding.ActivityMedicineListBinding
import com.mintlifescience.app.helperUtils.AppConstants
import com.mintlifescience.app.helperUtils.FirebaseConstants
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.login.LoginActivity
import com.mintlifescience.app.login.LoginViewModel
import com.mintlifescience.app.model.Medicine

class MedicineListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMedicineListBinding
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var brandName: String
    private lateinit var doctorName: String
    private var medicineList: List<Medicine> = emptyList()
    private val selectedMedicines: MutableList<Medicine> = mutableListOf()
    private val alreadySelectedMedicine: MutableList<Medicine> = mutableListOf()
    private lateinit var userId: String
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // Fix: observe BEFORE the null-userId early return so logout always navigates correctly
        loginViewModel.navigateToLogin.observe(this) {
            startActivity(
                Intent(this, LoginActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }

        brandName  = intent.getStringExtra(AppConstants.IntentKeys.BRAND_NAME_SHORT) ?: ""
        doctorName = intent.getStringExtra(AppConstants.IntentKeys.DOCTOR_NAME) ?: ""

        userId = PrefsManager.userId(this) ?: run {
            Toast.makeText(this, "User ID not found. Please log in again.", Toast.LENGTH_LONG).show()
            loginViewModel.logout()
            return
        }

        // Toolbar
        binding.toolbarSubtitle.text = "$brandName · Dr. $doctorName"
        binding.backArrow.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finish() }
        })

        // RecyclerView — 2-column grid
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        medicineAdapter = MedicineAdapter(
            context = this,
            dataList = medicineList,
            selectedMedicines = selectedMedicines,
            alreadySelectedMedicine = alreadySelectedMedicine,
            onSelectionChanged = { count -> updateFabText(count) }
        )
        binding.recyclerView.adapter = medicineAdapter

        // Fetch pre-selected medicines first, then load the full catalogue
        fetchDoctorMedicines {
            fetchMedicines()
        }

        // Search — TextWatcher on the TextInputEditText
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMedicines(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        // Save / done FAB
        binding.done.setOnClickListener {
            if (selectedMedicines != alreadySelectedMedicine) saveDoctorData()
            finish()
        }
    }

    // ── FAB label ──────────────────────────────────────────────────────────────

    private fun updateFabText(count: Int) {
        binding.done.text = if (count > 0) "Save Selection ($count)" else "Save Selection"
    }

    // ── Data loading ───────────────────────────────────────────────────────────

    private fun fetchMedicines() {
        showLoading(true)

        FirebaseDatabase.getInstance()
            .getReference(FirebaseConstants.ADMIN).child(brandName)
            .get()
            .addOnSuccessListener { snapshot ->
                val newList = mutableListOf<Medicine>()
                for (child in snapshot.children) {
                    child.getValue(Medicine::class.java)?.let { newList.add(it) }
                }
                medicineList = newList
                Log.d("MedicineListActivity", "Fetched ${medicineList.size} medicines for brand: $brandName")
                medicineAdapter.updateMedicineList(medicineList)
                showLoading(false)
                updateEmptyState(medicineList.isEmpty())
            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to fetch medicines: ${FirebaseConstants.ADMIN}/$brandName", e)
                Toast.makeText(this, "Failed to fetch medicines", Toast.LENGTH_SHORT).show()
                showLoading(false)
                updateEmptyState(medicineList.isEmpty())
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
                Log.e("MedicineListActivity", "Failed to fetch pre-selected medicines", e)
                onComplete() // still load catalogue even if pre-selection fails
            }
    }

    // ── Save ───────────────────────────────────────────────────────────────────

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

    // ── UI helpers ─────────────────────────────────────────────────────────────

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.recyclerView.visibility = View.GONE
            binding.emptyState.visibility   = View.GONE
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility   = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE    else View.VISIBLE
    }

    private fun filterMedicines(query: String) {
        val trimmed = query.trim()
        val filtered = if (trimmed.isEmpty()) medicineList
        else medicineList.filter { it.name?.contains(trimmed, ignoreCase = true) == true }
        medicineAdapter.updateMedicineList(filtered)
        updateEmptyState(filtered.isEmpty())
    }
}
