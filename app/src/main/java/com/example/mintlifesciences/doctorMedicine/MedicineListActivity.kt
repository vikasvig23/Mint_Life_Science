package com.example.mintlifesciences.doctorMedicine

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.Adapters.MedicineAdapter
import com.example.mintlifesciences.Model.Medicine
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.DoctorData
import com.example.mintlifesciences.databinding.ActivityMedicineListBinding
import com.example.mintlifesciences.recentDoctors.RecentDoctorData
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
        database = FirebaseDatabase.getInstance().getReference("brands").child(brandName)

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
        val databaseReference =FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
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
        val databaseReference = FirebaseDatabase.getInstance().getReference("Mint_Life_Science_Client")
        val recentDoctorsRef = databaseReference.child("RecentDoctors")

        // Save the selected medicines under the doctor's name
        recentDoctorsRef.child(doctorName).child("medicines")
            .setValue(selectedMedicines)
            .addOnSuccessListener {
                Log.d("MedicineListActivity", "Doctor data saved to RecentDoctors successfully!")

                // Check if the number of children exceeds 20
                recentDoctorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        // Get the list of children (doctors) in RecentDoctors
                        val children = snapshot.children.toMutableList()
                        if (children.size > 20) {
                            // Find and remove the oldest doctor (first child)
                            val oldestChild = children.firstOrNull()
                            oldestChild?.key?.let { oldestKey ->
                                recentDoctorsRef.child(oldestKey).removeValue()
                                    .addOnSuccessListener {
                                        Log.d("MedicineListActivity", "Oldest doctor removed from RecentDoctors")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("MedicineListActivity", "Failed to remove oldest doctor", e)
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


//method to add sample medicine fast
//private fun addSampleMedicines() {
//
//    val imageUrl = "https://firebasestorage.googleapis.com/v0/b/mint-life-sciences-admin-app.appspot.com/o/Medicine%20Images%2F1767693582?alt=media&token=8454b9fd-48ed-4295-89c6-b5b5f8657445"
//
//    val criticalCareMedicines = listOf(
//        Medicine(
//            image = "https://firebasestorage.googleapis.com/v0/b/mint-life-sciences-admin-app.appspot.com/o/Medicine%20Images%2F1767693582?alt=media&token=8454b9fd-48ed-4295-89c6-b5b5f8657445",
//            name = "Bvrol 1000",
//            salt = "Methylprednisolone 1000mg",
//            description = "20ml vial",
//            uses = "Used for various medical conditions",
//            mrp = "1000"
//        ),
//        Medicine(
//            image = "https://firebasestorage.googleapis.com/v0/b/mint-life-sciences-admin-app.appspot.com/o/Medicine%20Images%2F1767693582?alt=media&token=8454b9fd-48ed-4295-89c6-b5b5f8657445",
//            name = "Bvrol 40",
//            salt = "Methylprednisolone 40mg",
//            description = "10ml vial",
//            uses = "Used for various medical conditions",
//            mrp = "400"
//        ),
//        // Continue with the rest of the medicines
//        Medicine(
//            image = "https://firebasestorage.googleapis.com/v0/b/mint-life-sciences-admin-app.appspot.com/o/Medicine%20Images%2F1767693582?alt=media&token=8454b9fd-48ed-4295-89c6-b5b5f8657445",
//            name = "Hepamint 25000",
//            salt = "Heparin 25000 Iu",
//            description = "4 Thousand IU",
//            uses = "Used for blood clot prevention",
//            mrp = "2500"
//        ),    Medicine(imageUrl, "Izocin 100", "Amikacin (100 Mg/ 2 Ml)", "Antibiotic injection", "Treats bacterial infections", "200"),
//        Medicine(imageUrl, "Izocin 250", "Amikacin 250 Mg / 2 Ml", "Antibiotic injection", "Treats bacterial infections", "400"),
//        Medicine(imageUrl, "Izocin 500", "Amikacin 500 Mg Inj / 2 Ml", "Antibiotic injection", "Treats bacterial infections", "800"),
//        Medicine(imageUrl, "Izodime 1gm", "Ceftazidime 1 Gm", "Antibiotic injection", "Treats bacterial infections", "1000"),
//        Medicine(imageUrl, "Izoflox Iv 500", "Levofloxacin 500 Mgiv", "Antibiotic injection", "Treats bacterial infections", "500"),
//        Medicine(imageUrl, "Izomet 500", "Metronidazole 500mg", "Antibiotic injection", "Treats bacterial infections", "250"),
//        Medicine(imageUrl, "Izone S 1.5", "Cefoperazone 1000 Mg + Sulbactum 500 Mg", "Antibiotic injection", "Treats bacterial infections", "1500"),
//        Medicine(imageUrl, "Izone S 2Gm", "Cefoperazone 1 Gm + Sulbactum 1 Gm", "Antibiotic injection", "Treats bacterial infections", "2000"),
//        Medicine(imageUrl, "Izopen C 1gm", "Imipenem 500mg + Cilastatin 500mg", "Antibiotic injection", "Treats bacterial infections", "1800"),
//        Medicine(imageUrl, "Izopime 1Gm", "Cefepime 1gm Mmg", "Antibiotic injection", "Treats bacterial infections", "1000"),
//        Medicine(imageUrl, "Izopime T 1.125", "Cefepime 1000 Mg + Tazobactum 125 Mg", "Antibiotic injection", "Treats bacterial infections", "1125"),
//        Medicine(imageUrl, "Izovox 600", "Linezolid 600 Mg", "Antibiotic injection", "Treats bacterial infections", "600"),
//        Medicine(imageUrl, "Levemin 500", "Levetiracetam 500mg", "Anticonvulsant injection", "Treats seizures", "500"),
//        Medicine(imageUrl, "Lexidol", "Tramadol Injection 50mg/ml", "Pain reliever injection", "Treats moderate to severe pain", "50"),
//        Medicine(imageUrl, "Mint Dex 10", "Dextrose 10%", "Intravenous solution", "Restores blood sugar levels", "10"),
//        Medicine(imageUrl, "Mint Dex 5", "Dextrose 5%", "Intravenous solution", "Restores blood sugar levels", "5"),
//        Medicine(imageUrl, "Mint Ns 0.9 (100ml)", "Sodium Chloride 0.9%", "Intravenous solution", "Restores electrolytes", "100"),
//        Medicine(imageUrl, "Mint Ns 0.9 (500ml)", "Sodium Chloride 0.9%", "Intravenous solution", "Restores electrolytes", "500")
//    )
//
//    criticalCareMedicines.forEach { medicine ->
//        database.child(medicine.name!!).setValue(medicine)
//            .addOnSuccessListener {
//                println("${medicine.name} added successfully!")
//            }
//            .addOnFailureListener { e ->
//                println("Failed to add ${medicine.name}: ${e.message}")
//            }
//    }
//}