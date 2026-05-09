package com.mintlifescience.app.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.mintlifescience.app.addDoctor.AddDoctorActivity
import com.mintlifescience.app.databinding.ActivityLoginBinding
import com.mintlifescience.app.helperUtils.PrefsManager
import com.mintlifescience.app.signUp.SignUpActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PrefsManager.isLoggedIn(this)) {
            goToHome()
            return
        }

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        viewModel.navigateToHome.observe(this) { goToHome() }
        viewModel.navigateToLogin.observe(this) { /* already here */ }
        viewModel.errorMessage.observe(this) { msg ->
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
        viewModel.isLoading.observe(this) { loading ->
            binding.btn.isEnabled = !loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        binding.btn.setOnClickListener {
            val email = binding.emailInputLayout.editText?.text.toString().trim()
            val password = binding.passwordInputLayout.editText?.text.toString().trim()
            viewModel.login(email, password)
        }
        binding.txt3.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun goToHome() {
        startActivity(Intent(this, AddDoctorActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        finish()
    }
}
