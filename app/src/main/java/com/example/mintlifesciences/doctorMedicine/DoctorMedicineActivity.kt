package com.example.mintlifesciences.doctorMedicine

import DocMedicineViewModel
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding

class DoctorMedicineActivity : AppCompatActivity() {
    lateinit var binding: ActivityDoctorMedicineBinding
    private lateinit var viewModel: DocMedicineViewModel
    private lateinit var brandName : String
    private lateinit var doctorName : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_doctor_medicine)
        viewModel = ViewModelProvider(this)[DocMedicineViewModel::class.java]

//        // Retrieve the doctor's name from the intent
       doctorName = intent.getStringExtra("DOC_NAME") ?: ""
//        brandName = intent.getStringExtra("brand_name") ?: ""

        binding.docT.text = doctorName

        // Observe changes to the selected medicines
        viewModel.selectedMedicines.observe(this) { medicines ->
            // Update the UI with the list of selected medicines

        }

        binding.addMed.setOnClickListener {
            val intent = Intent(this, MedicineListActivity::class.java)
            intent.putExtra("brand_name", brandName )
            startActivity(intent)
        }
    }


}
