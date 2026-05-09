package com.mintlifescience.app.signUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mintlifescience.app.databinding.ActivitySignUpBinding
import com.mintlifescience.app.login.LoginActivity

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]

        viewModel.navigateToLogin.observe(this) { goToLogin() }
        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        viewModel.successMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.btn.isEnabled = !loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        binding.btn.setOnClickListener {
            val username = binding.nameInputLayout.editText?.text.toString().trim()
            val email = binding.emailInputLayout.editText?.text.toString().trim()
            val password = binding.passwordInputLayout.editText?.text.toString().trim()
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.signUp(username, email, password)
        }
        binding.txt3.setOnClickListener { goToLogin() }
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
