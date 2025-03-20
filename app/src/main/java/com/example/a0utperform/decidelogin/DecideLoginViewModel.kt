package com.example.a0utperform.decidelogin

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecideLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState


    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val user = authRepository.signInWithGoogle(context)
                _loginState.value = LoginState.Success(user)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun signInWithFacebook(activity: Activity) {
        authRepository.signInWithFacebook(
            activity,
            onSuccess = { _loginState.value = LoginState.Success(it) },
            onError = { error -> _loginState.value = LoginState.Error(error) }
        )
    }
}