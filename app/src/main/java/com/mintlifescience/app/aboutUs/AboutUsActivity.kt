package com.mintlifescience.app.aboutUs

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.mintlifescience.app.R
import com.mintlifescience.app.addDoctor.AddDoctorActivity

import com.mintlifescience.app.databinding.ActivityAboutUsBinding

class AboutUsActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAboutUsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_about_us)

        binding.backArrow.setOnClickListener {
            val intent = Intent(this, AddDoctorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
}

class ActivityAboutUsBinding {

}
