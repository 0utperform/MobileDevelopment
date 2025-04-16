package com.example.a0utperform.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _registerResult = MutableLiveData<Result<UserModel?>>()
    val registerResult: LiveData<Result<UserModel?>> get() = _registerResult

    fun registerUser(name: String, age: String, email: String, password: String, phone: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = authRepository.registerUser(name, age, email, password, phone)
                _registerResult.value = result
            } finally {
                _isLoading.value = false
            }
        }
    }
}