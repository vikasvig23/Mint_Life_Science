package com.example.mintlifesciences.addDoctor

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityAddDoctorBinding
import org.w3c.dom.Text

class AddDoctorActivity : AppCompatActivity(),OnClickListener {

    private lateinit var binding:ActivityAddDoctorBinding
    private lateinit var viewModel: AddDoctorViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_doctor)
        supportActionBar?.hide()
        binding=DataBindingUtil.setContentView(this,R.layout.activity_add_doctor)
        binding.btn.setOnClickListener(this)
        binding.recDocView.layoutManager=LinearLayoutManager(this)
        viewModel=ViewModelProvider(this)[AddDoctorViewModel::class.java]
        viewModel.init(this)
        var data= ArrayList<DoctorData>()
        data.add(DoctorData("",""))
        val adapter=AddDoctorAdapter(data)
       binding.recDocView.adapter=adapter




    }

    override fun onClick(p0: View?) {
        TODO("Not yet implemented")
    }
}