package com.example.a0utperform.data.state


sealed class LoginState {
    data class Success(val message: String) : LoginState()
    data class Error(val message: String) : LoginState()
    object Loading : LoginState()
}