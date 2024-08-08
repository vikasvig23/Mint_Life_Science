package com.example.mintlifesciences.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.R
import com.example.mintlifesciences.databinding.ActivityLoginBinding
import com.example.mintlifesciences.signUp.SignUpActivity


class LoginActivity : AppCompatActivity(), OnClickListener {
    lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        viewModel.init(this)
        binding.btn.setOnClickListener(this)
        binding.txt3.setOnClickListener(this)


    }

    override fun onClick(v: View?) {
        when(v){
            binding.btn->{
               val intent=Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
            binding.txt3->{
                val intent=Intent(this, SignUpActivity::class.java)
                startActivity(intent)
            }
        }
    }
}