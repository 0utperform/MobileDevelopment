package com.example.a0utperform.decidelogin

import com.google.firebase.auth.FirebaseUser

sealed class LoginState {
    data class Success(val user: FirebaseUser?) : LoginState()
    data class Error(val message: String) : LoginState()
    object Loading : LoginState()
}