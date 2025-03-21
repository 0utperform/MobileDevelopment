package com.example.a0utperform.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.dashboard.AuthRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableLiveData<Result<FirebaseUser?>>()
    val loginState: LiveData<Result<FirebaseUser?>> get() = _loginState

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = repository.loginUser(email, password)
        }
    }

    fun signOut() = repository.signOut()
}