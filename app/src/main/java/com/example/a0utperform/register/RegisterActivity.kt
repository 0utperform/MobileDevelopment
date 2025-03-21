package com.example.a0utperform.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.dashboard.Dashboard
import com.example.a0utperform.databinding.ActivityDashboardBinding
import com.example.a0utperform.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInRegisterActivity.setOnClickListener {
            val email = binding.edEmail.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                registerViewModel.registerUser(email, password)
            } else {
                Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show()
            }
        }

        observeRegisterState()
    }

    private fun observeRegisterState() {
        registerViewModel.registerState.observe(this) { result ->
            result.onSuccess { user ->
                Toast.makeText(this, "Account created for ${user?.email}", Toast.LENGTH_SHORT).show()
                Intent(this, Dashboard::class.java).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }.onFailure { error ->
                Toast.makeText(this, error.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}