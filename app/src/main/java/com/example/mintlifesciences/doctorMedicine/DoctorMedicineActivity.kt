package com.example.mintlifesciences.doctorMedicine

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.addDoctor.AddDoctorViewModel
import com.example.mintlifesciences.databinding.ActivityDoctorMedicineBinding

class DoctorMedicineActivity : AppCompatActivity() {
    lateinit var binding:ActivityDoctorMedicineBinding
    private lateinit var viewModel: DocMedicineViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_doctor_medicine)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        binding=DataBindingUtil.setContentView(this,R.layout.activity_doctor_medicine)
        viewModel= ViewModelProvider(this)[DocMedicineViewModel::class.java]
        viewModel.init(this)

       binding.backArrow.setOnClickListener {
           onBackPressed()
       }

        val name = intent.getStringExtra("DOC_NAME")

        val textView = findViewById<TextView>(R.id.doc_t)
        textView.text = name
    }
}