package com.example.mintlifesciences.aboutUs

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityAboutUsBinding
import com.example.mintlifesciences.homescreen.HomeActivity

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding=DataBindingUtil.setContentView(this,R.layout.activity_about_us)

        binding.backArrow.setOnClickListener {
            val intent = Intent(this,HomeActivity::class.java)
            startActivity(intent)
        }
    }
}