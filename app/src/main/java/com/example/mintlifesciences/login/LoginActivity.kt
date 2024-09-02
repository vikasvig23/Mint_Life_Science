package com.example.mintlifesciences.login


import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mintlifesciences.MainActivity
import com.example.mintlifesciences.databinding.ActivityLoginBinding
import com.example.mintlifesciences.homescreen.HomeActivity
import com.example.mintlifesciences.signUp.SignUpActivity

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginViewModel.init(this)

        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)


        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        if (isLoggedIn) {

            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.btn.setOnClickListener {
            val email = binding.emailInputLayout.editText?.text.toString().trim()
            val password = binding.passwordInputLayout.editText?.text.toString().trim()
            loginViewModel.login(email, password)
        }
        binding.txt3.setOnClickListener{
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}
