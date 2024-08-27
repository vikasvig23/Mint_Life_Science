package com.example.mintlifesciences.doctorMedicine

import DocMedicineViewModel
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityMedicineListBinding
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.Adapters.MedicineAdapter
import com.example.mintlifesciences.Model.Medicine
import com.google.firebase.database.*

class MedicineListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicineListBinding

    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var database: DatabaseReference

    private lateinit var brandName : String


    private val viewModel: DocMedicineViewModel by viewModels()

    private var medicineList: MutableList<Medicine> = mutableListOf()
    private var selectedMedicines: MutableList<Medicine> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        medicineAdapter = MedicineAdapter(this, medicineList, selectedMedicines)
        binding.recyclerView.adapter = medicineAdapter

        brandName = intent.getStringExtra("brand_name") ?: ""

        // Show progress dialog
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("brands").child(brandName)

        // Fetch medicines from Firebase
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                medicineList.clear()
                for (medicineSnapshot in snapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        medicineList.add(medicine)
                    }
                }
                medicineAdapter.updateMedicineList(medicineList)
                dialog.dismiss() // Dismiss the progress dialog once data is loaded
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss() // Dismiss the progress dialog in case of error
            }
        })

        // Search functionality
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterMedicines(newText ?: "")
                return true
            }
        })

        binding.done.setOnClickListener {
            finish()
        }

    }

    private fun filterMedicines(query: String) {
        val trimmedQuery = query.trim()
        val filteredList = if (trimmedQuery.isEmpty()) {
            medicineList  // Show the full list if the query is empty or just spaces
        } else {
            medicineList.filter {
                it.name?.contains(trimmedQuery, ignoreCase = true) ?: false
            }
        }
        medicineAdapter.updateMedicineList(filteredList)
    }
}
