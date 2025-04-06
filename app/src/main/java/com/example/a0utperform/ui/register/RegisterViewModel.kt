package com.example.a0utperform.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.datastore.UserModel
import com.example.a0utperform.data.datastore.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _registerResult = MutableLiveData<Result<UserModel?>>()
    val registerResult: LiveData<Result<UserModel?>> get() = _registerResult

    fun registerUser(name: String, email: String, password: String, phone: String) {
        viewModelScope.launch {
            val result = authRepository.registerUser(name, email, password, phone)
            _registerResult.value = result
        }
    }
}