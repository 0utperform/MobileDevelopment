package com.example.a0utperform.ui.decidelogin

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.a0utperform.data.model.UserModel
import com.example.a0utperform.data.local.user.UserPreference
import com.example.a0utperform.data.repository.AuthRepository
import com.example.a0utperform.data.state.LoginState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DecideLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreference: UserPreference
) : ViewModel() {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState


    val userSession: LiveData<UserModel?> = userPreference.getSession().asLiveData()

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = authRepository.signInWithGoogleIdToken(context)
            } finally {
                _isLoading.value = false
            }
        }
    }
}