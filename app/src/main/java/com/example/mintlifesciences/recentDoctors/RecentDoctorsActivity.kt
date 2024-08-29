package com.example.mintlifesciences.recentDoctors

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorAdapter
import com.example.mintlifesciences.addDoctor.AddDoctorViewModel
import com.example.mintlifesciences.databinding.ActivityRecentDoctorsBinding

class RecentDoctorsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecentDoctorsBinding
    private lateinit var selectedItem: String
    private lateinit var viewModel: AddDoctorViewModel
    private lateinit var adapter:RecentDoctorAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_recent_doctors)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding= DataBindingUtil.setContentView(this,R.layout.activity_recent_doctors)
        //binding.btn.setOnClickListener(this)
        binding.recDocView.layoutManager= LinearLayoutManager(this)
        viewModel= ViewModelProvider(this)[AddDoctorViewModel::class.java]
      //  viewModel.init(this)
        selectedItem = intent.getStringExtra("SELECTED_ITEM") ?: ""
        adapter = RecentDoctorAdapter(emptyList())
        binding.recDocView.adapter = adapter
        binding.recDocView.layoutManager = LinearLayoutManager(this)


        viewModel.loadDoctorData(selectedItem)

        viewModel.docData.observe(this, Observer { doctors ->
            Log.d("AddDoctorActivity", "Received data: $doctors")
            adapter.updateList(doctors)
        })

    }
}