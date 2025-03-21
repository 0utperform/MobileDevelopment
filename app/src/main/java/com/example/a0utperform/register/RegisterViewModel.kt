package com.example.a0utperform.register

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
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerState = MutableLiveData<Result<FirebaseUser?>>()
    val registerState: LiveData<Result<FirebaseUser?>> get() = _registerState

    fun registerUser(email: String, password: String) {
        viewModelScope.launch {
            _registerState.value = repository.registerUser(email, password)
        }
    }
}