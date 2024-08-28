package com.example.mintlifesciences.doctorMedicine

import DocMedicineViewModel
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding

class DoctorMedicineActivity : AppCompatActivity() {
    lateinit var binding: ActivityDoctorMedicineBinding
    // Initialize ViewModel using the 'by viewModels()' delegate at the class level
    private val viewModel: DocMedicineViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    private lateinit var brandName: String
    private lateinit var doctorName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_doctor_medicine)

        // Retrieve the doctor's name and brand name from the intent
        doctorName = intent.getStringExtra("doctorName") ?: ""
        brandName = intent.getStringExtra("brandName") ?: ""

        binding.docT.text = doctorName

//        viewModel.selectedMedicines.observe(this) { medicines ->
//            Log.d("DoctorMedicineActivity", "Selected Medicines: ${medicines.joinToString { it.name ?: "Unknown" }}")
//
//            if (medicines.isNotEmpty()) {
//                val lastAddedMedicine = medicines.lastOrNull()?.name ?: "Unknown"
//                binding.mediName.text = lastAddedMedicine
//                Toast.makeText(this, "Last Added Medicine: $lastAddedMedicine", Toast.LENGTH_SHORT).show()
//            } else {
//                binding.mediName.text = "No Medicine Selected"
//            }
//        }

        binding.addMed.setOnClickListener {
            val intent = Intent(this, MedicineListActivity::class.java)
            intent.putExtra("brand_name", brandName)
            intent.putExtra("doctorName", doctorName)
            startActivity(intent)
        }
    }
}
