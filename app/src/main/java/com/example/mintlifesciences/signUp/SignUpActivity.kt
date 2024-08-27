package com.example.mintlifesciences.signUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivitySignUpBinding
import com.example.mintlifesciences.login.LoginActivity

class SignUpActivity : AppCompatActivity(), OnClickListener {

    lateinit var binding: ActivitySignUpBinding
    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_sign_up)
        viewModel=ViewModelProvider(this)[SignUpViewModel::class.java]
        viewModel.init(this)
        binding.btn.setOnClickListener(this)
        binding.txt3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            binding.btn->{
                val intent= Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            binding.txt3->{
                val intent= Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }
}