package com.example.mintlifesciences.doctorMedicine

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.adapters.MedicineAdapter
import com.example.mintlifesciences.model.Medicine
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityMedicineListBinding
import com.google.firebase.database.*

class MedicineListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMedicineListBinding
    private lateinit var medicineAdapter: MedicineAdapter
    private lateinit var database: DatabaseReference

    private lateinit var brandName: String
    private lateinit var doctorName: String

    private var medicineList: MutableList<Medicine> = mutableListOf()
    private var selectedMedicines: MutableList<Medicine> = mutableListOf()
    private var alreadySelectedMedicine: MutableList<Medicine> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMedicineListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        medicineAdapter =
            MedicineAdapter(this, medicineList, selectedMedicines, alreadySelectedMedicine)
        binding.recyclerView.adapter = medicineAdapter

        brandName = intent.getStringExtra("brand_name") ?: ""
        doctorName = intent.getStringExtra("doctorName") ?: ""

        fetchDoctorMedicines()
        fetchMedicines()


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
            if (selectedMedicines != alreadySelectedMedicine) {
                saveDoctorData()
            }
            finish()
        }
    }

    ///FUNCTION TO FETCH MEDICINE
    private fun fetchMedicines() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
        builder.setView(R.layout.progress_layout)
        val dialog = builder.create()
        dialog.show()

        // Initialize Firebase Database
        database =
            FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Admin").child(brandName)

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
                dialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                dialog.dismiss()
            }
        })
    }

    ///FUNCTION TO FILTER MEDICINE
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


    ///FUNCTION TO SAVE DOCTOR DATA
    private fun saveDoctorData() {
        val databaseReference =
            FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
        databaseReference.child(brandName).child("Doctors").child(doctorName).child("medicines")
            .setValue(selectedMedicines)
            .addOnSuccessListener {
                Log.d("MedicineListActivity", "Doctor data saved successfully!")


                saveInRecentDoctors()
                //empty the Selected Medicines
                selectedMedicines.clear()

            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to save doctor data", e)
            }
    }

    private fun saveInRecentDoctors() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("RecentDoctors")

        // Save the selected medicines under the doctor's name
        databaseReference.child(doctorName).child("medicines")
            .setValue(selectedMedicines)
            .addOnSuccessListener {
                Log.d("MedicineListActivity", "Doctor data saved to RecentDoctors successfully!")

                // Check if the number of children exceeds 20
                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Get the list of children (doctors) in RecentDoctors
                        val children = snapshot.children.toMutableList()
                        if (children.size > 20) {
                            // Find and remove the oldest doctor (first child)
                            val oldestChild = children.firstOrNull()
                            oldestChild?.key?.let { oldestKey ->
                                databaseReference.child(oldestKey).removeValue()
                                    .addOnSuccessListener {
                                        Log.d(
                                            "MedicineListActivity",
                                            "Oldest doctor removed from RecentDoctors"
                                        )
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e(
                                            "MedicineListActivity",
                                            "Failed to remove oldest doctor",
                                            e
                                        )
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("MedicineListActivity", "Failed to retrieve data: ${error.message}")
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e("MedicineListActivity", "Failed to save doctor data to RecentDoctors", e)
            }
    }


    ///FUNCTION TO FETCH DOCTOR MEDICINES
    private fun fetchDoctorMedicines() {
        // Fetch doctor details from Firebase
        val db = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
            .child(brandName).child("Doctors").child(doctorName)

        db.get().addOnSuccessListener { dataSnapshot ->
            val doctorData = dataSnapshot.getValue(DoctorData::class.java)
            if (doctorData != null) {
                // Fetch medicines from the "Medicines" node
                val medicinesSnapshot = dataSnapshot.child("medicines")

                for (medicineSnapshot in medicinesSnapshot.children) {
                    val medicine = medicineSnapshot.getValue(Medicine::class.java)
                    if (medicine != null) {
                        alreadySelectedMedicine.add(medicine)
                    }
                }
            } else {
                Toast.makeText(this, "No doctor data found", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Log.e("DoctorMedicineActivity", "Failed to fetch doctor data", e)
        }
    }
}
