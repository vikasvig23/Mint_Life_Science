package com.example.mintlifesciences.addDoctor

import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityAddDoctorBinding
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import com.example.mintlifesciences.homescreen.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text

class AddDoctorActivity : AppCompatActivity() {

    lateinit var binding: ActivityAddDoctorBinding
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var adapter: AddDoctorAdapter
    private lateinit var selectedItem: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_doctor)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE

        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_doctor)
        //binding.btn.setOnClickListener(this)
        binding.recDocView.layoutManager = LinearLayoutManager(this)
        viewModel = ViewModelProvider(this)[AddDoctorViewModel::class.java]
        viewModel.init(this)
        selectedItem = intent.getStringExtra("SELECTED_ITEM") ?: ""

        adapter = AddDoctorAdapter(this, emptyList(),selectedItem)
        binding.recDocView.adapter = adapter
        binding.recDocView.layoutManager = LinearLayoutManager(this)


        binding.backArrow.setOnClickListener{
            onBackPressed()
            true
        }

        viewModel.loadDoctorData(selectedItem)



        viewModel.loadDoctorData(selectedItem)

        viewModel.docData.observe(this, Observer { doctors ->
            Log.d("MainActivity", "Received data: $doctors")
            adapter.updateList(doctors)
        })


        binding.btn.setOnClickListener {

            showDoctorDialog()
        }


    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Navigate back to MainActivity
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish() // Destroy SecondActivity when navigating back to MainActivity
    }

    private fun showDoctorDialog() {


        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor, null)

        dialogView.setOnClickListener{
            val intent=Intent(this,DoctorMedicineActivity::class.java)
            startActivity(intent)
            finish()
        }

        val docName = dialogView.findViewById<TextInputEditText>(R.id.doc_edit)
        val docSpec = dialogView.findViewById<TextInputEditText>(R.id.spec_edit)

        val dialog = AlertDialog.Builder(this)
            // .setTitle("Add Person")
            .setView(dialogView)
            //  .setNegativeButton("Cancel", null)
            .create()

        dialogView.findViewById<AppCompatButton>(R.id.cnfrmBtn).setOnClickListener {
            val name = docName.text.toString()
            val speciality = docSpec.text.toString()

            if (name.isEmpty() || speciality.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val doctor = DoctorData(name, speciality)
            viewModel.saveDoctorData(selectedItem, doctor)
            viewModel.addDoctor(DoctorData(name, speciality))
            dialog.dismiss()
        }
        dialog.show()

    }
}