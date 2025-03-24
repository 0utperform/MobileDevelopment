package com.example.a0utperform.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.databinding.ActivityLoginBinding
import com.example.a0utperform.ui.dashboard.ActivityMain
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.login.setOnClickListener {
            val email = binding.edEmail.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginViewModel.loginUser(email, password)
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        observeLoginState()
    }

    private fun observeLoginState() {
        loginViewModel.loginState.observe(this) { result ->
            result?.onSuccess { user ->
                Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                Intent(this, ActivityMain::class.java).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }?.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}