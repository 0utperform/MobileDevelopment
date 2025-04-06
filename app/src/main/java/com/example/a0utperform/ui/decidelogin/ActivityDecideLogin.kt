package com.example.a0utperform.ui.decidelogin

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.data.state.LoginState
import com.example.a0utperform.databinding.ActivityDecideLoginBinding
import com.example.a0utperform.ui.dashboard.ActivityMain
import com.example.a0utperform.ui.login.LoginActivity
import com.example.a0utperform.ui.register.RegisterActivity

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityDecideLogin : AppCompatActivity() {

    private lateinit var binding: ActivityDecideLoginBinding
    private val viewModel: DecideLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDecideLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        observeViewModel()
        setupClickListeners()
    }


    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    startActivity(Intent(this, ActivityMain::class.java))
                    finish()
                }
                is LoginState.Error -> showToast(state.message)
                is LoginState.Loading -> showLoading(true)
            }
        }
    }

    private fun setupClickListeners() {
        //binding.btnGoogleLogIn.setOnClickListener { viewModel.signInWithGoogle(this) }
        binding.signIn.setOnClickListener { navigateTo(LoginActivity::class.java) }
        binding.signUp.setOnClickListener { navigateTo(RegisterActivity::class.java) }
    }

    private fun navigateTo(destination: Class<*>) {
        startActivity(Intent(this, destination))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(isLoading: Boolean) {
        // Show loading indicator logic
    }
}