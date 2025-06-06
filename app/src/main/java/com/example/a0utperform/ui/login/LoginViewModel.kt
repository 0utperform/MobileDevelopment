package com.example.a0utperform.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.a0utperform.data.model.UserModel

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _loginResult = MutableLiveData<Result<UserModel?>>()
    val loginResult: LiveData<Result<UserModel?>> get() = _loginResult

    fun loginUser(email: String, password: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.loginUser(email, password)
                _loginResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }

}