package com.example.a0utperform.decidelogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.a0utperform.R
import com.example.a0utperform.dashboard.Dashboard
import com.example.a0utperform.databinding.ActivityDecideLoginBinding
import com.example.a0utperform.login.LoginActivity
import com.example.a0utperform.register.RegisterActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FacebookAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActivityDecideLogin : AppCompatActivity() {

    private lateinit var binding: ActivityDecideLoginBinding
    private val viewModel: DecideLoginViewModel by viewModels()
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDecideLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser

        if (firebaseUser != null) {
            startActivity(Intent(this,Dashboard::class.java))
            finish()
            return
        }

        observeViewModel()
        setupClickListeners()
    }


    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Success -> {
                    startActivity(Intent(this, Dashboard::class.java))
                    finish()
                }
                is LoginState.Error -> showToast(state.message)
                is LoginState.Loading -> showLoading(true)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnGoogleLogIn.setOnClickListener { viewModel.signInWithGoogle(this) }
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