package com.example.a0utperform.ui.setting.changepassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _changePasswordResult = MutableLiveData<Result<Unit>>()
    val changePasswordResult: LiveData<Result<Unit>> = _changePasswordResult

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            val result = authRepository.changePassword(newPassword)
            _changePasswordResult.value = result
        }
    }
}