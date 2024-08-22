package com.example.mintlifesciences.doctorMedicine

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorViewModel
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding

class DoctorMedicineActivity : AppCompatActivity() {
    lateinit var binding:ActivityDoctorMedicineBinding
    private lateinit var viewModel: DocMedicineViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_doctor_medicine)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding=DataBindingUtil.setContentView(this,R.layout.activity_doctor_medicine)
        viewModel= ViewModelProvider(this)[DocMedicineViewModel::class.java]
        viewModel.init(this)

       // binding

        val name = intent.getStringExtra("DOC_NAME")

        val textView = findViewById<TextView>(R.id.doc_t)
        textView.text = name
    }
}