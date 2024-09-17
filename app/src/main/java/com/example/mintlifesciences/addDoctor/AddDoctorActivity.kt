package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityAddDoctorBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import org.w3c.dom.Text

class AddDoctorActivity : AppCompatActivity() {
    lateinit var binding: ActivityAddDoctorBinding
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var adapter: AddDoctorAdapter
    private lateinit var selectedItem: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_doctor)

        binding.recDocView.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this)[AddDoctorViewModel::class.java]
        viewModel.init(this)

        selectedItem = intent.getStringExtra("SELECTED_ITEM") ?: ""

        adapter = AddDoctorAdapter(this, emptyList(), selectedItem, viewModel)
        binding.recDocView.adapter = adapter

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        viewModel.loadDoctorData(selectedItem)

        viewModel.docData.observe(this, Observer { doctors ->
            Log.d("AddDoctorActivity", "Received data: $doctors")
            adapter.updateList(doctors)
        })

        binding.btn.setOnClickListener {
            showDoctorDialog()
        }

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the current activity
            }
        })
    }

    private fun showDoctorDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null)

        dialogView.setOnClickListener {
            val intent = Intent(this, DoctorMedicineActivity::class.java)
            startActivity(intent)
        }

        val docName = dialogView.findViewById<TextInputEditText>(R.id.doc_edit)
        val docSpec = dialogView.findViewById<TextInputEditText>(R.id.spec_edit)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialogView.findViewById<AppCompatButton>(R.id.cnfrmBtn).setOnClickListener {
            val name = docName.text?.toString()?.trim()?.split(" ")?.joinToString(" ") { it.capitalize() } ?: ""
            val speciality = docSpec.text?.toString()?.trim()?.split(" ")?.joinToString(" ") { it.capitalize() } ?: ""

            if (name.isEmpty() || speciality.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val doctor = DoctorData(name, speciality)
            viewModel.saveDoctorData(selectedItem, doctor)
            viewModel.addDoctor(doctor)  // Ensure both methods are necessary
            dialog.dismiss()
        }

        dialog.show()
    }
}
