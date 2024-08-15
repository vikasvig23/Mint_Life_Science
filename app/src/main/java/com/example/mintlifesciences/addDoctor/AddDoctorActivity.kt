package com.example.mintlifesciences.addDoctor

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityAddDoctorBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.w3c.dom.Text

class AddDoctorActivity : AppCompatActivity() {

    lateinit var binding:ActivityAddDoctorBinding
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var adapter:AddDoctorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_doctor)
        supportActionBar?.hide()
        binding=DataBindingUtil.setContentView(this,R.layout.activity_add_doctor)
        //binding.btn.setOnClickListener(this)
        binding.recDocView.layoutManager=LinearLayoutManager(this)
        viewModel=ViewModelProvider(this)[AddDoctorViewModel::class.java]
        viewModel.init(this)
//        var data= ArrayList<DoctorData>()
//        data.add(DoctorData("",""))
//        val adapter=AddDoctorAdapter(data)
//       binding.recDocView.adapter=adapter

        viewModel.docData.observe(this, Observer { docData->
            adapter= AddDoctorAdapter(docData)
            binding.recDocView.adapter=adapter
            binding.recDocView.layoutManager=LinearLayoutManager(this)

        })

        binding.btn.setOnClickListener{

            showDoctorDialog()
        }



    }

    private fun showDoctorDialog() {



       val dialogView=LayoutInflater.from(this).inflate(R.layout.dialog_add_doctor,null)

        val docName=dialogView.findViewById<TextInputEditText>(R.id.doc_edit)
        val docSpec=dialogView.findViewById<TextInputEditText>(R.id.spec_edit)

        val dialog = AlertDialog.Builder(this)
            // .setTitle("Add Person")
            .setView(dialogView)
          //  .setNegativeButton("Cancel", null)
            .create()

        dialogView.findViewById<AppCompatButton>(R.id.cnfrmBtn).setOnClickListener{
            val name=docName.text.toString()
            val speciality=docSpec.text.toString()

                            if (name.isEmpty() || speciality.isEmpty() ) {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

            viewModel.addDoctor(DoctorData(name,speciality))
            dialog.dismiss()
        }
        dialog.show()

    }


}