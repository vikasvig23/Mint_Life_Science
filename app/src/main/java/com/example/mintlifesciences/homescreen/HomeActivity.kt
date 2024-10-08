package com.example.mintlifesciences.homescreen

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mintlifesciences.R
import com.example.mintlifesciences.UserData
import com.example.mintlifesciences.aboutUs.AboutUsActivity
import com.example.mintlifesciences.addDoctor.AddDoctorActivity
import com.example.mintlifesciences.databinding.ActivityHomeBinding
import com.example.mintlifesciences.login.LoginActivity
import com.example.mintlifesciences.login.LoginViewModel
import com.example.mintlifesciences.recentDoctors.RecentDoctorsActivity
import com.example.mintlifesciences.utils.AppUtils
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.childEvents

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: HomeAdapter
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        viewModel.init(this)


        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        setupDrawer()


        adapter = HomeAdapter(emptyList()) { item ->
            navigateToMainActivity(item)
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

        // Navigation drawer setup
        setSupportActionBar(binding.toolbar)
        drawerToggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.open_nav, R.string.close_nav
        )
        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)

        val versionName = AppUtils.getAppVersion(this)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val versionTextView = navView.findViewById<TextView>(R.id.nav_ver)
        versionTextView.text = "MintLifeSciences $versionName"


        // Handle back button press on system back press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    // Custom behavior for back press, if any
                    finish() // Finish the current activity
                }
            }
        })

    }

    private fun setupDrawer() {
        val headerView = binding.navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.nav_header_user_name)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.nav_header_user_email)


        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var userName = sharedPreferences.getString("userName", null)
        val userEmail = sharedPreferences.getString("userEmail", "user@example.com")

       if(userName==null && userEmail!=null){

           loginViewModel.fetchUserName(userEmail){ fetchUser->
               fetchUser?.let { name->
                   with(sharedPreferences.edit()){
                       putString("userName",name)
                       apply()
                   }
                   userNameTextView.text=name
               }
           }
       }
        else{
            userNameTextView.text=userName
       }
        userEmailTextView.text=userEmail
    }

    private fun updateUI(items: List<String>?) {
        items?.let {
            adapter.updateItems(it)
        }
    }

    private fun navigateToMainActivity(item: String) {
        if (viewModel.isNetworkAvailable(getApplication().applicationContext)){
        val intent = Intent(this, AddDoctorActivity::class.java)
        intent.putExtra("SELECTED_ITEM", item)
        startActivity(intent)
    }
        else {
            Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {


        when (item.itemId) {
            R.id.nav_home -> {
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                startActivity(intent)
            }
            R.id.nav_doctors -> {
                // Open RecentDoctorsActivity
                val intent = Intent(this, RecentDoctorsActivity::class.java)
                startActivity(intent)
                finish()
            }

            R.id.nav_about->{
                val intent=Intent(this, AboutUsActivity::class.java)
                startActivity(intent)
            }

            R.id.nav_logout->{
                loginViewModel.logout()

            }
            // Add more cases here for other items if needed
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

}
