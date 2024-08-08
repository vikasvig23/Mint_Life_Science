package com.example.mintlifesciences.homescreen

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
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
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.login.LoginViewModel
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding:ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_home)
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)
        setSupportActionBar(binding.root.findViewById(R.id.toolbar))
        supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.itemContainer

        viewModel.items.observe(this, Observer { items->
            updateUI(items)
        })
//        drawerLayout = binding.drawerLayout
//        navigationView = binding.navView

        val toggle=ActionBarDrawerToggle(this,binding.drawerLayout,binding.root.findViewById(R.id.toolbar), R.string.navigation_drawer_open,R.string.navigation_drawer_close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        binding.navView.setNavigationItemSelectedListener (this)
    }

    private fun updateUI(items: List<String>?) {
        binding.itemContainer.removeAllViews()
        if (items != null) {
            for (item in items) {
                val itemView = createItemView(item)
                binding.itemContainer.addView(itemView)
            }
        }
    }

    private fun createItemView(item: String): TextView {
        val textView = TextView(this).apply {
            text = item
            textSize = 20f // Increase text size
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.border)
            gravity=Gravity.CENTER
            stateListAnimator = AnimatorInflater.loadStateListAnimator(this@HomeActivity, R.animator.item_elevation_animator)
            setOnClickListener {
                Toast.makeText(this@HomeActivity, "Clicked: $item", Toast.LENGTH_SHORT).show()
            }
        }
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.FILL_PARENT
        ).apply {
            setMargins(16, 16, 16, 16) // Add margins to create gaps between items
            height= 120
        }
        textView.layoutParams = layoutParams
        return textView
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.drawer_menu, menu)
//        return true
//    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Handle Home click

            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}