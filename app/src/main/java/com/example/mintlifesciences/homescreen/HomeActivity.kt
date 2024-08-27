package com.example.mintlifesciences.homescreen

import android.animation.AnimatorInflater
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toolbar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.login.LoginViewModel
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding:ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_home)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE



        adapter = HomeAdapter(emptyList()) { item ->
            navigateToMainActivity(item)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.items.observe(this, Observer { items ->
            updateUI(items)
        })

        val toggle=ActionBarDrawerToggle(this,binding.drawerLayout,binding.root.findViewById(R.id.toolbar), R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener (this)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData() // Refresh data
            // Stop the refresh animation after 5 seconds
            Handler().postDelayed({
                binding.swipeRefreshLayout.isRefreshing = false
            }, 5000)
        }

        viewModel.loading.observe(this, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        })
        viewModel.error.observe(this, Observer { errorMessage ->
            errorMessage?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.errorHandled() // Reset error state after handling
            }
        })

    }


    private fun updateUI(items: List<String>?) {
        items?.let {
            adapter.updateItems(it)
        }
    }


    private fun navigateToMainActivity(item: String) {
        val intent = Intent(this, AddDoctorActivity::class.java)
        intent.putExtra("SELECTED_ITEM", item)
        startActivity(intent)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                val intent =Intent(this,HomeActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_doc->{

            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


}