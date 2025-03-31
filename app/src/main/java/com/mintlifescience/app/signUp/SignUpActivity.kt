package com.mintlifescience.app.signUp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.mintlifescience.app.R
import com.mintlifescience.app.databinding.ActivitySignUpBinding
import com.mintlifescience.app.login.LoginActivity

class SignUpActivity : AppCompatActivity(), OnClickListener {

    lateinit var binding: ActivitySignUpBinding
    lateinit var viewModel: SignUpViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        viewModel = ViewModelProvider(this)[SignUpViewModel::class.java]
        viewModel.init(this)

        binding.btn.setOnClickListener(this)
        binding.txt3.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btn -> {
                val username = binding.nameInputLayout.editText?.text.toString()
                val email = binding.emailInputLayout.editText?.text.toString()
                val password = binding.passwordInputLayout.editText?.text.toString()

                if (validateInput(username, email, password)) {
                    viewModel.signUp(username, email, password)
                }
            }
            binding.txt3 -> {
                navigateToLogin()
            }
        }
    }

    private fun validateInput(username: String, email: String, password: String): Boolean {

        return username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()
    }

    fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
    }
}
