package com.example.mintlifesciences.homescreen

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.doctorMedicine.DoctorMedicineActivity
import com.example.mintlifesciences.medicinePresentation.MedicineScreenActivity
import com.example.mintlifesciences.model.BrandItem

class HomeActivity : AppCompatActivity(){

    lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter
    private lateinit var doctorName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)

        // Retrieve doctorName and brandName from the Intent
        doctorName = intent.getStringExtra("doctorName") ?: ""

        binding.backArrow.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish() // Finish the current activity
            }
        })


        adapter = HomeAdapter(emptyList()) { item ->
            navigateToNextScreen(item)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter



        viewModel.items.observe(this, Observer { items ->
            updateUI(items)
        })

//        binding.swipeRefreshLayout.setOnRefreshListener {
//            viewModel.refreshData()
//            Handler().postDelayed({
//                binding.swipeRefreshLayout.isRefreshing = false
//            }, 5000)
//        }

//        viewModel.loading.observe(this, Observer { isLoading ->
//            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
//            if (!isLoading) {
//                binding.swipeRefreshLayout.isRefreshing = false
//            }
//        })
//
//        viewModel.error.observe(this, Observer { errorMessage ->
//            errorMessage?.let {
//                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
//                viewModel.errorHandled()
//            }
//        })


        binding.menu.setOnClickListener {
            openMenu()
        }
    }

    private fun updateUI(items: List<BrandItem>) {
        items?.let {
            adapter.updateItems(it)
        }
    }

    private fun navigateToNextScreen(item: String) {
        if (viewModel.isNetworkAvailable(getApplication().applicationContext)){
        val intent = Intent(this, DoctorMedicineActivity::class.java)
        intent.putExtra("doctorName",doctorName)
        intent.putExtra("brandName",item)
        startActivity(intent)
    }
        else {
            Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
        }
    }

    private fun openMenu() {
        val popupMenu = PopupMenu(this, binding.menu)
        menuInflater.inflate(R.menu.home_menu, popupMenu.menu)
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.make_presentation -> {
                    // Set isPresentation to true for the doctor
                    viewModel.updateDoctorPresentationStatus(doctorName, true)

                    // Navigate to MedicineScreenActivity
                    val intent = Intent(this, MedicineScreenActivity::class.java)
                    intent.putExtra("doctorName", doctorName)
                    intent.putExtra("isPresentation", true)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }


}
