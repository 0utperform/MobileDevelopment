package com.example.a0utperform.ui.decidelogin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.a0utperform.data.state.LoginState
import com.example.a0utperform.databinding.ActivityDecideLoginBinding
import com.example.a0utperform.ui.main_activity.ActivityMain
import com.example.a0utperform.ui.login.LoginActivity
import com.example.a0utperform.ui.register.RegisterActivity
import com.example.a0utperform.ui.setting.SettingViewModel

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityDecideLogin : AppCompatActivity() {

    private lateinit var binding: ActivityDecideLoginBinding
    private val viewModel: DecideLoginViewModel by viewModels()
    private val settingViewModel:SettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDecideLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)


        observeViewModel()
        setupClickListeners()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }
        viewModel.userSession.observe(this) { session ->
            if (session?.isLogin == true) {
                startActivity(Intent(this, ActivityMain::class.java))
                finish()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGoogleLogIn.setOnClickListener {
            viewModel.signInWithGoogle(this)
        }
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
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}