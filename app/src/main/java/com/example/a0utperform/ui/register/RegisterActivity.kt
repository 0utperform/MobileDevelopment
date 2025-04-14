package com.example.a0utperform.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.a0utperform.databinding.ActivityRegisterBinding
import com.example.a0utperform.ui.decidelogin.ActivityDecideLogin
import com.example.a0utperform.ui.decidelogin.DecideLoginViewModel
import com.example.a0utperform.ui.login.LoginActivity
import com.example.a0utperform.ui.main_activity.ActivityMain
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signIn.setOnClickListener {
            val name = binding.edName.text.toString().trim()
            val age = binding.edAge.text.toString().trim() ?: "0"
            val email = binding.edEmail.text.toString().trim()
            val password = binding.edPassword.text.toString().trim()
            val phone = binding.edPhone.text.toString().trim()
            val confirmPassword = binding.edPasswordConfirmation.text.toString().trim()

            fun isValidPhoneNumber(number: String): Boolean {
                return number.length in 10..15 && number.all { it.isDigit() }
            }

            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() || confirmPassword.isEmpty() || age.isEmpty() -> {
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
                }
                !isValidPhoneNumber(phone) -> {
                    Toast.makeText(this, "Phone number must be 10-15 digits with no symbols", Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
                age.length > 3  -> {
                    Toast.makeText(this, "Please fill with the right age format ", Toast.LENGTH_SHORT).show()
                }
                age.length <= 0  -> {
                    Toast.makeText(this, "Please fill with the right age format ", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    registerViewModel.registerUser(name, age, email, password, phone )
                }
            }
        }
        setupClickListeners()
        observeRegisterState()


    }

    private fun setupClickListeners() {
        binding.logIn.setOnClickListener { navigateTo(LoginActivity::class.java) }
    }
    private fun navigateTo(destination: Class<*>) {
        val intent = Intent(this, destination)
        startActivity(intent)
        finish() // removes the current activity from the back stack
    }

    private fun observeRegisterState() {
        registerViewModel.registerResult.observe(this) { result ->
            result.onSuccess {
                // Not expected to happen in this flow
            }.onFailure { error ->
                val message = error.message ?: "Registration failed"

                if (message.contains("verify your email", ignoreCase = true)) {
                    // Treat it as a success
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    Intent(this, ActivityDecideLogin::class.java).also { intent ->
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}